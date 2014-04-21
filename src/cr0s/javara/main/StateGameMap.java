package cr0s.javara.main;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.ISelectable;

public class StateGameMap extends BasicGameState {
	public static final int STATE_ID = 1;

	private GameContainer container;
	
	private Point pressStart = new Point(0, 0);
	private boolean selectionRectVisible = true;
	private Rectangle selectionRect = new Rectangle(0, 0, 0, 0);
	
	private boolean isAnyMovableEntitySelected = false;
	
	private Entity mouseOverEntity = null;
	
	public StateGameMap(final GameContainer container) {
		this.container = container;
	}
	
	@Override
	public void mouseDragged(final int arg0, final int arg1, final int newX, final int newY) {
	    if (Main.getInstance().getContainer().getInput().isMouseButtonDown(0)) {
		if (!this.selectionRectVisible) {
		    this.selectionRectVisible = true;
		}
		
		float startX = this.pressStart.getX();
		float startY = this.pressStart.getY();
		
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
	public final void mouseMoved(final int arg0, final int arg1, final int x, final int y) {
	    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
	
	    if (e != null) {
		if (this.mouseOverEntity != null && this.mouseOverEntity != e) {
		    this.mouseOverEntity.isMouseOver = false;
		} else {
		    if (e.isSelected && this.mouseOverEntity == e) {
			if (e instanceof IDeployable) {
			    if (((IDeployable)e).canDeploy()) { 
				Main.getInstance().setCursorType(CursorType.CURSOR_DEPLOY);
			    } else {
				Main.getInstance().setCursorType(CursorType.CURSOR_NO_DEPLOY);
			    }
			} else {
			    Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
			}
			return;
		    }
		}
		
		this.mouseOverEntity = e;
		e.isMouseOver = true;
		
		if (!e.isSelected) { 
		    Main.getInstance().setCursorType(CursorType.CURSOR_SELECT);
		}
	    } else {
		if (this.mouseOverEntity != null) {
		    this.mouseOverEntity.isMouseOver = false;
		    this.mouseOverEntity = null;
		}
		
		if (!this.isAnyMovableEntitySelected) {
		    Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
		} else {
		    setGotoCursorIfCellPassable(x, y);
		}
	    }
	}

	@Override
	public final void mouseClicked(final int button, final int x, final int y, final int clickCount) {
	    	Main.getInstance().getController().mouseClicked(button, x, y, clickCount);
		
	    	if (Main.getInstance().getSideBar().isMouseInsideBar()) {
	    	    Main.getInstance().getSideBar().mouseClicked(button, x, y);
	    	    return;
	    	}
	    	
	    	if (Main.getInstance().getBuildingOverlay().isInBuildingMode()) {
	    	    Main.getInstance().getBuildingOverlay().mouseClick(button);
	    	    
	    	    return;
	    	}
	    	
		if (button == 0) { 
		    Main.getInstance().getWorld().cancelAllSelection();
		    
		    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
		    
		    if (e != null) { 
			((ISelectable) e).select();
			Main.getInstance().getPlayer().selectedEntities.add(e);
			
			this.isAnyMovableEntitySelected = (e != null && e instanceof IMovable);
			if (this.isAnyMovableEntitySelected) {
			    if (this.mouseOverEntity == e && (e instanceof IDeployable)) {
				if (((IDeployable) e).canDeploy()) {
				    Main.getInstance().setCursorType(CursorType.CURSOR_DEPLOY);
				} else {
				    Main.getInstance().setCursorType(CursorType.CURSOR_NO_DEPLOY);
				}
			    } else {
				setGotoCursorIfCellPassable(x, y);
			    }
			} else {
			    Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
			}
		    } else {
			Main.getInstance().getPlayer().selectedEntities.clear();
			this.isAnyMovableEntitySelected = false;
			Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
		    }
		} else if (button == 1) {
		    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
		    
		    if (e != null) { 
			this.isAnyMovableEntitySelected = (e != null && e instanceof IMovable);
			
			if (this.mouseOverEntity == e && (e instanceof IDeployable) && (e.isSelected)) {
			    if (((IDeployable) e).canDeploy()) { 
				((IDeployable) e).deploy();

				this.isAnyMovableEntitySelected = false;
				this.mouseOverEntity = null;

				Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
			    }
			}
		    } else {
			if (this.isAnyMovableEntitySelected && Main.getInstance().getCursor() != CursorType.CURSOR_NO_GOTO) {
			    float destX = -Main.getInstance().getCamera().getOffsetX() + x;
			    float destY = -Main.getInstance().getCamera().getOffsetY() + y;

			    Main.getInstance().getPlayer().postMoveOrder(destX, destY);			    
			}
		    }
		}
	}

	@Override
	public final void mousePressed(final int button, final int x, final int y) {
	    	if (button == 0) { 
	    	    this.pressStart.setLocation(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
	    	}
	    	
		Main.getInstance().getController().mousePressed(button, x, y);
	}

	@Override
	public final void mouseReleased(final int button, final int x, final int y) {
	    Main.getInstance().getController().mouseReleased(button, x, y);
	    
	    if (button == 0 && this.selectionRectVisible) {
		this.selectionRectVisible = false;
		
		if (this.selectionRect.getWidth() * this.selectionRect.getHeight() > 4) {
		    LinkedList<Entity> entities = Main.getInstance().getWorld().selectMovableEntitiesInsideBox(this.selectionRect);
		    
		    Main.getInstance().getPlayer().selectedEntities.addAll(entities);
		    
		    for (Entity e : entities) {
			if (e instanceof IMovable) {
			    this.isAnyMovableEntitySelected = true;
			    setGotoCursorIfCellPassable(x, y);
			    return;
			}
		    }
		    
		    
		    this.isAnyMovableEntitySelected = false;
		    Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);    
		}
	    }
	}

	private void setGotoCursorIfCellPassable(int mouseX, int mouseY) {
		int cellX = (int) (-Main.getInstance().getCamera().getOffsetX() + mouseX) / 24;
		int cellY = (int) (-Main.getInstance().getCamera().getOffsetY() + mouseY) / 24;

		if (Main.getInstance().getWorld().isCellPassable(cellX, cellY)) {
		    Main.getInstance().setCursorType(CursorType.CURSOR_GOTO);
		} else {
		    Main.getInstance().setCursorType(CursorType.CURSOR_NO_GOTO);
		}	    
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
	public void enter(final GameContainer c, final StateBasedGame sbg)
			throws SlickException {
	}

	@Override
	public final int getID() {
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
		
		Main.getInstance().getSideBar().render(g);
	}

	@Override
	public final void update(final GameContainer arg0, final StateBasedGame arg1, final int delta)
			throws SlickException {
	    
		Main.getInstance().getController().update(container, delta);
		Main.getInstance().getCamera().update(container, delta);
		Main.getInstance().getWorld().update(delta);

		Main.getInstance().getSideBar().update(delta);
	}

}
