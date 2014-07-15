package cr0s.javara.util;

import java.util.Random;

import org.newdawn.slick.geom.Point;

public class PointsUtil {
    public static Pos interpolatePos(Pos from, Pos to, int mul, int div) {
	int fx = (int) from.getX();
	int fy = (int) from.getY();
	int fz = (int) from.getZ();

	int tx = (int) to.getX();
	int ty = (int) to.getY();
	int tz = (int) to.getZ();

	int px = lerp(fx, tx, mul, div);
	int py = lerp(fy, ty, mul, div);
	int pz = lerp(fz, tz, mul, div);

	return new Pos(px, py, pz);
    }


    public static int lerp(int a, int b, int mul, int div )
    {
	return a + (b - a) * mul / div;
    }    

    public static Pos lerpQuadratic(Pos a, Pos b, float pitch, int mul, int div) {
	Pos ret = interpolatePos(a, b, mul, div);
	
	if (pitch == 0) {
	    return ret;
	}
	
	float offset = (float) ((((((double)a.distanceTo(b) * mul) / div) * (div - mul)) / div) * Math.tan(pitch));
	
	ret.setZ(ret.getZ() + offset);
	
	return ret;
    }
    
    public static int distanceSq(Point p1, Point p2) {
	float dx = p1.getX() - p2.getX();
	float dy = p1.getY() - p2.getY();

	return (int) Math.ceil(dx * dx + dy * dy);
    }

    public static int rangeFromPdf(Random r, int samples)
    {
	final int CELL_SIZE = 24;
	
	int result = 0;
	for (int i = 0; i < samples; i++) {
	    // Get random number from -CELL_SIZE to CELL_SIZE
	    result += -CELL_SIZE + r.nextInt(2 * CELL_SIZE);
	}
	
	return result / samples;
    }    
}
