package cr0s.javara.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import cr0s.javara.util.Pos;

public class SideBarToolTip {
    private static final int TOOLTIP_ALPHA = 128;
    private static final Color TOOLTIP_BORDER_COLOR = Color.yellow.multiply(new Color(255, 255, 255, TOOLTIP_ALPHA));
    private static final Color TOOLTIP_BG_COLOR = new Color(0, 0, 0, TOOLTIP_ALPHA);
    
    private static final int BORDER_SIZE = 3;
    private static final int LEFT_OFFSET = 16;
    private static final Color TOOLTIP_TEXT_COLOR = Color.yellow.multiply(new Color(255, 255, 255, TOOLTIP_ALPHA));
    
    public static void renderTooltipAt(Graphics g, Pos pos, String text) {
	pos.setX(pos.getX() - LEFT_OFFSET);
	g.setLineWidth(BORDER_SIZE);
	
	int toolTipWidth = g.getFont().getWidth(text) + BORDER_SIZE * 2;
	int toolTipHeight = g.getFont().getHeight(text) + BORDER_SIZE * 2;
	
	g.setColor(TOOLTIP_BG_COLOR);
	g.fillRect(pos.getX() - toolTipWidth, pos.getY(), toolTipWidth, toolTipHeight);
	
	g.setColor(TOOLTIP_BORDER_COLOR);
	g.drawRect(pos.getX() - toolTipWidth, pos.getY(), toolTipWidth, toolTipHeight);
	
	g.setColor(TOOLTIP_TEXT_COLOR);
	g.drawString(text, pos.getX() - toolTipWidth + BORDER_SIZE, pos.getY() + BORDER_SIZE);
    }
}
