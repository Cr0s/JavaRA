package cr0s.javara.main;

import java.util.LinkedList;

import org.lwjgl.opengl.Display;
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
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.effect.MoveFlash;
import cr0s.javara.entity.effect.ScreenShaker;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.perfomance.PerfomanceGraphRenderer;
import cr0s.javara.perfomance.Profiler;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.cursor.CursorManager;
import cr0s.javara.ui.cursor.CursorType;
import cr0s.javara.util.Pos;

public class StateGameMap extends BasicGameState {
    public static final int STATE_ID = 1;

    private GameContainer container;

    private Point pressStart = new Point(0, 0);
    private boolean selectionRectVisible = true;
    private Rectangle selectionRect = new Rectangle(0, 0, 0, 0);
    private Color selectionFillColor = new Color(0, 0, 0, 64);

    private boolean isAnyMovableEntitySelected = false;

    private Entity mouseOverEntity = null;

    private final int CURSOR_UPDATE_INTERVAL_TICKS = 100;
    private int cursorUpdateTicks = CURSOR_UPDATE_INTERVAL_TICKS;
    
    public StateGameMap(final GameContainer container) {
	this.container = container;
    }

    @Override
    public void mouseDragged(final int oldX, final int oldY, final int newX, final int newY) {
	if (Main.getInstance().getContainer().getInput().isMouseButtonDown(0) || Main.getInstance().getSideBar().isMouseInsideBar()) {
	    if (!Main.getInstance().getBuildingOverlay().isInBuildingMode()) {
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
	    } else {
		Main.getInstance().getBuildingOverlay().mouseDragged(oldX, oldY, newX, newY);
	    }
	}
    }

    @Override
    public final void mouseMoved(final int arg0, final int arg1, final int x, final int y) {
	updateCursor();
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

	Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);

	// Is there entity under mouse
	Target target;
	if (e != null) {
	    target = new Target(e);
	} else {
	    target = new Target(new Pos((-Main.getInstance().getCamera().getOffsetX() + x) / 24, (-Main.getInstance().getCamera().getOffsetY() + y) / 24));
	}

