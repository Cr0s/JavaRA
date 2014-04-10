package cr0s.javara.render.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import cr0s.javara.resources.ResourceManager;

/**
 * Describes set of tiles and templates for current map renderer.
 * @author Cr0s
 */
public class TileSet {
    private HashMap<Integer, String> tiles;
    private String setName;
    
    public TileSet(final String aSetName) {
	this.setName = aSetName;
	this.tiles = new HashMap<>();
	
	InputStream input;
	try {
	    input = new FileInputStream(new File(ResourceManager.tilesetsFolder + aSetName + ".yaml"));

	    System.out.println("Loaded tileSet: " + ResourceManager.tilesetsFolder + aSetName + ".yaml");
	    Yaml tilesetYaml = new Yaml();
	    Map<String, Object> tilesetYamlMap = (Map) tilesetYaml.load(input);
	    for (String s : tilesetYamlMap.keySet()) {
		System.out.println("Loaded tileset segment: " + s);
	    }
	    
	    // Load Templates
	    Map<String, Object> templatesMap = (Map) tilesetYamlMap.get("Templates");
	    
	    for (Object v : templatesMap.values()) {
		Map<String, Object> template = (Map) v;
		
		Integer id = (Integer) template.get("Id");
		String image = (String) template.get("Image");
		
		System.out.println("Loaded template: " + id + " @ " + image);
		
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
