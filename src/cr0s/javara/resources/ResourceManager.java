package cr0s.javara.resources;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.util.WaveData;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.opengl.CursorLoader;
import org.newdawn.slick.opengl.Texture;

import cr0s.javara.entity.building.BibType;
import cr0s.javara.gameplay.Team.Alignment;
import redhorizon.filetypes.aud.AudFile;
import redhorizon.filetypes.mix.MixFile;
import redhorizon.filetypes.mix.MixRecord;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.filetypes.shp.ShpFile;
import redhorizon.filetypes.shp.ShpFileCnc;
import redhorizon.filetypes.tmp.TmpFileRA;
import soundly.XSound;

public class ResourceManager {

    private static ResourceManager instance;
    public static final String ROOT_FOLDER = System.getProperty("user.dir")
	    + System.getProperty("file.separator");

    public static final String RESOURCE_FOLDER = ROOT_FOLDER + "assets"
	    + System.getProperty("file.separator");
    public static final String PAL_FOLDER = RESOURCE_FOLDER + "pal"
	    + System.getProperty("file.separator");
    public static final String TILESETS_FOLDER = ROOT_FOLDER + "tilesets"
	    + System.getProperty("file.separator");

    public static final String MAPS_FOLDER = ROOT_FOLDER + "maps"
	    + System.getProperty("file.separator");

    public static final String SIDEBAR_CATEGORIES_SHEET = RESOURCE_FOLDER + "sidebar_buttons.png";

    public static Cursor pointerCursor;
    private HashMap<String, MixFile> mixes = new HashMap<>();
    private HashMap<String, ShpTexture> commonTextureSources = new HashMap<>();
    private HashMap<String, ShpTexture> shpTextureSources = new HashMap<>();
    private HashMap<String, TmpTexture> templatesTexureSources = new HashMap<>();
    private HashMap<String, PalFile> palettes = new HashMap<>();
    private HashMap<String, XSound> sounds = new HashMap<>();

    private SpriteSheet bib1, bib2, bib3;

    private ResourceManager() {
	loadMixes();
    }

    public static ResourceManager getInstance() {
	if (instance == null) {
	    instance = new ResourceManager();
	}

	return instance;
    }

    public void loadBibs() {
	bib1 = new SpriteSheet(getTemplateShpTexture("temperat", "bib1.tem").getAsCombinedImage(null), 24, 24);
	bib2 = new SpriteSheet(getTemplateShpTexture("temperat", "bib2.tem").getAsCombinedImage(null), 24, 24);
	bib3 = new SpriteSheet(getTemplateShpTexture("temperat", "bib3.tem").getAsCombinedImage(null), 24, 24);
    }

    public SpriteSheet getBibSheet(BibType bt) {
	switch (bt) {
	case SMALL:
	    return bib3;
	case MIDDLE:
	    return bib2;

	case BIG:
	    return bib1;

	default:
	    return null;
	}
    }

    private void loadMixes() {
	RandomAccessFile randomAccessFile = null;

	try {
	    List<Path> mixFiles = listDirectoryMixes(Paths.get(RESOURCE_FOLDER));

	    for (Path f : mixFiles) {
		randomAccessFile = new RandomAccessFile(f.toString().toLowerCase(), "r");
		FileChannel inChannel = randomAccessFile.getChannel();

		MixFile mix = new MixFile(f.getFileName().toString(), inChannel);

		mixes.put(mix.getFileName(), mix);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	}
    }

    public ShpTexture getSidebarTexture(String name) {
	MixFile mix = mixes.get("interface.mix");

	// Check texture sources cache
	if (commonTextureSources.containsKey(name)) {
	    return commonTextureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		ShpFileCnc shp = new ShpFileCnc(name, rbc);
		ShpTexture shpTexture = new ShpTexture(shp);
		commonTextureSources.put(name, shpTexture);
		return shpTexture;
	    } else {
		return null;
	    }
	}

	return null;
    }    

