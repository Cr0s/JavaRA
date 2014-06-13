package cr0s.javara.render.map;

import java.util.HashMap;
import java.util.Map.Entry;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.Pos;

public class SmudgeLayer {
    private static final int SMOKE_PERCENTAGE = 25;
    private static final String SMOKE_SPRITE = "smoke_m";
    public HashMap<Pos, Smudge> craters = new HashMap<Pos, Smudge>();
    public HashMap<Pos, Smudge> scorches = new HashMap<Pos, Smudge>();

    private TileMap map;

    private final String CRATER_SPRITE = "cr";
    private final String SCORCH_SPRITE = "sc";
    
    private final int MAX_TYPE = 6;
    private final int MAX_DEPTH_CRATER = 5;
    private final int MAX_DEPTH_SCORCH = 1;
    
    public SmudgeLayer(TileMap aMap) {
	this.map = aMap;
    }

    public class Smudge {
	public String type;
	public int depth;

	public Smudge (String aType, int aDepth) {
	    this.type = aType;
	    this.depth = aDepth;
	}
    }

    public void render(Graphics g) {
	renderLayer(g, this.scorches);
	renderLayer(g, this.craters);
    }
    
    private void renderLayer(Graphics g, HashMap<Pos, Smudge> layer) {
	for (Entry<Pos, Smudge> v : layer.entrySet()) {
	    Pos pos = v.getKey();
	    Smudge smudge = v.getValue();

	    int x = (int) pos.getX();
	    int y = (int) pos.getY();

	    // Check viewport bounds
	    if (x < (int) -Main.getInstance().getCamera().offsetX / 24 - 1
		    || x > (int) -Main.getInstance().getCamera().offsetX / 24 + (int) Main.getInstance().getContainer().getWidth()
		    / 24 + 1) {
		continue;
	    }

	    if (y < (int) -Main.getInstance().getCamera().offsetY / 24 - 1
		    || y > (int) -Main.getInstance().getCamera().offsetY / 24 + (int) Main.getInstance().getContainer().getHeight()
		    / 24 + 1) {
		continue;
	    }

	    // Don't draw shrouded smudges
	    if (Main.getInstance().getPlayer().getShroud() != null && !Main.getInstance().getPlayer().getShroud().isExplored(pos)) {
		continue;
	    }

	    String textureName = smudge.type + "." + this.map.getTileSet().getSetName().toLowerCase().substring(0, 3);
	    Point sheetPoint = map.getTheater().getShpTexturePoint(textureName);

	    int sX = (int) sheetPoint.getX();
	    int sY = (int) sheetPoint.getY();

	    if (sX != -1 && sY != -1) {
		this.map.getTheater().getSpriteSheet().renderInUse(x * 24 - 12, y * 24 - 12, sX / 24, (sY / 24) + smudge.depth);
	    }		    
	}	
    }
    
    public void addSmudge(Pos pos, boolean crater) {
	if (Main.getInstance().getWorld().getRandomInt(0, 100) < SMOKE_PERCENTAGE) {
	    Main.getInstance().getWorld().spawnSmokeAt(new Pos(pos.getX() * 24 + 12, pos.getY() * 24 + 12), SMOKE_SPRITE);
	}
	
	Smudge s = crater ? this.craters.get(pos) : this.scorches.get(pos);
	
	// There is smudge, increase depth
	if (s != null) {
	    if (crater) { 
		if (s.depth + 1 != this.MAX_DEPTH_CRATER) {
		    s.depth++;
		}
	    } else {
		if (s.depth + 1 != this.MAX_DEPTH_SCORCH) {
		    s.depth++;
		}
	    }
	} else { // Create new smudge
	    String type = crater ? "cr" : "sc";
	    type += Main.getInstance().getWorld().getRandomInt(1, this.MAX_TYPE + 1);
	    
	    s = new Smudge(type, 0);
	    
	    (crater ? this.craters : this.scorches).put(pos, s);
	}
    }
}
