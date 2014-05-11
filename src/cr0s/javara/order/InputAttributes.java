package cr0s.javara.order;

public class InputAttributes {
    public int mouseButton;
    
    public boolean isCtrlPressed, isShiftPressed, isAltPressed, isSpacePressed;
    
    public InputAttributes(int mouse) {
	this.mouseButton = mouse;
    }
}
