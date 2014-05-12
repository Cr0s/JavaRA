package soundly;

import java.io.InputStream;
import java.net.URL;

import org.newdawn.slick.SlickException;

//TODO: allow changing listener orientation for platformers, so that sounds from above appear to come from above
//TODO: add stop() and similar things for SoundGroup
//TODO: remove all OpenAL calls PER FRAME!!!

/**
 * 
 * 
 * @author Matt
 */
public class XSound extends ManagedSound {
    
    AudioData audio;
    
    //hackish, but fast...
    float lastVolume = 1f;
    float lastPitch = 1f;
    boolean lastLooping = false;
    boolean lastRelative = false;
    float lastX = 0f;
    float lastY = 0f;
    float lastDepth = 0f;
    
    int source = -1;
    int sourceIndex = -1;
    boolean inQueue = false;
    private boolean fading = false;
    
    /** Start gain for fading in/out */
    private float fadeStartGain;
    /** End gain for fading in/out */
    private float fadeEndGain;
    /** Countdown for fading in/out */
    private int fadeTime;
    /** Duration for fading in/out */
    private int fadeDuration;
    /** True if music should be stopped after fading in/out */
    private boolean stopAfterFade;
    
    private int defaultIndex = -1;
    
    
    protected XSound() {
    }
    
    /**
     * 
     * @param audio
     * @param description
     */
    public XSound(AudioData audio, String description) {
        super(description);
        this.audio = audio;
    }
    
    /**
     * 
     * @param audio
     */
    public XSound(AudioData audio) {
        this(audio, null);
    }
    
    /**
     * 
     * @param url
     * @param description
     * @throws SlickException
     */
    public XSound(URL url, String description) throws SlickException {
        this(Soundly.loadSound(url), description);
    }

    public XSound(String ref, InputStream in) throws SlickException {
        this(Soundly.loadWavSound(ref, in), ref);
    }
        
    
    /**
     * 
     * @param url
     * @throws SlickException
     */
    public XSound(URL url) throws SlickException {
        this(url, url.getFile());
    }
    
    /**
     * 
     * @param ref
     * @param description
     * @throws SlickException
     */
    public XSound(String ref, String description) throws SlickException {
        this(Soundly.loadSound(ref), description);
    }
    
    /**
     * 
     * @param ref
     * @throws SlickException
     */
    public XSound(String ref) throws SlickException {
        this(ref, ref);
    }
    
    /**
     * 
     * @return
     */
    public AudioData getAudio() {
        return audio;
    }
    
    public void setDefaultIndex(int sourceIndex) {
        defaultIndex = sourceIndex;
    }
    
    public int getDefaultIndex() {
        return defaultIndex;
    }
    
    public int getSource() {
        return source;
    }
    
    public int getSourceIndex() {
        return sourceIndex;
    }
    
    /** If subclasses want to loop by rewinding (e.g. for streams), return false. */
    protected boolean sourceLooping() {
        return isLooping();
    }
    
    protected void updateOpenAL(int source, int sourceIndex, int delta, boolean init) {
        boolean stopped = false;
        if (!inQueue && fadeTime > 0) {
            fadeTime -= delta;
            if (fadeTime <= 0) {
                fadeTime = 0;
                fading = false;
                if (stopAfterFade) {
                    stopped = true;
                    stop();
                }
            }
            if (!stopped) {
                float offset = (fadeEndGain - fadeStartGain) * (1 - (fadeTime / (float) fadeDuration));
                float v = fadeStartGain + offset;
                if (v<0)
                    v = 0f;
                setVolume(v);
            }
        }
        
        final Mix GLOBAL_MIX = Soundly.GLOBAL_MIX;
        
        float volume = mixedVolume() * GLOBAL_MIX.getVolume();
        float pitch = mixedPitch() * GLOBAL_MIX.getPitch();
        float x = mixedX() + GLOBAL_MIX.getXAdjustment();
        float y = mixedY() + GLOBAL_MIX.getYAdjustment();
        float depth = mixedDepth() + GLOBAL_MIX.getDepthAdjustment();
        boolean relative = isRelative();
        boolean looping = sourceLooping();
        
        //To minimize OpenAL calls...
        //update each property only if it has changed since last update, OR if 
        //we are initializing the source
        if (init || lastVolume!=volume)
            Soundly.setSourceVolume(source, volume);
        if (init || lastPitch!=pitch)
            Soundly.setSourcePitch(source, pitch);
        if (init || (lastX!=x || lastY!=y || lastDepth!=depth))
            Soundly.setSourcePosition(source, x, y, depth);
        if (init || lastRelative!=relative)
            Soundly.setSourceRelative(source, relative);
        if (init || lastLooping!=looping)
            Soundly.setSourceLooping(source, looping);
        
        lastVolume = volume;
        lastPitch = pitch;
        lastX = x;
        lastY = y;
        lastDepth = depth;
        lastRelative = relative;
        lastLooping = looping;
    }
    
    
    
    public boolean isPlaying() {
        return Soundly.isSourcePlaying(getSource());
    }
    
    public boolean isStopped() {
        return Soundly.isSourceStopped(getSource());
    }
    
    public boolean isPaused() {
        return Soundly.isSourcePaused(getSource());
    }
    
    public boolean seek(float position) {
        position = position % getAudio().getLength();
        return Soundly.sourceSeek(getSource(), position);
    }
    
    public float getSeekPosition() {
        return Soundly.getSourceSeekPosition(getSource());
    }
    
    public void stop() {
        Soundly.sourceStop(getSource());
    }
    
    public void rewind() {
        Soundly.sourceRewind(getSource());
    }
    
    public boolean resume() {
        if (!isPlaying()) {
            queue();
            return true;
        }
        return false;
    }
    
    public void pause() {
        Soundly.sourcePause(getSource());
    }
    
    public void clear() {
        Soundly.get().clearSound(this);
    }
    
    public int queue() {
        return queueAtIndex(-1);
    }
    
    public int queueAtIndex(int sourceIndex) {
        Soundly m = Soundly.get();
        //if this source is playing
        if (getSourceIndex() != -1 && m.isSourcePlaying(getSource())) {
            stop();
            //if no index was given, allow us to re-use the current index
            if (sourceIndex==-1)
                sourceIndex = getSourceIndex();
        }
        if (sourceIndex==-1)
            sourceIndex = getDefaultIndex();
        this.sourceIndex = m.queueAtIndex(sourceIndex, this);
        this.source = m.getSource(this.sourceIndex);
        return this.sourceIndex;
    }
    
    void clearSource() {
        stop();
        fading = false;
        inQueue = false;
        source = -1;
        sourceIndex = -1;
    }
    
    public boolean isFading() {
        return fading;
    }

    /**
     * Fade this music to the volume specified
     * 
     * @param duration Fade time in milliseconds.
     * @param endVolume The target volume
     * @param stopAfterFade True if music should be stopped after fading in/out
     */
    public void fade(int duration, float endVolume, boolean stopAfterFade) {
        this.stopAfterFade = stopAfterFade;
        fadeStartGain = getVolume();
        fadeEndGain = endVolume;
        fadeDuration = duration;
        fadeTime = duration;
        fading = true;
    }

    
}
