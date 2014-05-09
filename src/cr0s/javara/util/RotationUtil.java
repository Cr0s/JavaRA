package cr0s.javara.util;

public class RotationUtil {
    public static int getRotationFromXY(float srcX, float srcY, float x, float y) {
	float dx = x - srcX;
	float dy = y - srcY;
	
	// Simply magic
	int rot = 180 + ((int) (Math.atan2(-dy, dx) * (180 / Math.PI)) + 90);
	return (int) rot / 11;
    }
    
    public static int quantizeHarvestingFacing(int facing) {
	int step = 32 / 8; // harvester texture has 8 frames for each harvesting facings
	int a = (facing + step / 2);
	
	return a / step;	
    }
}
