package cr0s.javara.main;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.cursor.CursorManager;
import cr0s.javara.ui.cursor.CursorType;

public class StateLoadingScreen extends BasicGameState implements MouseListener, InputListener {

    public static final int STATE_ID = 3;


    private static final Color BACKGROUND_COLOR = new Color(32, 0, 0, 255);


    GameContainer c;

    private boolean isLoading = false;
    private int ticksBeforeLoading = 10;
    private int ticks, i;


    private String[] strings = {
	    "Filling Crates...", 
	    "Charging Capacitors...", 
	    "Reticulating Splines...", 
	    "Planting Trees...", 
	    "Building Bridges...", 
	    "Aging Empires...", 
	    "Compiling EVA...", 
	    "Constructing Pylons...", 
	    "Activating Skynet...", 
	    "Splitting Atoms...",
    };

    public StateLoadingScreen() {

    }

    @Override
    public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {	
    }

    @Override
    public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
	// TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
	// TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(int button, int x, int y) {

    }

    @Override
    public void mouseReleased(int button, int arg1, int arg2) {

    }

    @Override
    public void mouseWheelMoved(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void inputEnded() {
	// TODO Auto-generated method stub

    }

    @Override
    public void inputStarted() {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean isAcceptingInput() {
	// TODO Auto-generated method stub
	return true;
    }

    @Override
    public void setInput(Input arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(int arg0, char arg1) {
	// TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(int arg0, char arg1) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerButtonPressed(int arg0, int arg1) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerButtonReleased(int arg0, int arg1) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerDownPressed(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerDownReleased(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerLeftPressed(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerLeftReleased(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerRightPressed(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerRightReleased(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerUpPressed(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void controllerUpReleased(int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void enter(GameContainer c, StateBasedGame arg1)
	    throws SlickException {

    }

    @Override
    public int getID() {
	return this.STATE_ID;
    }

    @Override
    public void init(GameContainer c, StateBasedGame arg1)
	    throws SlickException {
	this.c = c;
	
	this.i = (int) (Math.random() * this.strings.length);
    }

    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1)
	    throws SlickException {
	// TODO Auto-generated method stub

    }

    @Override
    public void render(GameContainer c, StateBasedGame arg1, Graphics g)
	    throws SlickException {
	g.setColor(BACKGROUND_COLOR);
	g.fillRect(0, 0, c.getWidth(), c.getHeight());

	String s = this.strings[this.i];
	float x = c.getWidth() - 15 - g.getFont().getWidth(s);
	float y = c.getHeight() - 15 - g.getFont().getHeight(s);

	g.getFont().drawString(x, y, s);
    }

    @Override
    public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
	    throws SlickException {
	if (!this.isLoading) {		
	    if (--this.ticksBeforeLoading < 0) {
		this.i = (int) (Math.random() * this.strings.length);
		this.isLoading = true;

		//new Thread(new Runnable() {
		//	    @Override
		//	    public void run() {
		Main.getInstance().startNewGame("");
		Main.getInstance().enterState(1);
		//	    }
		//	}).start();		
		//  }
		//}
	    }
	}
    }
}
