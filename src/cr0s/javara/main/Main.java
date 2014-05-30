package cr0s.javara.main;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.newdawn.slick.Animation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.Log;

import soundly.Soundly;
import soundly.XSound;
import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.building.common.EntityPowerPlant;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.tank.EntityHeavyTank;
import cr0s.javara.gameplay.BuildingOverlay;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.render.Controller;
import cr0s.javara.render.World;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.shrouds.Shroud;
import cr0s.javara.render.shrouds.ShroudRenderer;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.GameSideBar;

public class Main extends StateBasedGame {
    public ResourceManager rm;

    private StateMainMenu menu;
    private StateGameMap gameMap;

    private static Main instance;

    private World w;
    private Camera camera;
    private Controller controller;

    private Team team;
    private Player player;

    private BuildingOverlay bo;

    public static boolean DEBUG_MODE = false;

    private CursorType currentCursor = CursorType.CURSOR_POINTER;

    private GameSideBar gsb;
    private ShroudRenderer observerShroudRenderer;
    
    public Main() {
	super("Java RA");
	SoundStore.get().init();
    }

    public static Main getInstance() {
	if (instance == null) {
	    instance = new Main();
	}

	return instance;
    }

    /**
     * Entry point.
     * 
     * @param argv
     *            The argument passed on the command line (if any)
     */
    public static void main(String[] argv) {
	try {
	    AppGameContainer container = new AppGameContainer(Main.getInstance(), 800,
		    600, false);

	    container.setMinimumLogicUpdateInterval(50);
	    container.setMaximumLogicUpdateInterval(50);
	    //container.setShowFPS(false);
	    //container.setSmoothDeltas(true);
	    //container.setVSync(true);
	    container.setTargetFrameRate(75);
	    container.setClearEachFrame(false);
	    container.start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void loadCursors() {

    }

    @Override
    public void initStatesList(GameContainer arg0) throws SlickException {
	this.addState(new StateMainMenu());		
	this.addState(new StateGameMap(arg0));
	this.addState(new StatePauseMenu());

	//this.getContainer().getInput().addMouseListener(this.getState(0));
	//this.getContainer().getInput().addMouseListener(this.getState(1));		
    }

    public Camera getCamera() {
	return camera;
    }

    public Controller getController() {
	return controller;
    }

    public World getWorld() {
	return w;
    }

    public void setWorld(World w) {
	this.w = w;
    }

    public void startNewGame(String mapName) {	
	rm = ResourceManager.getInstance();
	rm.loadBibs();

	camera = new Camera();
	try {
	    camera.init(this.getContainer());
	} catch (SlickException e1) {
	    e1.printStackTrace();
	}

	controller = new Controller(null, camera, this.getContainer().getInput());	
	w = new World("haos-ridges",
		this.getContainer(), camera);		

	initGame();
    }

    public void initGame() {
	Random r = new Random(System.currentTimeMillis());

	this.observerShroudRenderer = new ShroudRenderer(w);
	
	team = new Team();
	player = new Player(w, "Player", Alignment.SOVIET, new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
	player.setTeam(team);
	
	bo = new BuildingOverlay(player, w);

	w.addPlayer(player);

	Point playerSpawn = player.getPlayerSpawnPoint();	

	this.getCamera().setOffset(-Math.max(w.getMap().getBounds().getMinX(), (playerSpawn.getX() * 24) - this.getContainer().getWidth() / 2), -Math.max(w.getMap().getBounds().getMinY(), (playerSpawn.getY() * 24)));

	this.gsb = new GameSideBar(Main.getInstance().getTeam(), Main.getInstance().getPlayer());
	this.gsb.initSidebarPages();
    }


    public Player getPlayer() {
	return this.player;
    }

    public Team getTeam() {
	return this.player.getTeam();
    }

    public CursorType getCursor() {
	return this.currentCursor;
    }

    public void setCursorType(CursorType cursor) {
	if (this.currentCursor == cursor) {
	    return;
	}
	
	try {
	    switch (cursor) {
	    case CURSOR_POINTER:
		this.getContainer().setMouseCursor(ResourceManager.getInstance().pointerCursor, 0, 0);
		break;

	    case CURSOR_SELECT:
		getContainer().setAnimatedMouseCursor(ResourceManager.SELECT_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50, 50, 50});
		break;

	    case CURSOR_GOTO:
		getContainer().setAnimatedMouseCursor(ResourceManager.GOTO_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50});
		break;

	    case CURSOR_NO_GOTO:
		this.getContainer().setMouseCursor(ResourceManager.NO_GOTO_CURSOR, 16, 16);
		break;

	    case CURSOR_DEPLOY:
		getContainer().setAnimatedMouseCursor(ResourceManager.DEPLOY_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50 });
		break;

	    case CURSOR_NO_DEPLOY:
		this.getContainer().setMouseCursor(ResourceManager.NO_DEPLOY_CURSOR, 16, 16);
		break;

	    case CURSOR_ENTER:
		getContainer().setAnimatedMouseCursor(ResourceManager.ENTER_CURSOR, 16, 16, 32, 32, new int[] { 80, 80, 80 });
		break;
		
	    case CURSOR_NO_ENTER:
		this.getContainer().setMouseCursor(ResourceManager.NO_ENTER_CURSOR, 16, 16);
		break;
		
	    case CURSOR_ATTACK:
		getContainer().setAnimatedMouseCursor(ResourceManager.ATTACK_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50, 50, 50, 50, 50 });
		break;
		
	    default:
		break;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	this.currentCursor = cursor;
    }

    public GameSideBar getSideBar() {
	return this.gsb;
    }

    public ShroudRenderer getObserverShroudRenderer() {
	return this.observerShroudRenderer;
    }

    public BuildingOverlay getBuildingOverlay() {
	return this.bo;
    }
}
