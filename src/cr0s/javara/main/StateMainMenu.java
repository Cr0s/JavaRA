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

public class StateMainMenu extends BasicGameState implements MouseListener, InputListener {

	public static final int STATE_ID = 0;
	
	private ArrayList<MainMenuItem> menuItems = new ArrayList<>();
	
	private SpriteSheet ss;
	private Image menuBackground;
	private final int MENU_WIDTH = 480;
	private final float MENU_X = 155f, MENU_Y = 25f;
	
	private Image menuFrameVert;
	private final int MENU_FRAME_VERT_HEIGHT = 192, MENU_FRAME_VERT_WIDTH = 18;

	private Image menuFrameHoriz;
	private final int MENU_FRAME_HORIZ_HEIGHT = 18, MENU_FRAME_HORIZ_WIDTH = 191;
	
	private Image menuFrameTick;
	private final int MENU_FRAME_TICK_HEIGHT = 20, MENU_FRAME_TICK_WIDTH = 18;
	
	private final int MENU_BUTTONS_SPACE = 25;
	
	private Image menuButton, menuButtonMouseover, menuButtonPressed;
	private final int MENU_BUTTON_SIZE_SHEET = 128; // In sprites sheet
	private final int MENU_BUTTON_HEIGHT = 60;
	private final int MENU_BUTTON_WIDTH = MENU_WIDTH - MENU_BUTTONS_SPACE * 2;
	
	private int MENU_HEIGHT;
	
	private final float ITEMS_X = MENU_X + 25f, ITEMS_Y = MENU_Y + 25f;
	
	private boolean leftMousePressed = false;
	
	GameContainer c;
	
	public StateMainMenu() {

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
		if (button == 0) {
			this.leftMousePressed = true;
		}
	}