	// We have no selected entities
	if (Main.getInstance().getPlayer().selectedEntities.isEmpty()) {
	    if (target.isEntityTarget() && button == 0) {
		Main.getInstance().getPlayer().selectOneEntity(e);
	    } else {
		CursorManager.getInstance().setCursorType(CursorType.CURSOR_POINTER);
	    }
	} else {
	    InputAttributes ia = new InputAttributes(button);
	    OrderTargeter targeterForEntity = Main.getInstance().getPlayer().getBestOrderTargeterForTarget(target);
	    boolean moveFlashSpawned = false;

	    if (targeterForEntity != null) {
		// Issue orders to selected entities
		for (Entity entity : Main.getInstance().getPlayer().selectedEntities) {
		    if (!(entity instanceof EntityActor) || !entity.isSelected) {
			continue;
		    }

		    EntityActor ea = (EntityActor) entity;

		    Order order = ea.issueOrder(ea, targeterForEntity, target, ia);

		    if (order != null) {
			ea.resolveOrder(order);

			if (order.orderString.equals("Move") && !moveFlashSpawned) {
			    moveFlashSpawned = true;

			    MoveFlash flash = new MoveFlash(order.targetPosition.getX() * 24, order.targetPosition.getY() * 24, ea.team, ea.owner, 24, 24);
			    flash.setWorld(ea.world);
			    flash.isVisible = true;

			    ea.world.spawnEntityInWorld(flash);
			}
		    } else {
			// Current entity can't resolve this order, so de-select this entity
			if (entity != null) { 
			    entity.isSelected = false;
			}
		    }
		}

		// Play sound only if order given
		if (button == 1) { 
		    if (targeterForEntity.entity.owner == Main.getInstance().getPlayer()) {
			targeterForEntity.entity.playOrderSound();
		    }
		}
	    } else {
		Main.getInstance().getWorld().cancelAllSelection();
	    }

	    Main.getInstance().getPlayer().removeNotActuallySelectedEntities();

	    // No one entity left, set basic pointer cursor
	    if (!Main.getInstance().getPlayer().isAnyActorEntitySelected()) {		
		if (e != null && button == 0) {
		    Main.getInstance().getPlayer().selectOneEntity(e);
		} else {
		    CursorManager.getInstance().setCursorType(CursorType.CURSOR_POINTER);
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
		Main.getInstance().getWorld().cancelAllSelection();
		LinkedList<Entity> entities = Main.getInstance().getWorld().selectMovableEntitiesInsideBox(this.selectionRect);

		Main.getInstance().getPlayer().selectedEntities.addAll(entities);

		OrderTargeter targeterForEntity = Main.getInstance().getPlayer().getBestOrderTargeterForTarget(new Target(new Pos(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y)));
		if (targeterForEntity != null) {
		    targeterForEntity.entity.playSelectedSound();
		} else {
		    if (!entities.isEmpty()) { 
			((EntityActor) entities.get(0)).playSelectedSound();
		    }
		}
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
	//Profiler.getInstance().startForSection("Render: world");
	Main.getInstance().getWorld().render(g);
	//Profiler.getInstance().stopForSection("Render: world");

	if (this.selectionRectVisible) {
	    g.setLineWidth(2);
	    g.setColor(Color.white);
	    g.draw(selectionRect);

	    g.setColor(this.selectionFillColor);
	    g.fillRect(selectionRect.getMinX() + 2, selectionRect.getMinY() + 2, selectionRect.getWidth() - 2, selectionRect.getHeight() - 2);
	}

	Main.getInstance().getCamera().renderFinish(container, g);

	Main.getInstance().getSideBar().render(g);

	PerfomanceGraphRenderer.render(g, new Pos(10, arg0.getHeight() - PerfomanceGraphRenderer.HEIGHT - 10));
	
	arg0.getDefaultFont().drawString(0, 0, "FPS: " + arg0.getFPS());
	
	CursorManager.getInstance().drawCursor(g);
    }

    @Override
    public final void update(final GameContainer arg0, final StateBasedGame arg1, final int delta)
	    throws SlickException {

	Profiler.getInstance().startForSection("Tick");
	
	Main.getInstance().getController().update(container, delta);
	
	ScreenShaker.getInstance().update(delta);
	Main.getInstance().getCamera().update(container, delta);
	updateCursor();

	Main.getInstance().getWorld().update(delta);
	Main.getInstance().getBuildingOverlay().update(delta);
	Main.getInstance().getSideBar().update(delta);

	SoundManager.getInstance().update(delta);
	
	Profiler.getInstance().stopForSection("Tick");
    }

    private void updateCursor() {
	CursorManager.getInstance().update();

	if (--this.cursorUpdateTicks <= 0) {
	    this.cursorUpdateTicks = this.CURSOR_UPDATE_INTERVAL_TICKS;
	    return;
	}

	if (Main.getInstance().getSideBar().isMouseInsideBar() || Main.getInstance().getBuildingOverlay().isInBuildingMode()) {
	    CursorManager.getInstance().setCursorType(CursorType.CURSOR_POINTER);
	    return;
	}

	this.cursorUpdateTicks = this.CURSOR_UPDATE_INTERVAL_TICKS;
	int x = Main.getInstance().getContainer().getInput().getMouseX();
	int y = Main.getInstance().getContainer().getInput().getMouseY();
	Entity e = Main.getInstance().getWorld().getEntityInPoint(-Main.getInstance().getCamera().getOffsetX() + x, -Main.getInstance().getCamera().getOffsetY() + y);

	// Is there entity under mouse
	Target target;
	if (e != null) {
	    if (this.mouseOverEntity != null) {
		this.mouseOverEntity.isMouseOver = false;
	    }

	    this.mouseOverEntity = e;
	    e.isMouseOver = true;

	    target = new Target(e);
	} else {
	    if (this.mouseOverEntity != null) {
		this.mouseOverEntity.isMouseOver = false;
		this.mouseOverEntity = null;
	    }

	    target = new Target(new Pos((-Main.getInstance().getCamera().getOffsetX() + x) / 24, (-Main.getInstance().getCamera().getOffsetY() + y) / 24));
	}

	// We have no selected entities
	if (Main.getInstance().getPlayer().selectedEntities.isEmpty()) {
	    if (target.isEntityTarget()) {
		CursorManager.getInstance().setCursorType(CursorType.CURSOR_SELECT);
	    } else {
		CursorManager.getInstance().setCursorType(CursorType.CURSOR_POINTER);
	    }
	} else if (!Main.getInstance().getPlayer().selectedEntities.isEmpty()) {
	    OrderTargeter targeterForEntity = Main.getInstance().getPlayer().getBestOrderTargeterForTarget(target);

	    if (targeterForEntity != null) {
		CursorManager.getInstance().setCursorType(targeterForEntity.getCursorForTarget(targeterForEntity.entity, target));
	    } else {
		if (e != null) {
		    CursorManager.getInstance().setCursorType(CursorType.CURSOR_SELECT);
		} else {
		    CursorManager.getInstance().setCursorType(CursorType.CURSOR_POINTER);
		}
	    }
	}
    }
}
