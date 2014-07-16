package cr0s.javara.entity.effect;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.combat.Combat;
import cr0s.javara.combat.Projectile;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IEffect;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.Target;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;

public class TeslaZapRenderer extends Projectile implements IEffect {
    private static SpriteSheet tex;

    static int[][] steps = {
	new int[] {  8,  8,  4,  4, 0 },
	new int[] { -8, -8, -4, -4, 0 },
	new int[] {  8,  0,  4,  4, 1 },
	new int[] { -8,  0, -4,  4, 1 },
	new int[] {  0,  8,  4,  4, 2 },
	new int[] {  0, -8,  4, -4, 2 },
	new int[] { -8,  8, -4,  4, 3 },
	new int[] {  8, -8,  4, -4, 3 },
    };    

    private final int BIAS_LIMIT = 24 * 6;

    static {
	tex = new SpriteSheet(ResourceManager.getInstance().getConquerTexture("litning.shp").getAsCombinedImage(null), 24, 24);
    }

    private int timeUntilRemove = 10; // in ticks
    private boolean doneDamage = false;
    private boolean initialized = false;

    private Target guided;

    private Pos p;
    private boolean isBright;

    public TeslaZapRenderer(EntityActor srcActor, Pos srcPos, Pos passivePos,
	    EntityActor targetActor, int width, int height, boolean isBright) {
	this(srcActor, srcPos, passivePos, targetActor, width, height);

	this.isBright = isBright;
    }    

    public TeslaZapRenderer(EntityActor srcActor, Pos srcPos, Pos passivePos,
	    EntityActor targetActor, int width, int height) {
	super(srcActor, srcPos, passivePos, targetActor, width, height);

	this.guided = new Target(this.guidedTarget);

	//this.p = this.guided.isValidFor(this.sourceActor) ? this.guidedTarget.getPosition() : this.passiveTargetPos;
    }

    @Override
    public void updateEntity(int delta) {
	if (--timeUntilRemove <= 0) {
	    this.setDead();

	    return;
	}
    }

    public static float getRandom(float from, float to) {
	return (float) (from + Math.random() * (to - from));
    }

    @Override
    public void renderEntity(Graphics g) {
	this.tex.startUse();

	this.p = this.guided.isValidFor(this.sourceActor) ? this.guidedTarget.getPosition() : this.passiveTargetPos;	
	this.drawZapWandering(this.sourcePos, this.p, this.world.getRandomInt(25, 50), 5.0f);

	this.tex.endUse();
    }      

    private void drawZapWandering(Pos from, Pos to, float displace, float detail) {
	/*System.out.println("From: " + from + " to " + to);
	Pos z = new Pos(0, 0);
	Pos dist = to.sub(from);
	Pos norm = new Pos(-dist.getY(), dist.getX()).mul(1.0f / dist.length());


	if (this.world.getRandomInt(0, 2) != 0) {
	    Pos p1 = from.add(dist.mul(1 / 3f)).add(norm.mul(PointsUtil.rangeFromPdf(this.world.getRandom(), 2) * dist.length() / this.world.getRandomInt(24, this.BIAS_LIMIT)));
	    Pos p2 = from.add(dist.mul(2 / 3f)).add(norm.mul(PointsUtil.rangeFromPdf(this.world.getRandom(), 2) * dist.length() / this.world.getRandomInt(24, this.BIAS_LIMIT)));

	    p1 = this.drawZap(from, p1);
	    p2 = this.drawZap(p1, p2);
	    z = this.drawZap(p2, to);
	} else {
	    Pos p1 = from.add(dist.mul(1 / 2f)).add(norm.mul(PointsUtil.rangeFromPdf(this.world.getRandom(), 10) * dist.length() / this.world.getRandomInt(24, 5 * 24)));

	    p1 = this.drawZap(from, p1);
	    z = this.drawZap(p1, to);
	}*/

	if (displace < detail) {
	    this.drawZap(from, to);
	} else {
	    float midX = (to.getX() + from.getX()) * 0.5f;
	    float midY = (to.getY() + from.getY()) * 0.5f;

	    midX += (Math.random() - 0.5f) * displace;
	    midY += (Math.random() - 0.5f) * displace;	
	    
	    //drawSingleP2PLightning(g, x1,y1,mid_x,mid_y,displace*0.5f, detail, thickness);
	    //drawSingleP2PLightning(g, x2,y2,mid_x,mid_y,displace*0.5f, detail, thickness);
	    this.drawZapWandering(from, new Pos(midX, midY), displace * 0.5f, detail);
	    this.drawZapWandering(to, new Pos(midX, midY), displace * 0.5f, detail);
	}
    }

    /**
     * Draws a zap and returns last zap position
     * @param from source position
     * @param to target position
     * @param isBright is bright zaps
     * @return last sprite position
     */
    private Pos drawZap(final Pos from, final Pos to) {
	Pos dist = to.sub(from);
	Pos q = new Pos(-dist.getY(), dist.getX());
	float c = -from.dot(q);
	int spriteCount = 0;
	Pos z = from;

	while (to.getX() - z.getX() > 5 || to.getX() - z.getX() < -5 || to.getY() - z.getY() > 5 || to.getY() - z.getY() < -5) {
	    int[] minStep = this.chooseStep(to, z, c, q);

	    if (minStep == null) {
		throw new RuntimeException("Something wrong with tesla");
	    }

	    // Add sprite to cache
	    float spriteX = z.getX() + minStep[2];
	    float spriteY = z.getY() + minStep[3];

	    //TeslaZapRenderer.tex.getSubImage(0, minStep[4] + (this.isBright ? 0 : 4)).draw((int) spriteX, (int) spriteY);
	    TeslaZapRenderer.tex.getSubImage(0, 24 * (minStep[4] + (this.isBright ? 0 : 4)), 24, 24).drawEmbedded((int) spriteX, (int) spriteY, 24, 24);

	    z = z.add(new Pos(minStep[0], minStep[1]));

	    if (++spriteCount >= 1000) {
		System.err.println("Sprite limit!");
		break;
	    }
	}

	return z;
    }

    private int[] chooseStep(Pos to, Pos z, float c, Pos q) {
	int[] minStep = null;
	float minValue = 0;

	for (int[] step : this.steps) {
	    float currentValue = to.distanceToSq(z.add(new Pos(step[0], step[1])));
	    if (minValue == 0 || currentValue < minValue) {
		minStep = step;
		minValue = currentValue;
	    }
	}

	return minStep;
    }
}
