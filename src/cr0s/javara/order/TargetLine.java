package cr0s.javara.order;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import cr0s.javara.util.Pos;

public class TargetLine {
    private Pos self;
    private ArrayList<Pos> targets;
    private Color color;
    
    private int lifeTime = 150;
    
    public TargetLine(Pos s, Pos target, Color c, int time) {
	this.self = s;
	this.targets = new ArrayList<>();
	this.targets.add(target);
	this.color = c;
	this.lifeTime = time;
    }
    
    public TargetLine(Pos s, Pos target, Color c) {
	this.self = s;
	this.targets = new ArrayList<>();
	this.targets.add(target);
	this.color = c;	
    }
    
    public void update(int delta) {
	/*this.lifeTime--;
	
	if (this.lifeTime <= 0) {
	    this.lifeTime = 0;
	}*/
    }
    
    public void render(Graphics g) {
	g.setColor(this.color);
	
	g.setLineWidth(1);
	for (Pos t : this.targets) {
	    g.drawLine(self.getX(), self.getY(), t.getX(), t.getY());
	    g.drawOval(t.getX() - 2, t.getY() - 2, 4, 4);
	}
    }
    
    public void refresh() {
	this.lifeTime = 150;
    }
}
