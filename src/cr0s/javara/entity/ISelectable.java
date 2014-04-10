package cr0s.javara.entity;

/**
 * Describes entity which can be selected by mouse.
 * @author Cr0s
 */
public interface ISelectable {
    public void select();
    public void cancelSelect();
    public boolean isSelected();
}
