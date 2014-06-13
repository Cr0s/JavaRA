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
    
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Float.floatToIntBits(this.x) + (prime * 2 + Float.floatToIntBits(this.y)) + (prime * 2 + Float.floatToIntBits(this.z));
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Pos other = (Pos) obj;
	if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x) || Float.floatToIntBits(y) != Float.floatToIntBits(other.y) || Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Pos (" + this.getX() + "; " + this.getY() + "; " + this.getZ() + ")";
    }
}
