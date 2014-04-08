package cr0s.javara.util;

public class Point {
	public float x;
	public float y;
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public int getTileX() {
		return (int)x;
	}
	
	public int getTileY() {
		return (int)y;
	}
}
