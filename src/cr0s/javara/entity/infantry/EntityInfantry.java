package cr0s.javara.entity.infantry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.newdawn.slick.BigImage;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.activities.AttackInfantry;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.MoveInfantry;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.EntityBlockingMap.FillsSpace;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.SoundManager;

public abstract class EntityInfantry extends MobileEntity implements IShroudRevealer {

    private static final float DEFAULT_MOVE_SPEED = 0.5f;

    private static final int WIDTH = 50;
    private static final int HEIGHT = 39;

    public static final int MAX_FACING = 8;

    public static Point subcellOffsets[] = new Point[6];

    static {
	subcellOffsets[SubCell.TOP_LEFT.ordinal()] = new Point(-21, -12);
	subcellOffsets[SubCell.TOP_RIGHT.ordinal()] = new Point(-5, -12);

	subcellOffsets[SubCell.CENTER.ordinal()] = new Point(-13, -6);

	subcellOffsets[SubCell.BOTTOM_LEFT.ordinal()] = new Point(-21, 2);
	subcellOffsets[SubCell.BOTTOM_RIGHT.ordinal()] = new Point(-5, 2);
    }

    protected ShpTexture texture;
    protected int currentFrame;

    protected Sequence currentSequence;
    protected Sequence standSequence;
    protected Sequence runSequence;    
    protected Sequence attackingSequence;
    protected ArrayList<Sequence> idleSequences = new ArrayList<>();

    private int randomTicksBeforeIdleSeq = 0;

    public enum AnimationState { IDLE, ATTACKING, MOVING, IDLE_ANIMATING, WAITING };
    private AnimationState currentAnimationState;


    private final static int MIN_IDLE_DELAY_TICKS = 350;
    private final static int MAX_IDLE_DELAY_TICKS = 900;

    private final String SELECTED_SOUND = "ready";
    private HashMap<String, Integer[]> orderSounds;
    private final int MAX_VERSIONS = 4;    

    public EntityInfantry(Float posX, Float posY, Team team, Player owner) {
	this(posX, posY, team, owner, SubCell.CENTER);
    }

    public EntityInfantry(Float posX, Float posY, Team team, Player owner, SubCell sub) {
	super(posX, posY, team, owner, WIDTH, HEIGHT);

	this.currentSubcell = sub;

	this.posX += subcellOffsets[sub.ordinal()].getX();
	this.posY += subcellOffsets[sub.ordinal()].getY();

	this.fillsSpace = FillsSpace.ONE_SUBCELL;

	this.setCurrentAnimationState(AnimationState.IDLE);

	this.selectedSounds.put(SELECTED_SOUND, new Integer[] { 1, 3 } );
	this.selectedSounds.put("report1", new Integer[] { 0, 1, 2, 3 } );
	this.selectedSounds.put("yessir1", new Integer[] { 0, 1, 2, 3 } );

	this.orderSounds = new HashMap<>();
	this.orderSounds.put("ackno", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("affirm1", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("noprob", new Integer[] { 1, 3 });
	this.orderSounds.put("overout", new Integer[] { 1, 3 });
	this.orderSounds.put("ritaway", new Integer[] { 1, 3 });
	this.orderSounds.put("roger", new Integer[] { 1, 3 });
	this.orderSounds.put("ugotit", new Integer[] { 1, 3 });

	this.unitVersion = SoundManager.getInstance().r.nextInt(4); // from 0 to 3	

	this.randomTicksBeforeIdleSeq = (int) (this.MIN_IDLE_DELAY_TICKS + Math.random() * (this.MAX_IDLE_DELAY_TICKS - this.MIN_IDLE_DELAY_TICKS));
    }

    @Override
    public Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY) {
	return world.getInfantryPathfinder().findPathFromTo((EntityInfantry) e, aGoalX, aGoalY);
    }

    @Override
    public boolean canEnterCell(Point cellPos) {
	return world.isCellPassable(cellPos, (this.desiredSubcell == null) ? this.currentSubcell : this.desiredSubcell);
    }

    @Override
    public float getMoveSpeed() {
	return DEFAULT_MOVE_SPEED;
    }    

    @Override
    public float getTextureX() {
	return this.posX;
    }

    @Override
    public float getTextureY() {
	return this.posY;
    } 

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);

	if (this.currentSequence != null) { 
	    this.currentSequence.update(this.currentFacing);
	}

