package cr0s.javara.render.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.newdawn.slick.Color;
import org.yaml.snakeyaml.Yaml;

import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.TmpTexture;

/**
 * Describes set of tiles and templates for current map renderer.
 * @author Cr0s
 */
public class TileSet {

    public static HashMap<String, Integer> renameMap = new HashMap<>();

    public static final int SURFACE_CLEAR_ID = 3;
    public static final int SURFACE_BEACH_ID = 6;
    public static final int SURFACE_ROCK_ID = 8;
    public static final int SURFACE_ROAD_ID = 9;
    public static final int SURFACE_WATER_ID = 10;
    public static final int SURFACE_RIVER_ID = 11;
    public static final int SURFACE_ROUGH_ID = 14;
    public static final int SURFACE_BUILDING_CLEAR_ID = 15;
    public static final int SURFACE_BUILDING = 2;
    public static final int SURFACE_ORE_GOLD = 16;
    public static final int SURFACE_ORE_GEM = 17;
    
    static {
	renameMap.put("Clear", SURFACE_CLEAR_ID);
	renameMap.put("Beach", SURFACE_BEACH_ID);
	renameMap.put("Rock", SURFACE_ROCK_ID);
	renameMap.put("Road", SURFACE_ROAD_ID);
	renameMap.put("Water", SURFACE_WATER_ID);
	renameMap.put("River", SURFACE_RIVER_ID);
	renameMap.put("Rough", SURFACE_ROUGH_ID);
	renameMap.put("Ore", SURFACE_ORE_GOLD);
	renameMap.put("Gems", SURFACE_ORE_GEM);
    }

    private HashMap<Integer, String> tiles;
    public HashMap<Integer, HashMap<Integer, String>> tilesSurfaces;

    public HashMap<Integer, Color> terrainColors;
    
    private String setName;

    public TileSet(final String aSetName) {
	this.setName = aSetName;
	this.tiles = new HashMap<>();
	this.tilesSurfaces = new HashMap<>();

	InputStream input;
	try {
	    input = new FileInputStream(new File(ResourceManager.TILESETS_FOLDER + (aSetName + ".yaml").toLowerCase()));

	    System.out.println("Loaded tileSet: " + ResourceManager.TILESETS_FOLDER + (aSetName + ".yaml").toLowerCase());
	    Yaml tilesetYaml = new Yaml();
	    Map<String, Object> tilesetYamlMap = (Map) tilesetYaml.load(input);
	    //for (String s : tilesetYamlMap.keySet()) {
		//System.out.println("Loaded tileset segment: " + s);
	    //}

	    // Load terrain colors
	    Map<String, Object> terrainMap = (Map) tilesetYamlMap.get("Terrain");
	    this.terrainColors = new HashMap<>();
	    
	    for (Object v : terrainMap.values()) {
		Map<String, Object> tt = (Map) v;
		
		Integer typeId = this.renameMap.get((String) tt.get("Type"));
		int r = (int) tt.get("ColorR");
		int g = (int) tt.get("ColorG");
		int b = (int) tt.get("ColorB");
		
		this.terrainColors.put(typeId, new Color(r, g, b, 255));
	    }
	    
	    // Load Templates
	    Map<String, Object> templatesMap = (Map) tilesetYamlMap.get("Templates");

	    for (Object v : templatesMap.values()) {
		Map<String, Object> template = (Map) v;

		Integer id = (Integer) template.get("Id");
		String image = (String) template.get("Image");

		String size = (String) template.get("Size");

		// size = width,height
		int width = Integer.parseInt(size.substring(0, size.indexOf(",")));
		int height = Integer.parseInt(size.substring(size.indexOf(",") + 1, size.length()));


		Map<Integer, String> surfaceMap = (Map<Integer, String>) template.get("Tiles");

		HashMap<Integer, String> tiles = new HashMap<>();
		for (Object index : surfaceMap.keySet()) {
		    tiles.put((int) index, surfaceMap.get(index));
		}

		this.tilesSurfaces.put(id, tiles);
		//System.out.println("Loaded template: " + id + " @ " + image);

		this.tiles.put(id, image);
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    public String getSetName() {
	return this.setName;
    }

    public HashMap<Integer, String> getTiles() {
	return this.tiles;
    }
}
