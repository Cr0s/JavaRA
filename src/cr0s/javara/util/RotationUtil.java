package cr0s.javara.util;

public class RotationUtil {
    public static int getRotationFromXY(float srcX, float srcY, float x, float y) {
	float dx = x - srcX;
	float dy = y - srcY;
	
	// Simply magic
	int rot = 180 + ((int) (Math.atan2(-dy, dx) * (180 / Math.PI)) + 90);
	return (int) rot / 11;
    }
    
    public static int quantizeFacings(int facing, int max) {
	int step = 32 / max;
	int a = (facing + step / 2);
	
	return a / step;	
    }
    
    /*public static int getFacingForInfantryFromDir(Point start, Point end) {
	Point vec = new Point(end.getX() - start.getX(), end.getY() - start.getY());
	
	if (vec.getX() == 1 && vec.getY() == 0) {
	    return 6; // right
	} else if (vec.getX() == -1 && vec.getY() == 0) {
	    return 2; // left
	} else if (vec.getX() == 0 && vec.getY() == 1) {
	    return 7; // down
	} else if (vec.getX() == 0 && vec.getY() == -1) {
	    return 0;
	} else if (vec.getX() == -1 && vec.getY() == -1) {
	    return 1; // top left diag
	} else if (vec.getX() == 1 && vec.getY() == -1) {
	    return 6; // top right diag
	}
    }*/
}
