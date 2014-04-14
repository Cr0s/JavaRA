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
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.yaml.snakeyaml.Yaml;

import redhorizon.utilities.BufferUtility;
import cr0s.javara.main.Main;
import cr0s.javara.render.World;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.TmpTexture;

public class TileMap {
    private short width, height;

    private Theater theater;
    private TileSet tileSet;

    private TileReference[][] mapTiles;
    private LinkedList<MapEntity> mapEntities;

    private final int GRASS_ID = 0xFF; // 255
    private final int GRASS_ID_BIG = 0xFFFF; // 65635

    private World world;

    public TileMap(World aWorld, String mapName) {
	this.world = aWorld;

	InputStream input;
	try {
	    input = new FileInputStream(new File(ResourceManager.MAPS_FOLDER
		    + mapName + System.getProperty("file.separator")
		    + "map.yaml"));

	    Yaml mapYaml = new Yaml();
	    Map<String, Object> mapYamlMap = (Map) mapYaml.load(input);	    
	    
	    TileSet tileYamlSet = new TileSet(
		    (String) mapYamlMap.get("Tileset"));
	    this.tileSet = tileYamlSet;

	    input = new FileInputStream(new File(ResourceManager.RESOURCE_FOLDER + System.getProperty("file.separator")
		    + "trees.yaml"));

	    Yaml treesYaml = new Yaml();
	    Map<String, Object> treesYamlMap = (Map) mapYaml.load(input);	    
	    
	    this.mapEntities = new LinkedList<MapEntity>();
	    Map<String, Object> entitiesMap = (Map) mapYamlMap.get("Actors");
	    for (Object v : entitiesMap.values()) {
		Map<String, Object> actor = (Map) v;

		String id = (String) actor.get("Name");
		
		String footprint = ((Map<String, String>) (((Map<String, Object>) treesYamlMap.get(id.toUpperCase())).get("Building"))).get("Footprint");
		String dimensions = ((Map<String, String>) (((Map<String, Object>) treesYamlMap.get(id.toUpperCase())).get("Building"))).get("Dimensions");
		System.out.println("id: " + id + "(" + dimensions + "): " + footprint);
		int x = (Integer) actor.get("LocationX");
		int y = (Integer) actor.get("LocationY");

		ShpTexture st = ResourceManager.getInstance()
			.getTemplateShpTexture(this.tileSet.getSetName(),
				id + ".tem");

		if (st != null) {
		    MapEntity me = new MapEntity(x, y, st, footprint, dimensions);
		    this.mapEntities.add(me);
		}
	    }

	    this.theater = new Theater(this, this.tileSet);

	    // Read binary map
	    loadBinaryMap(mapName);

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    private void loadBinaryMap(String mapName) {
	try (RandomAccessFile randomAccessFile = new RandomAccessFile(Paths
		.get(ResourceManager.MAPS_FOLDER + mapName
			+ System.getProperty("file.separator") + "map.bin")
		.toString(), "r")) {
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
		    int tile = (int) (mapBytes.getShort() & 0xFFFF);

		    short index = (short) (mapBytes.get() & 0xFF);

		    // Randomize clear grass
		    if (tile == GRASS_ID || tile == GRASS_ID_BIG) {
			index = (short) r.nextInt(16);
		    }

		    this.mapTiles[x][y] = new TileReference<Integer, Byte>(
			    tile, (byte) index);
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

	Color blockedColor = new Color(255, 0, 0, 32);

	// Draw tiles layer
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		if (x < (int) -camera.offsetX / 24 - 1
			|| x > (int) -camera.offsetX / 24 + (int) c.getWidth()
				/ 24 + 1) {
		    continue;
		}

		if (y < (int) -camera.offsetY / 24 - 1
			|| y > (int) -camera.offsetY / 24 + (int) c.getHeight()
				/ 24 + 1) {
		    continue;
		}

		if ((int) this.mapTiles[x][y].getTile() != 0) {
		    Point sheetPoint = theater
			    .getTileTextureSheetCoord(this.mapTiles[x][y]);

		    int index = (int) ((byte) this.mapTiles[x][y].getIndex() & 0xFF);

		    int sX = (int) sheetPoint.getX();
		    int sY = (int) sheetPoint.getY();

		    if (sX != -1 && sY != -1) {
			this.theater.getSpriteSheet().renderInUse(x * 24, y * 24, sX / 24, (sY / 24) + index);
		    }
		}

		/*
		 * if (Main.DEBUG_MODE) { g.setColor(Color.red); g.drawRect(x *
		 * 24, y * 24, 24, 24); g.setColor(pColor);
		 */
	    }
	}

	/*
	 * theater.getSpriteSheet().draw(); for (Rectangle r :
	 * theater.texturesBounds) { g.setColor(Color.red); g.draw(r);
	 * g.setColor(pColor); }
	 */

	// Draw map entities
	for (MapEntity me : this.mapEntities) {
	    int x = me.getX();
	    int y = me.getY();

	    // Don't draw invisible entities
	    if (x < (int) -camera.offsetX / 24 - 2
		    || x > (int) -camera.offsetX / 24 + (int) c.getWidth() / 24
			    + 2) {
		continue;
	    }

	    if (y < (int) -camera.offsetY / 24 - 2
		    || y > (int) -camera.offsetY / 24 + (int) c.getHeight()
			    / 24 + 2) {
		continue;
	    }

	    ShpTexture t = me.getTexture();

	    Point sheetPoint = this.theater.getShpTexturePoint(t
		    .getTextureName());

	    int sX = (int) sheetPoint.getX();
	    int sY = (int) sheetPoint.getY();

	    this.theater.getSpriteSheet()
		    .getSubImage(sX, sY, t.width, t.height)
		    .drawEmbedded(x * 24, y * 24, t.width, t.height);
	}

	this.theater.getSpriteSheet().endUse();
    }

    public LinkedList<MapEntity> getMapEntities() {
	return this.mapEntities;
    }

    public void fillBlockingMap(int[][] blockingMap) {
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		int id = (int) ((int) this.mapTiles[x][y].getTile() & 0xFFFF);
		int index = (int) ((byte) this.mapTiles[x][y].getIndex() & 0xFF);

		Integer[] surfaces = this.theater.tilesSurfaces.get(id);
		
		if (surfaces != null && index >= surfaces.length) {
		    continue;
		}
		
		if (surfaces != null) {
		    blockingMap[x][y] = surfaces[index];
		} 
	    }
	}
	
	fillWithMapEntities(blockingMap);
    }
    
    private void fillWithMapEntities(int[][] blockingMap) {
	for (MapEntity me : this.mapEntities) {
	    for (int cX = 0; cX < me.getWidth(); cX++) {
		for (int cY = 0; cY < me.getHeight(); cY++) {
		    blockingMap[me.getX() + cX][me.getY() + cY] = me.getFootprintCells()[cX][cY];
		}
	    }
	}
    }
    
    public TileSet getTileSet() {
	return this.tileSet;
    }
}
