package cr0s.javara.resources;

import java.util.Random;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.util.Pos;
import soundly.Soundly;
import soundly.XSound;

public class SoundManager {
    private static SoundManager instance;

    private int LAYER_SPEECH = 3;
    private int LAYER_UNIT = 2;
    private int LAYER_SFX = 1;

    public Random r;

    private SoundManager() {
	Soundly sound = Soundly.get();

	sound.init(10);

	// Listener radius is player's vieport radius
	Rectangle viewportRect = Main.getInstance().getCamera().viewportRect;
	sound.setListenerRadius((float) Math.sqrt(viewportRect.getHeight() * viewportRect.getHeight() + viewportRect.getWidth() * viewportRect.getWidth()) / 2f);

	sound.setDistanceCheckingEnabled(true);
	sound.setProximityEnabled(true); 

	this.r = new Random(System.currentTimeMillis());
    }

    public static SoundManager getInstance() {
	if (instance == null) {
	    instance = new SoundManager();
	}

	return instance;
    }

    public void playSpeechSoundGlobal(String sound) {
	XSound snd = ResourceManager.getInstance().loadSpeechSound(sound);
	snd.setLayer(LAYER_SPEECH);
	snd.setVolume(1);

	playSoundGlobal(snd);
    }

    public void playSfxGlobal(String sound, float volume) {
	XSound snd = ResourceManager.getInstance().loadSound("sounds.mix", sound + ".aud");
	snd.setLayer(LAYER_SFX);
	snd.setVolume(volume);

	playSoundGlobal(snd);
    }

    public void playSoundGlobal(XSound snd) {	
	snd.setLooping(false);
	snd.setPosition(0, 0);
	snd.setRadius(9000); // player must hear it everywhere on map
	snd.setProximityEnabled(false);

	snd.queue();
    }

    public void playUnitSoundGlobal(EntityActor unit, String sound, int version) {
	String prefix = (unit.owner.getAlignment() == Alignment.ALLIED) ? ".v" : ".r"; 
	String name = sound + prefix + "0" + version;

	XSound snd = ResourceManager.getInstance().loadUnitSound(unit.owner.getAlignment(), name);

	if (snd != null) {
	    snd.setLayer(LAYER_UNIT);
	    playSoundGlobal(snd);
	} else {
	    System.err.println("Sound not found: " + name);
	}
    }

    public void update(int delta) {
	Soundly.get().update(delta);
    }

    public void playSfxAt(String sound, Pos pos) {
	XSound snd = ResourceManager.getInstance().loadSound("sounds.mix", sound + ".aud");

	if (snd != null) {
	    snd.setLayer(LAYER_SFX);
	    snd.setVolume(1);

	    snd.setPosition(pos.getX(), pos.getY());
	    snd.setRadius(24 * 10);
	    snd.setProximityEnabled(true);

	    snd.queue();
	}
    }
}
