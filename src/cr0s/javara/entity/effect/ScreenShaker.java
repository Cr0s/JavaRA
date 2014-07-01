package cr0s.javara.entity.effect;

import java.util.LinkedList;

import cr0s.javara.main.Main;
import cr0s.javara.util.Pos;

public class ScreenShaker {

    private LinkedList<ShakeEffect> effects;
    private static ScreenShaker instance;

    private Pos minMultiplier = new Pos(-5f, -5f);
    private Pos maxMultiplier = new Pos(5f, 5f);

    private int ticks = 0;

    private ScreenShaker() {
	this.effects = new LinkedList<ShakeEffect>();
    }

    public static ScreenShaker getInstance() {
	if (instance == null) {
	    instance = new ScreenShaker();
	}

	return instance;
    }

    public void update(int delta) {
	if (!this.effects.isEmpty()) {
	    LinkedList<ShakeEffect> e = new LinkedList<ShakeEffect>();

	    for (ShakeEffect s : this.effects) {
		if (s.expiryTime > this.ticks) {
		    e.add(s);
		}
	    }

	    this.effects = e;

	    if (!this.effects.isEmpty()) {
		this.scrollScreen();
	    }	    
	}
	
	this.ticks++;
    }

    private Pos getScrollOffset() {
	return this.getMultiplier().mul(this.getIntensity()).mul(new Pos(
		(float) Math.sin((this.ticks * 4 * Math.PI) / 4),
		(float) Math.cos((this.ticks * 4 * Math.PI) / 5)));
    }

    private float getIntensity() {
	float intensity = 0f;

	Pos cp = new Pos(
		-Main.getInstance().getCamera().getOffsetX() + Main.getInstance().getContainer().getWidth() / 2,
		-Main.getInstance().getCamera().getOffsetY() + Main.getInstance().getContainer().getHeight() / 2);

	for (ShakeEffect e : this.effects) {
	    intensity += (float) e.intensity / e.position.distanceToSq(cp);
	}

	return Math.min(100 * 1024 * 1024 * intensity, 10);
    }

    private Pos getMultiplier() {
	Pos mul = new Pos(0, 0);

	for (ShakeEffect e : this.effects) {
	    mul = mul.add(e.multiplier);
	}

	if (mul.getX() < this.minMultiplier.getX()) {
	    mul.setX(this.minMultiplier.getX());
	}

	if (mul.getY() < this.minMultiplier.getY()) {
	    mul.setY(this.minMultiplier.getY());
	}

	if (mul.getX() > this.maxMultiplier.getX()) {
	    mul.setX(this.maxMultiplier.getX());
	}

	if (mul.getY() > this.maxMultiplier.getY()) {
	    mul.setY(this.maxMultiplier.getY());
	}	

	return mul;
    }

    private void scrollScreen() {
	Pos offset = this.getScrollOffset();

	Main.getInstance().getCamera().scrollBy(offset);
    }

    public void addEffect(int time, Pos position, int intensity) {
	ShakeEffect se = new ShakeEffect(this.ticks + time, position, intensity, new Pos(1, 1));

	this.effects.addLast(se);
    }

    private class ShakeEffect {
	public int expiryTime;
	public Pos position;
	public int intensity;
	public Pos multiplier;

	public ShakeEffect(int expiryTime, Pos position, int intensity, Pos multiplier) {
	    this.expiryTime = expiryTime;
	    this.position = position;
	    this.intensity = intensity;
	    this.multiplier = multiplier;
	}
    }
}
