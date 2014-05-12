package soundly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Matt
 */
public class SoundGroup extends ManagedSound {
    
    private List<ManagedSound> sounds;
    
    /**
     * 
     * @param description
     * @param sounds
     */
    public SoundGroup(String description, ManagedSound ... sounds) {
        super(description);
        this.sounds = Arrays.asList(sounds);
    }
    
    public SoundGroup(ManagedSound ... sounds) {
        this(null, sounds);
    }
    
    
    /**
     * 
     * @param size
     */
    public SoundGroup(int size) {
        this(null, size);
    }
    
    /**
     * 
     * @param description
     * @param size
     */
    public SoundGroup(String description, int size) {
        super(description);
        sounds = new ArrayList<ManagedSound>(size);
    }
    
    /**
     * 
     * @param description
     */
    public SoundGroup(String description) {
        this(description, 5);
    }
    
    /**
     * 
     */
    public SoundGroup() {
        this((String)null);
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public boolean addSound(ManagedSound s) {
        s.setPosition(getX(), getY(), getDepth());
        s.parent = this;
        return this.sounds.add(s);
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public ManagedSound getSound(int index) {
        return sounds.get(index);
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public boolean removeSound(ManagedSound s) {
        s.parent = null;
        return sounds.remove(s);
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public ManagedSound removeSound(int index) {
        ManagedSound s = sounds.remove(index);
        s.parent = null;
        return s;
    }
    
    /**
     * 
     */
    public void clear() {
        for (int i=0; i<size(); i++) {
            getSound(i).parent = null;
        }
        sounds.clear();
    }
    
    /**
     * 
     * @return
     */
    public int size() {
        return sounds.size();
    }
    
    /**
     * 
     * @return
     */
    public int rnd() {
        return (int)(Math.random()*size());
    }
    
    //Queues a random source at the next available index
    public int queue() {
        return queueAtIndex(-1);
    }

    //Queues a random source at the given index
    public int queueAtIndex(int sourceIndex) {
        return getSound(rnd()).queueAtIndex(sourceIndex);
    }

    //Queues the given source at the next available index
    public int queueSound(int index) {
        return queueSoundAtIndex(index, -1);
    }
    
    //Queues the given source at the given index
    public int queueSoundAtIndex(int index, int sourceIndex) {
        return getSound(index).queueAtIndex(sourceIndex);
    }
}