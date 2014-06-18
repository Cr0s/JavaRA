package cr0s.javara.combat;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IEffect;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public abstract class Projectile extends Entity implements IEffect {    
    public Weapon weapon;
    public float firepowerModifier = 1.0f;

    public Pos pos, sourcePos;
    public EntityActor sourceActor;

    public Pos passiveTargetPos;
    public EntityActor guidedTarget;

    protected ShpTexture tex;
    protected Sequence projectileSq;

    public int numFacings = 32;
    protected int seqLength = 0;

    private Projectile(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    public Projectile(Pos srcPos, Pos passivePos, EntityActor targetActor, int width, int height) {
	super(srcPos.getX(), srcPos.getY(), null, null, width, height);

	this.sourcePos = srcPos;
	this.passiveTargetPos = passivePos;
	this.guidedTarget = targetActor;
    }

    public Projectile(EntityActor srcActor, Pos srcPos, Pos passivePos, EntityActor targetActor, int width, int height) {
	super(srcPos.getX(), srcPos.getY(), srcActor.team, srcActor.owner, width, height);

	this.sourceActor = srcActor;
	this.sourcePos = srcPos;
	this.passiveTargetPos = passivePos;
    }

    public void initTexture(String textureName, int facings, int len) {

	this.tex = ResourceManager.getInstance().getConquerTexture(textureName);

	if (this.tex != null) {
	    this.numFacings = facings;
	    this.seqLength = len;
	    this.projectileSq = new Sequence(tex, 0, facings, len, 1, null);
	}
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }    


    @Override
    public void updateEntity(int delta) {
	if (this.projectileSq != null) {
	    this.projectileSq.update(this.currentFacing);
	}

	this.posX = this.pos.getX();
	this.posY = this.pos.getY() - this.pos.getZ(); // Z is height above ground
    }

    @Override
    public void renderEntity(Graphics g) {
	if (this.projectileSq != null) {
	    this.projectileSq.render(this.pos.getX() - this.sizeWidth / 2, this.pos.getY() - this.pos.getZ() - this.sizeHeight / 2);
	}
    }    
}
