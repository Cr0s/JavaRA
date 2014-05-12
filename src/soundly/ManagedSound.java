package soundly;

import org.newdawn.slick.openal.Audio;

/**
 * ManagedSound is the base
 * 
 * @author Matt
 */
public abstract class ManagedSound {
    
    private float priority = 0f;
    private int layer = 0;
    private float pitch = 1f;
    private float volume = 1f;
    private String description = "";
    private boolean looping = false;
    private boolean relative = false;
    private boolean replaceable = true;
    
    private Mix mix = null;
    ManagedSound parent;
    
    private float radius;
    float x, y, depth;
    private boolean proximity;
    float proximityVol = 0f;
    float distanceFromListener = 0f;
    boolean withinEarshot = false;
    
    private boolean music = false;
    
    /**
     * 
     * @param description
     */
    public ManagedSound(String description) {
        this.description = description;
    }
    
    /**
     * 
     */
    public ManagedSound() {
        this(null);
    }
    
    public abstract int queue();
    public abstract int queueAtIndex(int sourceIndex);
    
    public ManagedSound getParent() {
        return parent;
    }
    //source.pitch = 0.25
    //source.queue() 
    //source.update() - loads OpenAL 
    //source.pitch = 0.5
    //source.queue()
    //source.update() - loads another OpenAL
    //now we have two sources playing at different pitches
    //as soon as we do:
    //source.setPitch(1f)
    //source.update()
    //both sources will 
    
    
    /**
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }
    
    public void setDescription() {
        this.description = description;
    }
    
    /**
     * 
     * @param volume
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * 
     * @return
     */
    public float getVolume() {
        return volume;
    }

    
    /**
     * 
     * @param pitch
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
    
    /**
     * 
     * @return
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(float priority) {
        this.priority = priority;
    }

    public float getPriority() {
        return parent!=null ? parent.getPriority() : priority;
    }
    
    /**
     * @param level the level to set
     */
    public void setLayer(int level) {
        this.layer = level;
    }
    
    public int getLayer() {
        return parent!=null ? parent.getLayer() : layer;
    }
    
    /**
     * @param looping the looping to set
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isLooping() {
        return parent!=null ? parent.isLooping() : looping;
    }
    
    /**
     * @param relative the relative to set
     */
    public void setRelative(boolean relative) {
        this.relative = relative;
    }

    public boolean isRelative() {
        return parent!=null ? parent.isRelative() : relative;
    }
    
    public void setReplaceable(boolean replaceable) {
        this.replaceable = replaceable;
    }
    
    public boolean isReplaceable() {
        return replaceable;
    }

    public Mix getMix() {
        return mix;
    }
    
    public void setMix(Mix mix) {
        this.mix = mix;
    }
    
    public float mixedPitch() {
        return  (getMix()!=null ? getMix().getPitch() : 1f) *
                 getPitch() * 
                 (getParent()!=null ? getParent().mixedPitch() : 1f);
    }
    
    public float mixedVolume() {
        float proxVol = 0f;
        if (isProximityEnabled() && Soundly.get().isProximityEnabled())
            proxVol = proximityVol;
        float prox = Math.max(0f, getVolume()-proxVol);
        float f = (getMix()!=null ? getMix().getVolume() : 1f);
        float f2= (getParent()!=null ? getParent().mixedVolume() : 1f);
        return f * prox * f2;
    }
    
    public float mixedX() {
        return (getMix()!=null ? getMix().getXAdjustment() : 0f) +
                getX() + (getParent()!=null ? getParent().mixedX() : 0f);
    }
    
    public float mixedY() {
        return (getMix()!=null ? getMix().getYAdjustment() : 0f) +
                getY() + (getParent()!=null ? getParent().mixedY() : 0f);
    }
    
    public float mixedDepth() {
        return (getMix()!=null ? getMix().getDepthAdjustment() : 0f) +
                getDepth() + (getParent()!=null ? getParent().mixedDepth() : 0f);
    }
    
    public void replaceEvent(ManagedSound audio, int sourceIndex) {
        //do nothing... override!
    }

    /**
     * 
     * @return
     */
    public String toString() {
        String d = getDescription();
        return d!=null ? d : "null";
    }
    
    /**
     * 
     * @param radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
        distanceChanged();
    }
    
    /**
     * Sets the relative position of this Audio2D element.
     * 
     * Note: This method updates the distance and changes OpenAL parameters,
     * so setPosition(1, 1); is more efficient than calling setX(1);setY(1);
     * @param x 
     * @param y
     * @param depth  
     */
    public void setPosition(float x, float y, float depth) {
        float oldX = this.getX();
        float oldY = this.getY();
        float oldDepth = this.getDepth();
        this.x = x;
        this.y = y;
        this.depth = depth;
        if (oldX!=x||oldY!=y||oldDepth!=depth) {
            updatePosition();
            //recalculate the distance from listener
            distanceChanged();
        }
    }
    
    /**
     * 
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        setPosition(x, y, getDepth());
    }
    
    //override...
    /**
     * 
     */
    public void updatePosition() { }

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(float x) {
        setPosition(x, getY());
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(float y) {
        setPosition(getX(), y);
    }
    
    /**
     * 
     * @param depth
     */
    public void setDepth(float depth) {
        setPosition(getX(), getY(), depth);
    }

    /**
     * @return the depth
     */
    public float getDepth() {
        return depth;
    }
    
    
    //calculateGain:
    // (vol-proximity) * parentVol * mixVol
    //calculatePitch
    //calculateX/Y/Depth
    
    
    //TODO: cleanup distanceChanged with new implementations
    public float distanceChanged() {
        Soundly mgr = Soundly.get();
        distanceFromListener = Soundly.calculateDistance(getX(), getY(), mgr.getListenerX(), mgr.getListenerY());
        proximityVol = Soundly.calculateFromCenter(this, distanceFromListener);
        withinEarshot = proximityVol >= 0f && proximityVol <= 1f;
        return distanceFromListener;
    }
    
    /**
     * 
     * @return
     */
    public boolean isWithinEarshot() {
        distanceChanged();
        return withinEarshot;
    }

    /**
     * 
     * @param proximity
     */
    public void setProximityEnabled(boolean proximity) {
        this.proximity = proximity;
    }
//TODO: cleanup distance checking, reduce calls to calculateDistance(), add withinEarshot() EVENT
    
    public boolean isProximityEnabled() {
        return getParent()!=null ? getParent().isProximityEnabled() : proximity;
    }
    
    public float getRadius() {
        float rad = radius;
        if (rad<=0f && getParent()!=null)
            return getParent().getRadius();
        return rad;
    }
    
    
    /**
     * @return the music
     */
    public boolean isMusic() {
        return music;
    }

    /**
     * @param music the music to set
     */
    public void setMusic(boolean music) {
        this.music = music;
    }
}

//HUGE ISSUE: if SimpleSound is placed in multiple sources with different priorities,
//how does the manager know which priority to check against?
//SOLUTION: add method play(....) which allows for a one-off, otherwise you need to use XSound
//

//child.setLooping(true);
//parent.setLooping(false); //child.isLooping will return false!

//child.setPosition(0, 5);
//parent.setPosition(5, 5);
//child position => (5, 10)

//child.setPosition(5, 0);
//parent.setPosition(0, 0);
//child.setVolume(0.5f);
//parent.setVolume(0.5f);
//parent.setRelative(true);
//relative => true
//volume => .25f
//position => (5, 0) relative to LISTENER



//Mix with parent: Volume, Pitch, X, Y, Depth
//Use parent if <= 0: Radius
//Use parent: the rest