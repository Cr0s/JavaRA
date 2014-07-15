package cr0s.javara.entity.building.soviet;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Point;

import cr0s.javara.combat.Armament;
import cr0s.javara.combat.attack.AttackCharge;
import cr0s.javara.combat.attack.AttackCharge.Charge;
import cr0s.javara.combat.attack.AttackTurreted;
import cr0s.javara.combat.attack.AutoTarget;
import cr0s.javara.combat.weapon.WeaponTeslaZap;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDefense;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.IPips;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.actor.activity.activities.harvester.FindResources;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IOreCapacitor;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.Pos;

public class EntityTeslaCoil extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IHaveCost, IDefense {

    private SpriteSheet sheet;

    private final String TEXTURE_NAME = "tsla.shp";
    private final String MAKE_TEXTURE_NAME = "tslamake.shp";

    public static final int WIDTH_TILES = 1;
    public static final int HEIGHT_TILES = 2;

    private static final int POWER_CONSUMPTION_LEVEL = 150;

    private static final int SHROUD_REVEALING_RANGE = 3;

    private final int CORRUPTED_OFFSET = 10;

    private int currentFrame = 0;
    private static final int BUILDING_COST = 1200;

    private boolean isCharging;
    private int chargeTicks = 0;
    private int chargeFrame = 0;
    private final int MAX_CHARGE_FRAMES = 9;
    private final int CHARGE_OFFSET = 1;

    private AttackCharge attack;
    private Armament arma;

    private AutoTarget autoTarget;

    private Turret turret;

    private boolean wasIdle;    

    public EntityTeslaCoil(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "_ x");

	setBibType(BibType.NONE);
	setProgressValue(-1);

	setMaxHp(400);
	setHp(getMaxHp());

	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();

	this.unitProductionAlingment = Alignment.SOVIET;

	this.requiredToBuild.add(EntityWarFactory.class);

	this.attack = new AttackCharge(this);
	this.attack.setMaxChargeTicks(this.MAX_CHARGE_FRAMES * 2);
	this.attack.setRechargeTicks(20 * 3);

	this.arma = new Armament(this, new WeaponTeslaZap());
	this.arma.addBarrel(new Pos(0, 10, 32), 0);

	this.attack.addArmament(this.arma);

	this.autoTarget = new AutoTarget(this, this.attack);

	this.ordersList.addAll(this.attack.getOrders());	

	this.setName("tsla");
    }

    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);

	this.sheet = new SpriteSheet(tex.getAsCombinedImage(owner.playerColor), 24, 48);
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	int textureIndex = (this.getHp() < this.getMaxHp() / 2) ? this.CORRUPTED_OFFSET + this.currentFrame  : this.currentFrame;

	this.sheet.getSubImage(0, textureIndex).draw(nx, ny);

	// Draw bounding box if debug mode is on
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(2);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    g.setLineWidth(1);
	}

	// Draw range circle
	if (this.isSelected) {
	    Circle c = new Circle(this.getPosition().getX(), this.getPosition().getY(), this.attack.getMaxRange() * 24 - this.autoTarget.RANGE_TOLERANCE);
	    g.setColor(Color.yellow);
	    g.draw(c);
	}	

	// Render repairing wrench
	if (this.repairIconBlink) {
	    repairImage.draw(this.boundingBox.getX() + this.boundingBox.getWidth() / 2 - repairImage.getWidth() / 2, this.boundingBox.getY() + this.boundingBox.getHeight() / 2 - repairImage.getHeight() / 2);
	}	
    }

    @Override
    public boolean shouldRenderedInPass(int passnum) {
	return passnum == -1;
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);

	this.attack.update(delta);
	this.autoTarget.update(delta);	

	this.boundingBox.setBounds(this.posX, this.posY + 12, this.WIDTH_TILES * 24, HEIGHT_TILES * 24 - 12);

	if (this.isIdle() || !(this.currentActivity instanceof Charge)) {
	    this.wasIdle = true;

	    this.isCharging = false;
	    this.chargeFrame = 0;
	    this.currentFrame = 0;
	} else {
	    if (!this.isIdle() && this.currentActivity instanceof Charge && wasIdle) {
		this.isCharging = true;
		SoundManager.getInstance().playSfxAt("tslachg2", this.getPosition());

		this.wasIdle = false;
	    }
	}


	if (this.currentActivity instanceof Charge && this.chargeFrame != this.MAX_CHARGE_FRAMES) {
	    this.currentFrame = this.CHARGE_OFFSET + this.chargeFrame;

	    if (this.chargeFrame != this.MAX_CHARGE_FRAMES) {
		this.chargeFrame++;
	    }
	}
    }

    @Override
    public void select() {
	this.isSelected = true;
    }

    @Override
    public void cancelSelect() {
	this.isSelected = false;
    }

    @Override
    public boolean isSelected() {
	return this.isSelected;
    }

    @Override
    public float getHeightInTiles() {
	return this.tileHeight;
    }

    @Override
    public float getWidthInTiles() {
	return this.tileWidth;
    }

    @Override
    public int getConsumptionLevel() {
	return this.POWER_CONSUMPTION_LEVEL;
    }

    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }

    @Override
    public Image getTexture() {
	return this.sheet.getSubImage(0, 0);
    }

    @Override
    public int getBuildingCost() {
	return BUILDING_COST;
    }  

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (super.issueOrder(self, targeter, target, ia) == null) {
	    return this.attack.issueOrder(self, targeter, target, ia);
	}

	this.attack.cancelAttack();

	return super.issueOrder(self, targeter, target, ia);
    }

    @Override
    public void resolveOrder(Order order) {
	if (order.orderString.equals("Attack") || order.orderString.equals("Stop")) {
	    this.attack.resolveOrder(order);
	} else {
	    super.resolveOrder(order);
	}
    }    
}
