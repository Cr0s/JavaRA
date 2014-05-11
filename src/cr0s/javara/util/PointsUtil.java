package cr0s.javara.util;

import org.newdawn.slick.geom.Point;

public class PointsUtil {
    public static Point interpolatePos(Point from, Point to, int mul, int div) {
	int fx = (int) from.getX();
	int fy = (int) from.getY();
	
	int tx = (int) to.getX();
	int ty = (int) to.getY();
	
	int px = lerp(fx, tx, mul, div);
	int py = lerp(fy, ty, mul, div);
	
	return new Point(px, py);
    }
    
	
    public static int lerp(int a, int b, int mul, int div )
    {
	return a + (b - a) * mul / div;
    }    
    
    public static int distanceSq(Point p1, Point p2) {
	float dx = p1.getX() - p2.getX();
	float dy = p1.getY() - p2.getY();
	
	return (int) Math.ceil(dx * dx + dy * dy);
    }
}
