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
import cr0s.javara.util.Pos;

public class StateTestScreen extends BasicGameState implements MouseListener, InputListener {

    public static final int STATE_ID = 4;


    private static final Color BACKGROUND_COLOR = new Color(32, 0, 0, 255);


    GameContainer c;


    private float displace;


    private int ticks;

    public StateTestScreen() {

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
    }
    
    public static float getRandom(float from, float to) {
	return (float) (from + Math.random() * (to - from));
    }
    
    public static void drawLine(Graphics g, float _x1, float _y1, float _x2, float _y2, float thickness)
    {
	g.setLineWidth(thickness);
	g.drawLine(_x1, _y1, _x2, _y2);
    }
    
    public static void drawChainLightning(Graphics g, Pos[] points, float thickness, int numberOfBolts)
    {
       for (int i = 0;i<points.length-1;i++)
       {
          drawP2PLightning(g, points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i+1].getY(), getRandom(60f, 140f), getRandom(0.8f, 3.8f), thickness, numberOfBolts);
       }
    }
    
    public static void drawP2ALightning(Graphics g, float x1, float y1, float x2, float y2, float displace, float detail, float thickness, float noise, int numberOfBolts)
    {
       for (int i=0;i<numberOfBolts;i++)
       {
          g.setColor(new Color((int) getRandom(14f, 54f), (int) getRandom(100f, 210f), (int) getRandom(200f, 239f), 255));
          drawSingleP2PLightning(g, x1, y1, x2 + getRandom(-noise, noise), y2 + getRandom(-noise, noise), 117, 1.8f, thickness);
       }
    }
    
    public static void drawP2PLightning(Graphics g, float x1, float y1, float x2, float y2, float displace, float detail, float thickness, int numberOfBolts)
    {
       for (int i=0;i<numberOfBolts;i++)
       {
	   g.setColor(new Color((int) getRandom(14f, 54f), (int) getRandom(100f, 210f), (int) getRandom(200f, 239f), 255));
          drawSingleP2PLightning(g, x1, y1, x2, y2, 117, 1.8f, thickness);
       }
    }
    
    public static void drawSingleP2PLightning(Graphics g, float x1, float y1, float x2, float y2, float displace, float detail, float thickness)
    {
      if (displace < detail)
      {
         drawLine(g, x1, y1, x2, y2, thickness);
      }
      else
      {
        float mid_x = (x2+x1)*0.5f;
        float mid_y = (y2+y1)*0.5f;
        mid_x += (Math.random()-0.5f)*displace;
        mid_y += (Math.random()-0.5f)*displace;
        drawSingleP2PLightning(g, x1,y1,mid_x,mid_y,displace*0.5f, detail, thickness);
        drawSingleP2PLightning(g, x2,y2,mid_x,mid_y,displace*0.5f, detail, thickness);
      }
    }    
    
    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1)
	    throws SlickException {
	// TODO Auto-generated method stub

    }

    @Override
    public void render(GameContainer c, StateBasedGame arg1, Graphics g)
	    throws SlickException {
	g.setColor(Color.black);
	g.clear();
	
	//Graphics g, float x1, float y1, float x2, float y2, float displace, float detail, float thickness, int numberOfBolt
	final int LENGTH = 400;
	drawP2PLightning(g, c.getWidth() / 2 - LENGTH / 2, c.getHeight() / 2, c.getWidth() / 2 + LENGTH / 2, c.getHeight() / 2, this.displace, 2.0f, 1.0f, 1);
    }

    @Override
    public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
	    throws SlickException {
	if (++this.ticks % 2 == 0) {
	    this.displace++;
	}
    }
}
