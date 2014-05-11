package cr0s.javara.order;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;

public class Order {
    public String orderString;
    public Entity subject;
    
    public Point targetPosition;
    public Entity targetEntity;
    public String targetString;
    public Point extraPosition;
    
    public Order(String order, Entity subj, Point targetLoc, Entity targetEnt, String targetStr, Point extraLoc) {
	this.orderString = order;
	this.subject = subj;
	this.targetPosition = targetLoc;
	this.targetEntity = targetEnt;
	this.targetString = targetStr;
	this.extraPosition = extraLoc;
    }
    
    public Order(String order, Entity subj, Point targetLoc, Entity targetEnt, String targetStr) {
	this(order, subj, targetLoc, targetEnt, targetStr, null);
    }
    
    public Order(String order, Entity subj, Point targetLoc, Entity targetEnt) {
	this(order, subj, targetLoc, targetEnt, null, null);
    }
    
    public Order(String order, Entity subj, Point targetLoc) {
	this(order, subj, targetLoc, null, null, null);
    }
}
