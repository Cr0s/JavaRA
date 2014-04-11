package cr0s.javara.main;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
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
	
	private Point pressStart = new Point(0, 0);
	private boolean selectionRectVisible = true;
	private Rectangle selectionRect = new Rectangle(0, 0, 0, 0);
	
	public StateGameMap(final GameContainer container) {
		this.container = container;
	}
	
	@Override
	public void mouseDragged(final int arg0, final int arg1, final int newX, final int newY) {
	    if (Main.getInstance().getContainer().getInput().isMouseButtonDown(0)) {
		if (!this.selectionRectVisible) {
		    this.selectionRectVisible = true;
		}
		
		float startX = this.pressStart.x;
		float startY = this.pressStart.y;
		
		float endX = -Main.getInstance().getCamera().getOffsetX() + newX;
		float endY = -Main.getInstance().getCamera().getOffsetY() + newY;
		float s;
		
		// Swap if necessary
		if (startX > endX) {
		    s = endX;
		    endX = startX;
		    startX = s;
		}

		if (startY > endY) {
		    s = endY;
		    endY = startY;
		    startY = s;
		}
		
		int boxH = (int) (endY - startY);
		int boxW = (int) (endX - startX);
		
		this.selectionRect.setBounds(startX, startY, boxW, boxH);	
	    }
	}

	@Override
	public final void mouseMoved(final int arg0, final int arg1, final int newX, final int newY) {

	}

	@Override
	public final void mouseClicked(final int button, final int x, final int y, final int clickCount) {
		Main.getInstance().getController().mouseClicked(button, x, y, clickCount);
		
		if (button == 0) { 
		    // TODO: check click inside entity
		    
		    Main.getInstance().getWorld().cancelAllSelection();
		}
	}

	@Override
	public final void mousePressed(final int button, final int x, final int y) {
	    	if (button == 0) { 
	    	    this.pressStart.changePos(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
	    	}
	    	
		Main.getInstance().getController().mousePressed(button, x, y);
	}

	@Override
	public final void mouseReleased(final int button, final int x, final int y) {
	    if (button == 0 && this.selectionRectVisible) {
		this.selectionRectVisible = false;
		
		if (this.selectionRect.getWidth() * this.selectionRect.getHeight() > 4) {
		    Main.getInstance().getWorld().selectEntitiesInsideBox(this.selectionRect);
		}
	    }
		Main.getInstance().getController().mouseReleased(button, x, y);
	}

	@Override
	public void mouseWheelMoved(final int arg0) {
		
	}

	@Override
	public void inputEnded() {
		
	}

	@Override
	public void inputStarted() {
		
	}

	@Override
	public final boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void setInput(final Input arg0) {
		
	}

	@Override
	public final void keyPressed(final int arg0, final char arg1) {
		if (this.container.getInput().isKeyDown(Input.KEY_ESCAPE)) {
			Main.getInstance().enterState(StatePauseMenu.STATE_ID);
		}
	}

	@Override
	public void keyReleased(final int arg0, final char arg1) {

	}

	@Override
	public void controllerButtonPressed(final int arg0, final int arg1) {
		
	}

	@Override
	public void controllerButtonReleased(final int arg0, final int arg1) {
		
	}

	@Override
	public void controllerDownPressed(final int arg0) {
		
	}

	@Override
	public void controllerDownReleased(final int arg0) {
		
	}

	@Override
	public void controllerLeftPressed(final int arg0) {
		
	}

	@Override
	public void controllerLeftReleased(final int arg0) {

	}

	@Override
	public void controllerRightPressed(final int arg0) {

		
	}

	@Override
	public void controllerRightReleased(final int arg0) {

	}

	@Override
	public void controllerUpPressed(final int arg0) {

		
	}

	@Override
	public void controllerUpReleased(final int arg0) {

		
	}

	@Override
	public void enter(final GameContainer arg0, final StateBasedGame arg1)
			throws SlickException {
		
	}

	@Override
	public final int getID() {
		// TODO Auto-generated method stub
		return this.STATE_ID;
	}

	@Override
	public void init(final GameContainer arg0, final StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(final GameContainer arg0, final StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void render(final GameContainer arg0, final StateBasedGame arg1, final Graphics g)
			throws SlickException {
		Main.getInstance().getWorld().render(g);
		
		if (this.selectionRectVisible) {
		    g.setLineWidth(2);
		    g.setColor(Color.white);
		    g.draw(selectionRect);
		}
		
		Main.getInstance().getCamera().renderFinish(container, g);
	}

	@Override
	public final void update(final GameContainer arg0, final StateBasedGame arg1, final int delta)
			throws SlickException {
		Main.getInstance().getController().update(container, delta);
		Main.getInstance().getCamera().update(container, delta);
		Main.getInstance().getWorld().update(delta);
	}

}
