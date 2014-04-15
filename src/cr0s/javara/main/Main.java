package cr0s.javara.main;
import java.util.Random;

import org.newdawn.slick.Animation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.Log;

import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.building.EntityPowerPlant;
import cr0s.javara.entity.building.EntityProc;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.render.Controller;
import cr0s.javara.render.World;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.util.Point;
import cr0s.javara.vehicle.tank.EntityHeavyTank;

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
	
	public static boolean DEBUG_MODE = false;
	
	private CursorType currentCursor = CursorType.CURSOR_POINTER;
	
	private GameSideBar gsb;
	
	public Main() {
		super("Java RA");
	}

	public static Main getInstance() {
		if (instance == null) {
			instance = new Main();
		}
		
		return instance;
	}

	/**
	 * Entry point to the scroller example
	 * 
	 * @param argv
	 *            The argument passed on the command line (if any)
	 */
	public static void main(String[] argv) {
		try {
			AppGameContainer container = new AppGameContainer(Main.getInstance(), 800,
					600, false);
			
			container.setMinimumLogicUpdateInterval(20);
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
		
		team = new Team();
		player = new Player("anus", Alignment.SOVIET, Color.red);//new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));

		this.gsb = new GameSideBar(Main.getInstance().getTeam(), Main.getInstance().getPlayer());
		
		// Create testing base
		EntityPowerPlant e1 = new EntityPowerPlant(24 * 49, 24 * 53, team, player);
		e1.setHp(r.nextInt(e1.getMaxHp()));
		
		EntityPowerPlant e2 = new EntityPowerPlant(24 * 46, 24 * 53, team, player);
		e2.setHp(r.nextInt(e2.getMaxHp()));

		EntityBarracks b1 = new EntityBarracks(24 * 48, 24 * 57, team, player);
		b1.setHp(r.nextInt(b1.getMaxHp()));		

		EntityProc p1 = new EntityProc(24 * 42, 24 * 56, team, player);
		p1.setHp(r.nextInt(p1.getMaxHp()));		
		
		w.addBuildingTo(p1);
		w.addBuildingTo(b1);
		w.addBuildingTo(e1);
		w.addBuildingTo(e2);
		
		EntityMcv mcv = new EntityMcv(24 * 54, 28 * 50, team, player);
		mcv.isVisible = true;
		
		EntityHarvester harv = new EntityHarvester(24 * 43, 24 * 58, team, player);
		harv.isVisible = true;
		
		EntityHeavyTank eht = new EntityHeavyTank(24 * 58, 28 * 50, team, player);
		w.spawnEntityInWorld(eht);
		EntityHeavyTank eht1 = new EntityHeavyTank(24 * 62, 28 * 50, team, player);
		w.spawnEntityInWorld(eht1);
		EntityHeavyTank eht3 = new EntityHeavyTank(24 * 66, 28 * 50, team, player);
		w.spawnEntityInWorld(eht3);
		
		w.spawnEntityInWorld(harv);
		w.spawnEntityInWorld(mcv);
	}
	
	
	public Player getPlayer() {
	    return this.player;
	}
	
	public Team getTeam() {
	    return this.team;
	}
	
	public CursorType getCursor() {
	    return this.currentCursor;
	}
	
	public void setCursorType(CursorType cursor) {
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
}
