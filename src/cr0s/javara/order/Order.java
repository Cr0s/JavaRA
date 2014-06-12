package cr0s.javara.order;

import cr0s.javara.entity.Entity;
import cr0s.javara.util.Pos;

public class Order {
    public String orderString;
    public Entity subject;
    
    public Pos targetPosition;
    public Entity targetEntity;
    public String targetString;
    public Pos extraPosition;
    
    public Order(String order, Entity subj, Pos targetLoc, Entity targetEnt, String targetStr, Pos extraLoc) {
	this.orderString = order;
	this.subject = subj;
	this.targetPosition = targetLoc;
	this.targetEntity = targetEnt;
	this.targetString = targetStr;
	this.extraPosition = extraLoc;
    }
    
    public Order(String order, Entity subj, Pos targetLoc, Entity targetEnt, String targetStr) {
	this(order, subj, targetLoc, targetEnt, targetStr, null);
    }
    
    public Order(String order, Entity subj, Pos targetLoc, Entity targetEnt) {
	this(order, subj, targetLoc, targetEnt, null, null);
    }
    
    public Order(String order, Entity subj, Pos targetLoc) {
	this(order, subj, targetLoc, null, null, null);
    }
}
