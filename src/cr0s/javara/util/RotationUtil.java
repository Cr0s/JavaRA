package cr0s.javara.util;

import org.newdawn.slick.geom.Point;

public class RotationUtil {
    public static int getRotationFromXY(float srcX, float srcY, float x, float y) {
	float dx = x - srcX;
	float dy = y - srcY;

	// Simply magic
	int rot = 270 + (int) Math.toDegrees(Math.atan2(-dy, dx));
	return (int) (rot / 11.25);
    }

    public static int quantizeFacings(int facing, int max) {
	int step = 32 / max;
	int a = (facing + step / 2);

	return a / step;	
    }

    public static Point facingToRecoilVector(int facing) {	
	int facingToDegrees = (int) Math.floor(facing * 11.25f);

	int resX = 0;
	int resY = 0;
	double facingRadians = 0;

	// Diagonal facings
	if (facing % 8 != 0) {
	    facingRadians = Math.toRadians(facingToDegrees - 90);
	    resX = (int)  Math.signum(Math.cos(facingRadians));
	    resY = (int) -Math.signum(Math.sin(facingRadians));	    
	} else { // Orthogonal facings
	    switch (facing) {
	    case 0:
		resX = 0;
		resY = 1;
		break;
		
	    case 8:
		resX = 1;
		resY = 0;
		break;
		
	    case 16:
		resX = 0;
		resY = -1;
		break;
		
	    case 24:
		resX = -1;
		resY = 0;
		break;
	    }
	}

	//System.out.println("Facing: " + facingToDegrees + " (" + facing + ")");	
	//System.out.println("\tx: " + resX + " | y: " + resY);

	return new Point(resX, resY);
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
