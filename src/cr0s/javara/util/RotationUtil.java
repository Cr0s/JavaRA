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

    public static Pos facingToRecoilVector(int facing) {	
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

	return new Pos(resX, resY);
    }
}
