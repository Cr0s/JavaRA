package cr0s.javara.entity.turreted;

import java.util.List;

import org.newdawn.slick.Graphics;

public interface IHaveTurret {
    public void drawTurrets(Graphics g);
    public void updateTurrets(int delta);
    
    public List<Turret> getTurrets();
}
