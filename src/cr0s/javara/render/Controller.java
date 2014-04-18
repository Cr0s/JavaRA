package cr0s.javara.render;

import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import cr0s.javara.gameplay.Player;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.viewport.Camera;

public class Controller {

	private Player player;

	private Camera camera;
	private float baseX, baseY;
	private float offsetX, offsetY;

	// Controls
	private Input input;
	private boolean mouseDownRight = false;
	private boolean mouseDownLeft = false;

	private final int VIEWPORT_SCROLL_TRESHOLD = 35; // TODO: config this
	private final float SCROLL_SPEED = 0.5f; // TODO: config this
	private final int ALLOWED_BORDER_TILES = 0;


	public Controller(Player player, Camera camera, Input input) {
		this.player = player;
		this.camera = camera;
		this.input = input;

	}

	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		this.input = gc.getInput();

	}

	public void update(final GameContainer gc, final int delta)
			throws SlickException {
		if (gc.isPaused()) {
			return;
		}

		float dx = 0f, dy = 0;

		int mouseX = gc.getInput().getMouseX();
		int mouseY = gc.getInput().getMouseY();

		if (mouseX <= VIEWPORT_SCROLL_TRESHOLD) {
			dx = 1;
		}
		if (mouseX >= gc.getWidth() - VIEWPORT_SCROLL_TRESHOLD) {
			dx = -1;
		}

		if (mouseY <= VIEWPORT_SCROLL_TRESHOLD) {
			dy = 1;
		}
		if (mouseY >= gc.getHeight() - VIEWPORT_SCROLL_TRESHOLD) {
			dy = -1;
		}

		float newOffsetX = 0;
		float newOffsetY = 0;
		
		if (camera.map != null) {
			int mapWidthPixels = (int) camera.map.getBounds().getWidth() + (TileMap.MAP_OFFSET_TILES * 24) + (TileMap.ALLOWED_DARKNESS_SHIFT_XMAX * 24);
			int mapHeightPixels = (int) camera.map.getBounds().getHeight() + (TileMap.MAP_OFFSET_TILES * 24) + (TileMap.ALLOWED_DARKNESS_SHIFT * 24);
			
			newOffsetX = camera.getOffsetX() + (float) Math.floor(dx * SCROLL_SPEED * delta);
			newOffsetY = camera.getOffsetY() + (float) Math.floor(dy * SCROLL_SPEED * delta);
			
			float lowerBoundX = camera.map.getBounds().getMinX() - (2 * TileMap.MAP_OFFSET_TILES * 24) + (TileMap.ALLOWED_DARKNESS_SHIFT * 24);
			float lowerBoundY = camera.map.getBounds().getMinY() - (2 * TileMap.MAP_OFFSET_TILES * 24) + (TileMap.ALLOWED_DARKNESS_SHIFT * 24);
			
			// Clamp offset by map size
			if (newOffsetX > lowerBoundX) {
				newOffsetX = lowerBoundX;
			} else if (mapWidthPixels + newOffsetX < gc.getWidth()) {
				newOffsetX = -(mapWidthPixels - gc.getWidth());
			}
			
			if (newOffsetY > lowerBoundY) {
				newOffsetY = lowerBoundY;
			} else if (mapHeightPixels + newOffsetY < gc.getHeight()) {
				newOffsetY = -(mapHeightPixels - gc.getHeight());
			}
		}

		
		camera.setOffset(newOffsetX, newOffsetY);
	}


	public void mouseClicked(int button, int x, int y, int clickCount) {
		switch (button) {
		case 0:

			break;
		case 1:

			break;

		case 2:
			break;

		}
	}

	public void mousePressed(int button, int x, int y) {
		switch (button) {
		case 0:
			mouseDownLeft = true;

			break;
		case 1:
			mouseDownRight = true;
			break;

		case 2:

			break;

		}

	}

	public void mouseReleased(int button, int x, int y) {
		switch (button) {
		case 0:
			mouseDownLeft = false;
			break;
		case 1:
			mouseDownRight = false;
			break;
		}
	}
}