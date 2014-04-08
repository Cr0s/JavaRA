package cr0s.javara.render.map;

public class TileReference {
	
	/*
	 * Reference format
	 * bytebyte|byte
	 * ushort   byte
	 */
	
	public static int getType(int tileRef) {
		return (int)((tileRef >> 8) & 0xFFFF);
	}
	
	public static int getIndex(int tileRef) {
		return (int)(tileRef & 0xFF);
	}
}
