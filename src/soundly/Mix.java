/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundly;

/**
 * A 'mix' -- e.g. SFXMix, MusicMix, HUDMix, SpeechMix
 * or, say, FootstepsMix, if you really want to get specific!
 * 
 * This is useful for situations where you know you want to mix the different
 * sound elements in a certain way; e.g. pan all footsteps left, or allow the
 * user to adjust the game or HUD sounds differently, etc.
 * 
 * @author Matt
 */
public class Mix {
    
    private float volume = 1f;
    private float pitch = 1f;
    private float xAdjustment = 0f;
    private float yAdjustment = 0f;
    private float depthAdjustment = 0f;
    private String name = null;
    
    public Mix() {
        this(null, 1f, 1f);
    }
    
    public Mix(String name, float volume, float pitch) {
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public Mix(String name) {
        this(name, 1f, 1f);
    }
    
    public Mix(float volume, float pitch) {
        this(null, volume, pitch);
    }
    
    public Mix(float volume) {
        this(null, volume);
    }
    
    public Mix(String name, float volume) {
        this(name, volume, 1f);
    }
    
    /**
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @param pitch the pitch to set
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * @return the xAdjustment
     */
    public float getXAdjustment() {
        return xAdjustment;
    }

    /**
     * @param xAdjustment the xAdjustment to set
     */
    public void setXAdjustment(float xAdjustment) {
        this.xAdjustment = xAdjustment;
    }

    /**
     * @return the yAdjustment
     */
    public float getYAdjustment() {
        return yAdjustment;
    }

    /**
     * @param yAdjustment the yAdjustment to set
     */
    public void setYAdjustment(float yAdjustment) {
        this.yAdjustment = yAdjustment;
    }

    /**
     * @return the depthAdjustment
     */
    public float getDepthAdjustment() {
        return depthAdjustment;
    }

    /**
     * @param depthAdjustment the depthAdjustment to set
     */
    public void setDepthAdjustment(float depthAdjustment) {
        this.depthAdjustment = depthAdjustment;
    }
    
    
    public String toString() {
        return String.valueOf(name);
    }
}
