package cr0s.javara.main;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.CursorLoader;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IMovable;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.util.Point;
import cr0s.javara.entity.ISelectable;

public class StateGameMap extends BasicGameState {
	public static final int STATE_ID = 1;

	private GameContainer container;
	
	private Point pressStart = new Point(0, 0);
	private boolean selectionRectVisible = true;
	private Rectangle selectionRect = new Rectangle(0, 0, 0, 0);
	
	private boolean isAnyMovableEntitySelected = false;
	
	private Entity mouseOverEntity = null;
	
	private GameSideBar gsb;
	
	public StateGameMap(final GameContainer container) {
		this.container = container;
		
		this.gsb = new GameSideBar(Main.getInstance().getTeam(), Main.getInstance().getPlayer());
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
	public final void mouseMoved(final int arg0, final int arg1, final int x, final int y) {
	    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
	
	    if (e != null) {
		if (this.mouseOverEntity != null && this.mouseOverEntity != e) {
		    this.mouseOverEntity.isMouseOver = false;
		} else {
		    if (e.isSelected && this.mouseOverEntity == e) {
			if (e instanceof IDeployable) {
			    if (((IDeployable)e).canDeploy()) { 
				Main.getInstance().setDeployCursor();
			    }
			} else {
			    Main.getInstance().resetCursor();
			}
			return;
		    }
		}
		
		this.mouseOverEntity = e;
		e.isMouseOver = true;
		
		if (!e.isSelected) { 
		    Main.getInstance().setSelectCursor();
		}
	    } else {
		if (this.mouseOverEntity != null) {
		    this.mouseOverEntity.isMouseOver = false;
		    this.mouseOverEntity = null;
		}
		
		if (!this.isAnyMovableEntitySelected) {
		    Main.getInstance().resetCursor();
		} else {
		    Main.getInstance().setGotoCursor();
		}
	    }
	}

	@Override
	public final void mouseClicked(final int button, final int x, final int y, final int clickCount) {
	    	Main.getInstance().getController().mouseClicked(button, x, y, clickCount);
		
		if (button == 0) { 
		    Main.getInstance().getWorld().cancelAllSelection();
		    
		    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
		    
		    if (e != null) { 
			((ISelectable) e).select();
			
			this.isAnyMovableEntitySelected = (e != null && e instanceof IMovable);
			if (this.isAnyMovableEntitySelected) {
			    if (this.mouseOverEntity == e && (e instanceof IDeployable)) {
				if (((IDeployable) e).canDeploy()) {
				    Main.getInstance().setDeployCursor();
				} else {
				    // TODO: Main.getInstance().setNoDeployCursor();
				}
			    } else {
				Main.getInstance().setGotoCursor();
			    }
			} else {
			    Main.getInstance().resetCursor();
			}
		    } else {
			this.isAnyMovableEntitySelected = false;
			Main.getInstance().resetCursor();
		    }
		} else if (button == 1) {
		    Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);
		    
		    if (e != null) { 
			this.isAnyMovableEntitySelected = (e != null && e instanceof IMovable);
			if (this.isAnyMovableEntitySelected) {
			    if (this.mouseOverEntity == e && (e instanceof IDeployable) && (e.isSelected)) {
				if (((IDeployable) e).canDeploy()) { 
				    ((IDeployable) e).deploy();
				    
				    this.isAnyMovableEntitySelected = false;
				    this.mouseOverEntity = null;
				    
				    Main.getInstance().resetCursor();
				}
			    } else {
				// TODO: move to cell code
			    }
			}
		    }
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
	    Main.getInstance().getController().mouseReleased(button, x, y);
	    
	    if (button == 0 && this.selectionRectVisible) {
		this.selectionRectVisible = false;
		
		if (this.selectionRect.getWidth() * this.selectionRect.getHeight() > 4) {
		    LinkedList<Entity> entities = Main.getInstance().getWorld().selectEntitiesInsideBox(this.selectionRect);
		    
		    for (Entity e : entities) {
			if (e instanceof IMovable) {
			    this.isAnyMovableEntitySelected = true;
			    Main.getInstance().setGotoCursor();
			    return;
			}
		    }
		    
		    
		    this.isAnyMovableEntitySelected = false;
		    Main.getInstance().resetCursor();	    
		}
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
		
		this.gsb.render(g);
	}

	@Override
	public final void update(final GameContainer arg0, final StateBasedGame arg1, final int delta)
			throws SlickException {
	    
		Main.getInstance().getController().update(container, delta);
		Main.getInstance().getCamera().update(container, delta);
		Main.getInstance().getWorld().update(delta);
		this.gsb.update(delta);
	}

}
