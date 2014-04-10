package cr0s.javara.render.map;

public class TileReference<U, V> {
	
    	private U tile;
    	private V index;
	
	public TileReference(final U aTile, final V aIndex) {
	    this.tile = aTile;
	    this.index = aIndex;
	}
	
	public U getTile() {
	    return this.tile;
	}
	
	public V getIndex() {
	    return this.index;
	}
}
