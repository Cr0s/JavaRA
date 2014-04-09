package cr0s.javara.main;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.tiled.TiledMap;

import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.render.Controller;
import cr0s.javara.render.World;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.Point;

public class StateGameMap extends BasicGameState {
	public static final int STATE_ID = 1;

	private GameContainer container;
	
	public StateGameMap(GameContainer container) {
		this.container = container;
	}
	
	@Override
	public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		Main.getInstance().getController().mouseClicked(button, x, y, clickCount);
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		Main.getInstance().getController().mousePressed(button, x, y);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		Main.getInstance().getController().mouseReleased(button, x, y);
	}

	@Override
	public void mouseWheelMoved(int arg0) {
		
	}

	@Override
	public void inputEnded() {
		
	}

	@Override
	public void inputStarted() {
		
	}

	@Override
	public boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void setInput(Input arg0) {
		
	}

	@Override
	public void keyPressed(int arg0, char arg1) {
		if (this.container.getInput().isKeyDown(Input.KEY_ESCAPE)) {
			Main.getInstance().enterState(StatePauseMenu.STATE_ID);
		}
	}

	@Override
	public void keyReleased(int arg0, char arg1) {

	}

	@Override
	public void controllerButtonPressed(int arg0, int arg1) {
		
	}

	@Override
	public void controllerButtonReleased(int arg0, int arg1) {
		
	}

	@Override
	public void controllerDownPressed(int arg0) {
		
	}

	@Override
	public void controllerDownReleased(int arg0) {
		
	}

	@Override
	public void controllerLeftPressed(int arg0) {
		
	}

	@Override
	public void controllerLeftReleased(int arg0) {

	}

	@Override
	public void controllerRightPressed(int arg0) {

		
	}

	@Override
	public void controllerRightReleased(int arg0) {

	}

	@Override
	public void controllerUpPressed(int arg0) {

		
	}

	@Override
	public void controllerUpReleased(int arg0) {

		
	}

	@Override
	public void enter(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return this.STATE_ID;
	}

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics g)
			throws SlickException {
		Main.getInstance().getWorld().render(g);
		Main.getInstance().getCamera().renderFinish(container, g);
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int delta)
			throws SlickException {
		Main.getInstance().getController().update(container, delta);
		Main.getInstance().getCamera().update(container, delta);
		Main.getInstance().getWorld().update(delta);
	}

}
