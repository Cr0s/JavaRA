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

import cr0s.javara.entity.building.EntityConstructionYard;
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
			//container.setTargetFrameRate(60);
			container.setClearEachFrame(false);
			container.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		Team t = new Team();
		
		for (int x = 0; x < 15; x++) {
			EntityConstructionYard e = new EntityConstructionYard(r.nextInt(50) * 24, r.nextInt(50) * 24, t, new Player("anus", Alignment.SOVIET, new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256))));
			
			if (r.nextBoolean()) {
				e.setHp(1);
			}
			
			e.isVisible = true;
			w.spawnEntityInWorld(e);
		}
		
		EntityMcv mcv = new EntityMcv(40 * 24, 40 * 24, t, new Player("anus", Alignment.SOVIET, new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256))));
		mcv.isVisible = true;
		
		w.spawnEntityInWorld(mcv);
		
	}
}
