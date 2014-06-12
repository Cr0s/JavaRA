package cr0s.javara.util;

import org.newdawn.slick.geom.Point;

public class Pos extends Point {

    private float z;
    
    public Pos(float x, float y) {
	super(x, y);
	
	this.z = 0;
    }

    public Pos(float x, float y, float z) {
	this(x, y);
	
	this.z = z;
    }
    
    public int getCellX() {
	return (int) this.x / 24;
    }
    
    public int getCellY() {
	return (int) this.y / 24;
    }
    
    public int getCellZ() {
	return (int) this.z / 24;
    }    
    
    public Pos getCellPos() {
	return new Pos(getCellX(), getCellY(), getCellZ());
    }
    
    public float getZ() {
	return this.z;
    }
    
    public void setZ(float z) {
	this.z = z;
    }
    
    public int distanceToSq(Pos other) {
	float dx = this.x - other.getX();
	float dy = this.y - other.getY();
	float dz = this.z - other.getZ();

	return (int) Math.ceil(dx * dx + dy * dy + dz * dz);	
    }
    
    public double distanceTo(Pos other) {
	return Math.sqrt(this.distanceToSq(other));
    }
}