    public ShpTexture getConquerTexture(String name) {
	MixFile mix = mixes.get("conquer.mix");

	// Check texture sources cache
	if (commonTextureSources.containsKey(name)) {
	    return commonTextureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		ShpFileCnc shp = new ShpFileCnc(name, rbc);
		ShpTexture shpTexture = new ShpTexture(shp);
		shpTexture.palleteName = "temperat.pal";
		commonTextureSources.put(name, shpTexture);
		return shpTexture;
	    } else {
		return null;
	    }
	}

	return null;
    }

    public XSound loadSpeechSound(String name) {
	return loadSound("speech.mix", name + ".aud");
    }

    public XSound loadSound(String mixname, String name) {
	MixFile mix = mixes.get(mixname);

	// Check texture sources cache
	if (this.sounds.containsKey(name)) {
	    return sounds.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);
		AudFile aud = new AudFile(name, rbc);

		XSound sound = null;
		try {
		    sound = new XSound(name, new BufferedInputStream(Channels.newInputStream(aud.getSoundData())));
		} catch (SlickException e) {
		    e.printStackTrace();
		}
		//aud.close();

		if (sound != null) {
		    this.sounds.put(name, sound);
		}

		return sound;
	    } else {
		return null;
	    }
	}

	return null;	
    }

    public ShpTexture getTemplateShpTexture(String tileSetName, String name) {
	MixFile mix = mixes.get(tileSetName.toLowerCase() + ".mix");

	// Check texture sources cache
	if (shpTextureSources.containsKey(name)) {
	    return shpTextureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		ShpFileCnc shp = new ShpFileCnc(name, rbc);
		ShpTexture shpTexture = new ShpTexture(shp);
		shpTexture.palleteName = tileSetName + ".pal";
		shpTextureSources.put(name, shpTexture);

		return shpTexture;
	    } else {
		System.out.println("Record SHP (" + name +") in " + tileSetName + ".mix is not found");
		return null;
	    }
	} else {
	    System.out.println("Mix file " + tileSetName + ".mix is not found");    
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
		.get(PAL_FOLDER + name).toString().toLowerCase(), "r")) {
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

    public XSound loadUnitSound(Alignment alignment, String name) {
	String mixname = "allies.mix";
	if (alignment == Alignment.SOVIET) {
	    mixname = "russian.mix";
	}

	return loadSound(mixname, name);
    }

    public ShpTexture getInfantryTexture(String name) {
	MixFile mix = mixes.get("hires.mix");

	// Check texture sources cache
	if (commonTextureSources.containsKey(name)) {
	    return commonTextureSources.get(name);
	}

	if (mix != null) {
	    MixRecord rec = mix.getEntry(name);

	    if (rec != null) {
		ReadableByteChannel rbc = mix.getEntryData(rec);

		ShpFileCnc shp = new ShpFileCnc(name, rbc);
		ShpTexture shpTexture = new ShpTexture(shp);
		commonTextureSources.put(name, shpTexture);
		return shpTexture;
	    } else {
		System.out.println("HIRES: " + name + " not found");
		return null;
	    }
	}

	return null;
    }

    public ShpTexture getShpTexture(String name) {
	// Check texture sources cache
	if (commonTextureSources.containsKey(name)) {
	    return commonTextureSources.get(name);
	}

	RandomAccessFile randomAccessFile = null;
	
	try {
	    randomAccessFile = new RandomAccessFile(Paths
			.get(RESOURCE_FOLDER + name).toString().toLowerCase(), "r");
	    
	    FileChannel inChannel = randomAccessFile.getChannel();
	    
	    ShpFileCnc shp = new ShpFileCnc(name, inChannel);
	    ShpTexture shpTexture = new ShpTexture(shp);
	    shpTexture.palleteName = "temperat.pal";
	    
	    commonTextureSources.put(name, shpTexture);
	    return shpTexture;
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	} finally {
	    if (randomAccessFile != null) {
		try {
		    randomAccessFile.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
}
