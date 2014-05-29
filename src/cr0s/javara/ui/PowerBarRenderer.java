package cr0s.javara.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;

public class PowerBarRenderer {
    private int width, height;
    private Point barPos;

    public Color powerNormal = Color.green;
    public Color powerDown = Color.yellow;
    public Color powerDownCritical = Color.red;

    private Ewma providedLerp = new Ewma(0.03f);
    private Ewma usedLerp = new Ewma(0.03f);

    public PowerBarRenderer(Point aBarPos, int aWidth, int aHeight) {
	this.barPos = aBarPos;

	this.width = aWidth;
	this.height = aHeight;
    }

    public void render(Graphics g, Color filterColor) {
	float scaleBy = 100.0f;
	int provided = Main.getInstance().getPlayer().getBase().getPowerLevel();
	int used = Main.getInstance().getPlayer().getBase().getConsumptionLevel();

	int max = Math.max(provided, used);
	while (max >= scaleBy) {
	    scaleBy *= 2;
	}

	float providedFrac = providedLerp.update(provided / scaleBy);
	float usedFrac = usedLerp.update(used / scaleBy);

	Color color = (used > provided) ? (((float) provided / (float) used <= 0.6f) ? this.powerDownCritical : this.powerDown) : this.powerNormal;
	float barY = lerp(barPos.getY(), barPos.getY() - this.height, providedFrac);
	
	g.setColor(color.multiply(filterColor));
	g.fillRect(barPos.getX(), barY, (float) this.width, providedFrac * this.height);

	g.setColor(Color.white.multiply(filterColor));
	float usedHeight = lerp(barPos.getY(), barPos.getY() - this.height, usedFrac);
	
	g.fillRect(barPos.getX() + (this.width / 2), barPos.getY() - this.height * usedFrac, this.width / 3, this.height * usedFrac);
    }	

    private class Ewma {
	private float animRate;
	private float value = 0;

	public Ewma (float aAnimRate) {
	    this.animRate = aAnimRate;
	}

	public float update(float newValue) {
	    this.value = lerp(this.value, newValue, this.animRate);
	    return value;
	}
    }

    public static float lerp(float a, float b, float t) { return a + t * (b - a); }
}
