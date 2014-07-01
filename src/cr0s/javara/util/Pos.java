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
	final int prime = 3;
	int result = 1;
	result = prime * result + (int) this.getX() + (prime * 2 + (int) this.getY()) + (prime * 3 + (int) this.getZ());
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
	if ((int) x != (int) other.x || (int)y != (int) other.y || (int) z != (int) other.z)
	    return false;
	return true;
    }

    public Pos add(Pos other) {
	Pos p = new Pos(0, 0);
	p.setX(this.getX() + other.getX());
	p.setY(this.getY() + other.getY());
	p.setZ(this.getZ() + other.getZ());
	
	return p;
    }
    
    @Override
    public String toString() {
	return "Pos (" + this.getX() + "; " + this.getY() + "; " + this.getZ() + ")";
    }

    public Pos sub(Pos other) {
	Pos p = new Pos(0, 0);
	p.setX(this.getX() - other.getX());
	p.setY(this.getY() - other.getY());
	p.setZ(this.getZ() - other.getZ());
	
	return p;
    }

    public Pos rotate2D(float angle) {
	return new Pos(this.getX() * (float) Math.sin(angle), this.getY() * (float) Math.cos(angle), this.getZ());
    }
    
    public Pos mul(float c) {
	Pos p = new Pos(0, 0);
	p.setX(this.getX() * c);
	p.setY(this.getY() * c);
	p.setZ(this.getZ() * c);
	
	return p;
    }

    public float getHorizontalLength() {
	return (float) Math.sqrt(this.getX() * this.getX() + this.getY() * this.getY());
    }

    public Pos mul(Pos other) {
	Pos p = new Pos(0, 0);
	p.setX(this.getX() * other.getX());
	p.setY(this.getY() * other.getY());
	p.setZ(this.getZ() * other.getZ());
	
	return p;
    }    
}
