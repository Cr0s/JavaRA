package cr0s.javara.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import redhorizon.filetypes.mix.MixFile;
import redhorizon.filetypes.mix.MixRecord;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.filetypes.shp.ShpFile;
import redhorizon.filetypes.shp.ShpFileCnc;
import redhorizon.filetypes.tmp.TmpFileRA;

public class ResourceManager {

    private static ResourceManager instance;
    public static final String rootFolder = System.getProperty("user.dir")
	    + System.getProperty("file.separator");
    
    public static final String resourceFolder = rootFolder + "assets"
	    + System.getProperty("file.separator");
    public static final String palFolder = resourceFolder + "pal"
	    + System.getProperty("file.separator");
    public static final String tilesetsFolder = rootFolder + "tilesets"
	    + System.getProperty("file.separator");
    
    public static final String mapsFolder = rootFolder + "maps"
	    + System.getProperty("file.separator");

    private HashMap<String, MixFile> mixes = new HashMap<>();
    private HashMap<String, ShpTexture> conquerTextureSources = new HashMap<>();
    private HashMap<String, TmpTexture> templatesTexureSources = new HashMap<>();
    private HashMap<String, PalFile> palettes = new HashMap<>();

    private ResourceManager() {
	loadMixes();
    }

    public static ResourceManager getInstance() {
	if (instance == null) {
	    instance = new ResourceManager();
	}

	return instance;
    }

    private void loadMixes() {
	RandomAccessFile randomAccessFile = null;

	try {
	    List<Path> mixFiles = listDirectoryMixes(Paths.get(resourceFolder));

	    for (Path f : mixFiles) {
		randomAccessFile = new RandomAccessFile(f.toString(), "r");
		FileChannel inChannel = randomAccessFile.getChannel();

		MixFile mix = new MixFile(f.getFileName().toString(), inChannel);

		mixes.put(mix.getFileName(), mix);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	}
    }

    public ShpTexture getConquerTexture(String name) {
	MixFile mix = mixes.get("conquer.mix");

	// Check texture sources cache
	if (conquerTextureSources.containsKey(name)) {
	    return conquerTextureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		ShpFileCnc shp = new ShpFileCnc(name, rbc);
		ShpTexture shpTexture = new ShpTexture(shp);
		conquerTextureSources.put(name, shpTexture);
		return shpTexture;
	    } else {
		return null;
	    }
	}

	return null;
    }
    
    public TmpTexture getTemplateTexture(String type, String name) {
	type = type.toLowerCase();
	MixFile mix = mixes.get(type + ".mix");

	// Check texture sources cache
	if (templatesTexureSources.containsKey(name)) {
	    return templatesTexureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		TmpFileRA tmp = new TmpFileRA(name, rbc);
		TmpTexture tmpTexture = new TmpTexture(tmp, type);
		
		templatesTexureSources.put(name, tmpTexture);
		return tmpTexture;
	    } else {
		System.out.println("Record (" + name +") in " + type + ".mix is not found");
		return null;
	    }
	}

	System.out.println(type + ".mix is not found");
	return null;
    }    

    public PalFile getPaletteByName(String name) {
	if (palettes.containsKey(name)) {
	    return palettes.get(name);
	}

	try (RandomAccessFile randomAccessFile = new RandomAccessFile(Paths
		.get(palFolder + name).toString(), "r")) {
	    FileChannel inChannel = randomAccessFile.getChannel();
	    PalFile palfile = new PalFile(name, inChannel);

	    palettes.put(name, palfile);

	    return palfile;
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	}

	return null;
    }

    List<Path> listDirectoryMixes(Path resourceFolder) throws IOException {
	List<Path> result = new ArrayList<>();
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(
		resourceFolder, "*.{mix}")) {
	    for (Path entry : stream) {
		result.add(entry);
	    }
	} catch (DirectoryIteratorException ex) {
	    throw ex.getCause();
	}
	return result;
    }
}
