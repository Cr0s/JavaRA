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
import cr0s.javara.util.Point;

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
		w = new World("forest-path",
				this.getContainer(), camera);		
		
		initGame();
	}
	
	public void initGame() {
		Random r = new Random(System.currentTimeMillis());
		
		team = new Team();
		player = new Player("anus", Alignment.SOVIET, Color.red);//new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));


		// Create testing base
		EntityPowerPlant e1 = new EntityPowerPlant(24 * 15, 24 * 18, team, player);
		e1.setHp(r.nextInt(e1.getMaxHp()));
		
		EntityPowerPlant e2 = new EntityPowerPlant(24 * 12, 24 * 18, team, player);
		e2.setHp(r.nextInt(e2.getMaxHp()));

		EntityBarracks b1 = new EntityBarracks(24 * 14, 24 * 23, team, player);
		b1.setHp(r.nextInt(b1.getMaxHp()));		

		EntityProc p1 = new EntityProc(24 * 8, 24 * 22, team, player);
		p1.setHp(r.nextInt(p1.getMaxHp()));		
		
		w.addBuildingTo(p1);
		w.addBuildingTo(b1);
		w.addBuildingTo(e1);
		w.addBuildingTo(e2);
		
		EntityMcv mcv = new EntityMcv(24 * 20, 28 * 20, team, player);
		mcv.isVisible = true;
		
		EntityHarvester harv = new EntityHarvester(24 * 9, 24 * 24, team, player);
		harv.isVisible = true;
		
		w.spawnEntityInWorld(harv);
		w.spawnEntityInWorld(mcv);
		
	}
	
	public void resetCursor() {
	    try {
		this.getContainer().setMouseCursor(ResourceManager.getInstance().pointerCursor, 32, 0);
	    } catch (SlickException e) {
		e.printStackTrace();
	    }
	}
	
	public void setGotoCursor() {
	    try {
		getContainer().setAnimatedMouseCursor(ResourceManager.GOTO_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50});
	    } catch (SlickException e) {
		e.printStackTrace();
	    }	    
	}
	
	public void setSelectCursor() {
	    try {
		getContainer().setAnimatedMouseCursor(ResourceManager.SELECT_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50, 50, 50});
	    } catch (SlickException e) {
		e.printStackTrace();
	    }	    
	}
	
	public void setDeployCursor() {
	    try {
		getContainer().setAnimatedMouseCursor(ResourceManager.DEPLOY_CURSOR, 16, 16, 32, 32, new int[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50 });
	    } catch (SlickException e) {
		e.printStackTrace();
	    }	    
	}	
	
	public Player getPlayer() {
	    return this.player;
	}
	
	public Team getTeam() {
	    return this.team;
	}
}