	@Override
	public void mouseReleased(int button, int arg1, int arg2) {
		if (button == 0) {
			this.leftMousePressed = false;
			
			for (MainMenuItem m : menuItems) {
				if (m.isSelected) {
					switch (m.id) {
						case 0:
							Main.getInstance().startNewGame("map1");
							Main.getInstance().enterState(1);
							break;
							
						case 2:
							c.exit();
							break;
					}
				}
			}
		}
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
	public void enter(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
	    Main.getInstance().setCursorType(CursorType.CURSOR_POINTER);
	}

	@Override
	public int getID() {
		return this.STATE_ID;
	}

	@Override
	public void init(GameContainer c, StateBasedGame arg1)
			throws SlickException {
		this.c = c;
		
		menuItems.add(new MainMenuItem(0, "Start test game"));
		menuItems.add(new MainMenuItem(1, "Options"));
		menuItems.add(new MainMenuItem(2, "Quit"));
		
		this.MENU_HEIGHT = MENU_FRAME_HORIZ_HEIGHT + menuItems.size() * (this.MENU_BUTTON_HEIGHT + MENU_BUTTONS_SPACE);
		
		try {
			this.ss = new SpriteSheet(ResourceManager.RESOURCE_FOLDER + "dialog.png", 1024, 512);
			
			this.menuBackground = ss.getSubImage(0, 0, MENU_WIDTH, MENU_HEIGHT);
			this.menuFrameVert = ss.getSubImage(480, 0, MENU_FRAME_VERT_WIDTH, MENU_FRAME_VERT_HEIGHT);
			this.menuFrameHoriz = ss.getSubImage(0, 480, MENU_FRAME_HORIZ_WIDTH, MENU_FRAME_HORIZ_HEIGHT);
			
			this.menuFrameTick = ss.getSubImage(191, 480, MENU_FRAME_TICK_WIDTH, MENU_FRAME_TICK_HEIGHT);
			
			this.menuButton = ss.getSubImage(512, -1, MENU_BUTTON_SIZE_SHEET, MENU_BUTTON_SIZE_SHEET + 2);
			this.menuButtonMouseover = ss.getSubImage(512, 127, MENU_BUTTON_SIZE_SHEET, MENU_BUTTON_SIZE_SHEET);
			this.menuButtonPressed = ss.getSubImage(512 + 128, 127, MENU_BUTTON_SIZE_SHEET, MENU_BUTTON_SIZE_SHEET);
		} catch (SlickException e) {
			e.printStackTrace();
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
		g.clear();
		
		this.menuBackground.draw(MENU_X, MENU_Y);
		
		// Draw frames
		Image scaledFrameVert = this.menuFrameVert.getScaledCopy(MENU_FRAME_VERT_WIDTH, MENU_HEIGHT);
		scaledFrameVert.draw(MENU_X - MENU_FRAME_VERT_WIDTH, MENU_Y);
		scaledFrameVert.draw(MENU_X + MENU_WIDTH, MENU_Y);
		
		Image scaledFrameHoriz = this.menuFrameHoriz.getScaledCopy(MENU_WIDTH + 2 * MENU_FRAME_TICK_WIDTH, MENU_FRAME_HORIZ_HEIGHT);
		scaledFrameHoriz.draw(MENU_X - MENU_FRAME_TICK_WIDTH, MENU_Y - MENU_FRAME_HORIZ_HEIGHT);
		scaledFrameHoriz.draw(MENU_X - MENU_FRAME_TICK_WIDTH, MENU_Y + MENU_HEIGHT);
		
		// Menu ticks in corners
		/*this.menuFrameTick.draw(MENU_X - MENU_FRAME_TICK_WIDTH, MENU_Y - MENU_FRAME_TICK_HEIGHT);
		this.menuFrameTick.draw(MENU_X + MENU_WIDTH, MENU_Y - MENU_FRAME_TICK_HEIGHT);
		
		this.menuFrameTick.draw(MENU_X - MENU_FRAME_TICK_WIDTH, MENU_Y + MENU_HEIGHT);
		this.menuFrameTick.draw(MENU_X + MENU_WIDTH, MENU_Y + MENU_HEIGHT);
		*/
		
		// Draw menu buttons
		int itemsSize = this.menuItems.size();
		Image menuItem = menuButton.getScaledCopy(MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
		Image menuItemMouseover = menuButtonMouseover.getScaledCopy(MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
		Image menuItemPressed = menuButtonPressed.getScaledCopy(MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
		
		int mouseX = c.getInput().getMouseX();
		int mouseY = c.getInput().getMouseY();
		
		for (int i = 0; i < itemsSize; i++) {
			MainMenuItem m = this.menuItems.get(i);
			
			float itemX = this.ITEMS_X;
			float itemY = this.ITEMS_Y + (i * (this.MENU_BUTTON_HEIGHT + this.MENU_BUTTONS_SPACE));
			
			m.boundingBox.setBounds(itemX, itemY, this.MENU_BUTTON_WIDTH, this.MENU_BUTTON_HEIGHT);
			
			if (m.boundingBox.contains(mouseX, mouseY)) {
				if (!this.leftMousePressed) {
					m.isMouseOver = true;
					m.isSelected = false;
					
					menuItemMouseover.draw(itemX, itemY);
				} else {
					m.isSelected = true;
					menuItemPressed.draw(itemX, itemY);
				}
			} else {
				m.isMouseOver = false;
				m.isSelected = false;
				
				menuItem.draw(itemX, itemY);
			}
			
			c.getDefaultFont().drawString(itemX + (MENU_BUTTON_WIDTH / 2) - (c.getDefaultFont().getWidth(m.text) / 2), itemY + MENU_BUTTON_HEIGHT / 2 - (c.getDefaultFont().getLineHeight() / 2), m.text);
			
		}
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	private class MainMenuItem {
		public int id;
		public String text;
		
		public Rectangle boundingBox = new Rectangle(0, 0, 0, 0);
		public boolean isMouseOver = false;
		public boolean isSelected = false;
		
		public MainMenuItem(int id, String text) {
			this.id = id;
			this.text = text;
		}
	}

}