	this.boundingBox.setBounds(this.posX + this.texture.width / 3 + 2, this.posY + this.texture.height / 2 - 15, 13, 18);
	if ((this.currentActivity instanceof MoveInfantry || this.currentActivity instanceof MoveInfantry.MovePart) && this.getCurrentAnimationState() != AnimationState.WAITING) {
	    this.setCurrentAnimationState(AnimationState.MOVING);
	    this.currentSequence = this.runSequence;
	} else if (currentActivity instanceof AttackInfantry) {
	    this.setCurrentAnimationState(AnimationState.ATTACKING);
	    this.currentSequence = this.attackingSequence;
	} else if (this.isIdle()) {
	    if (this.getCurrentAnimationState() != AnimationState.IDLE && this.getCurrentAnimationState() != AnimationState.IDLE_ANIMATING) {
		this.setCurrentAnimationState(AnimationState.IDLE);
		this.currentSequence = this.standSequence;
	    } else if (this.getCurrentAnimationState() == AnimationState.IDLE) {
		if (--this.randomTicksBeforeIdleSeq <= 0) {
		    this.randomTicksBeforeIdleSeq = world.getRandomInt(this.MIN_IDLE_DELAY_TICKS, this.MAX_IDLE_DELAY_TICKS);

		    if (this.idleSequences.size() > 0) {
			this.currentSequence = this.idleSequences.get(world.getRandomInt(0, this.idleSequences.size()));
			this.setCurrentAnimationState(AnimationState.IDLE_ANIMATING);
		    }
		} else { // Waiting for idle animation in stand state
		    this.currentSequence = this.standSequence;
		}
	    } else if (this.getCurrentAnimationState() == AnimationState.IDLE_ANIMATING) {
		if (this.currentSequence.isFinished()) {
		    this.setCurrentAnimationState(AnimationState.IDLE);
		    this.currentSequence.reset();
		}
	    }
	} else if (this.getCurrentAnimationState() == AnimationState.WAITING) {
	    this.currentSequence = this.standSequence;
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	//if (this.sheet != null) {
	this.currentSequence.render(this.posX, this.posY);
	//}
    }

    @Override
    public void moveTo(Point destCell, EntityBuilding ignoreBuilding) {
	this.goalX = (int) destCell.getX();
	this.goalY = (int) destCell.getY();

	MoveInfantry move = new MoveInfantry(this, destCell, getMinimumEnoughRange(), ignoreBuilding);

	// If we already moving
	if (this.currentActivity instanceof MoveInfantry) {
	    this.currentActivity.cancel();
	} else if (this.currentActivity instanceof MoveInfantry.MovePart) {
	    this.currentActivity.queueActivity(move);
	    return;
	}

	queueActivity(move);
    }

    @Override
    public void startMovingByPath(Path p, EntityBuilding ignoreBuilding) {
	this.goalX = (int) p.getX(p.getLength() - 1);
	this.goalY = (int) p.getY(p.getLength() - 1);

	queueActivity(new MoveInfantry(this, p, new Point(goalX, goalY), ignoreBuilding));
    }     

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

    @Override
    public float getCenterPosX() {
	return this.boundingBox.getCenterX();
    }

    @Override
    public float getCenterPosY() {
	return this.boundingBox.getCenterY();
    }	

    public String getSelectSound() {
	return SELECTED_SOUND;
    }    

    @Override
    public void playSelectedSound() {
	for (String s : this.selectedSounds.keySet()) {
	    Integer[] versions = this.selectedSounds.get(s);

	    boolean canPlay = false;
	    for (int i = 0; i < Math.min(MAX_VERSIONS, versions.length); i++) {
		if (versions[i] == this.unitVersion) {
		    canPlay = true;
		    break;
		}
	    }

	    if (SoundManager.getInstance().r.nextBoolean() && canPlay) {
		SoundManager.getInstance().playUnitSoundGlobal(this, s, this.unitVersion);
		return;
	    }
	}

	SoundManager.getInstance().playUnitSoundGlobal(this, SELECTED_SOUND, 1);
    }    

    @Override
    public void playOrderSound() {
	// Play order sound
	for (String s : this.orderSounds.keySet()) {
	    Integer[] versions = this.orderSounds.get(s);

	    if (this.unitVersion >= versions.length) {
		continue;
	    }

	    boolean canPlay = false;
	    for (int i = 0; i < Math.min(MAX_VERSIONS, versions.length); i++) {
		if (versions[i] == this.unitVersion) {
		    canPlay = true;
		    break;
		}
	    }

	    if (SoundManager.getInstance().r.nextBoolean() && canPlay) {
		SoundManager.getInstance().playUnitSoundGlobal(this, s, this.unitVersion);
		return;
	    }
	}

	if (SoundManager.getInstance().r.nextBoolean()) {
	    SoundManager.getInstance().playUnitSoundGlobal(this, "ackno", this.unitVersion);
	} else {
	    SoundManager.getInstance().playUnitSoundGlobal(this, "affirm1", this.unitVersion);
	}	
    }

    public AnimationState getCurrentAnimationState() {
	return currentAnimationState;
    }

    public void setCurrentAnimationState(AnimationState currentAnimationState) {
	this.currentAnimationState = currentAnimationState;
    }    
}
