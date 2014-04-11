package cr0s.javara.render.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;
import org.yaml.snakeyaml.Yaml;

import redhorizon.utilities.BufferUtility;
import cr0s.javara.main.Main;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;

public class TileMap {
    private short width, height;

    private Theater theater;
    private TileSet tileSet;

    private TileReference[][] mapTiles;
    
    private final int GRASS_ID = 0xFF; // 255
    
    public TileMap(String mapName) {
	InputStream input;
	try {
	    input = new FileInputStream(new File(ResourceManager.mapsFolder + mapName + System.getProperty("file.separator") + "map.yaml"));

	    Yaml mapYaml = new Yaml();
	    Map<String, String> mapYamlMap = (Map) mapYaml.load(input);


	    TileSet tileYamlSet = new TileSet(mapYamlMap.get("Tileset"));
	    this.tileSet = tileYamlSet;
	 
	    this.theater = new Theater(this.tileSet);
	    
	    // Read binary map
	    loadBinaryMap(mapName);
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    private void loadBinaryMap(String mapName) {	
	try (RandomAccessFile randomAccessFile = new RandomAccessFile(Paths
		.get(ResourceManager.mapsFolder + mapName + System.getProperty("file.separator") + "map.bin").toString(), "r")) {
	    FileChannel inChannel = randomAccessFile.getChannel();

	    // Read one byte and pair of two shorts: map height and width
	    ByteBuffer mapHeader = ByteBuffer.allocate(5);
	    mapHeader.order(ByteOrder.LITTLE_ENDIAN);
	    inChannel.read(mapHeader);
	    mapHeader.rewind();
	    
	    if (mapHeader.get() != 1) {
		System.err.println("Invalid map.");
		return;
	    }
	    
	    this.width = mapHeader.getShort();
	    this.height = mapHeader.getShort();
	    
	    this.mapTiles = new TileReference[width][height];
	    
	    System.out.println("Map size: " + this.width + " x " + this.height);
	    
	    // Height, Width and sizeof(short) + sizeof(byte)
	    ByteBuffer mapBytes = BufferUtility.readRemaining(inChannel);
	    mapBytes.order(ByteOrder.LITTLE_ENDIAN);
	    
	    Random r = new Random();
	    for (int x = 0; x < this.width; x++) {
		for (int y = 0; y < this.height; y++) {
			short tile = mapBytes.getShort();
			
			short index = (short) (mapBytes.get() & 0xFF);
			
			// Randomize clear grass
			if (tile == GRASS_ID) {
			    index = (short) r.nextInt(16);
			}
			
			this.mapTiles[x][y] = new TileReference<Short, Byte>(tile, (byte) index);
		}
	    }
	    
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	}	
    }
    
    public short getWidth() {
	return width;
    }

    public short getHeight() {
	return height;
    }
    
    public void render(GameContainer c, Graphics g, Camera camera) {	
	Color pColor = g.getColor();
	
	this.theater.getSpriteSheet().startUse();

	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		if (x < (int) -camera.offsetX / 24 - 1 || x > (int) -camera.offsetX / 24 + (int) c.getWidth() / 24 + 1) {
		    continue;
		}
		
		if (y < (int) -camera.offsetY / 24  -1 || y > (int) -camera.offsetY / 24 + (int) c.getHeight() / 24 + 1) {
		    continue;
		}		
		
		if ((short) this.mapTiles[x][y].getTile() != 0) {
		    Point sheetPoint = theater.getTileTextureSheetCoord(this.mapTiles[x][y]);

		    int index = (int) ((byte) this.mapTiles[x][y].getIndex() & 0xFF);
		    int sX = (int) sheetPoint.getX();
		    int sY = (int) sheetPoint.getY();

		    this.theater.getSpriteSheet().renderInUse(x * 24, y * 24, sX / 24, (sY / 24) + index);
		}
		
		/*if (Main.DEBUG_MODE) {
		    g.setColor(Color.red);
		    g.drawRect(x * 24, y * 24, 24, 24);
		    g.setColor(pColor);
		}*/
	    }
	}
	
	this.theater.getSpriteSheet().endUse();			
    }
}
