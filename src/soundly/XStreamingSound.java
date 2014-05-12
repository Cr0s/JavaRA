/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundly;

import ibxm.Module;
import ibxm.OpenALMODPlayer;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.OpenALException;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;
import soundly.impl.XOpenALStreamPlayer;

/**
 * XStream is like XSound except that a source index must be given 
 * in the constructor. To play an XStream at a different source,
 * you must use setDefaultIndex or reset (which will stop playback and create a new 
 * OpenALStreamPlayer for the given source).
 * 
 * @author Matt
 */
public class XStreamingSound extends XSound {
    
    /** The player we're going to ask to stream data */
    private XOpenALStreamPlayer streamPlayer;
    /** The MOD play back system */
    private static OpenALMODPlayer modPlayer;
    /** The module to play back */
    private Module module;
    
    private String ref;
    private URL url;
    boolean stopped = true;
    
    /** Only OGG streams and MOD sounds are currently accepted. */
    public XStreamingSound(String ref, String description) throws SlickException {
        super(new AudioData(), description);
        SoundStore.get().init();
        this.ref = ref;
        try {
            if (ref.toLowerCase().endsWith(".ogg")) {
                streamPlayer = new XOpenALStreamPlayer(source, ref);
            } else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
                if (SoundStore.get().isDeferredLoading())
                    Log.warn("Deferred loading for MOD is not supported; loading immediately");
                modPlayer = new OpenALMODPlayer();
                module = OpenALMODPlayer.loadModule(ResourceLoader.getResourceAsStream(ref));
            } else {
                throw new SlickException("not a valid stream format, must be: .xm, .mod or .ogg");
            }
        } catch (IOException e) {
            Log.error(e);
            throw new SlickException("Failed to load sound: " + ref, e);
        }
    }
    
    /** Only OGG streams and MOD sounds are currently accepted. */
    public XStreamingSound(URL url, String description) throws SlickException {
        super(new AudioData(), description);
        SoundStore.get().init();
        String ref = url.getFile();
        this.url = url;
        try {
            if (ref.toLowerCase().endsWith(".ogg")) {
                streamPlayer = new XOpenALStreamPlayer(source, url);
            } else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
                if (SoundStore.get().isDeferredLoading())
                    Log.warn("Deferred loading for MOD is not supported; loading immediately");
                modPlayer = new OpenALMODPlayer();
                module = OpenALMODPlayer.loadModule(url.openStream());
            } else {
                throw new SlickException("not a valid stream format, must be: .xm, .mod or .ogg");
            }
        } catch (IOException e) {
            Log.error(e);
            throw new SlickException("Failed to load sound: " + ref, e);
        }
    }
    
    public XStreamingSound(String ref) throws SlickException {
        this(ref, null);
    }
    
    public XStreamingSound(URL url) throws SlickException {
        this(url, null);
    }
    
    public boolean sourceLooping() {
        return false;
    }
    
    public void setLooping(boolean looping) {
        super.setLooping(looping);
        if (streamPlayer!=null)
            streamPlayer.setLooping(looping);
    }
    
    public void clearSource() {
        clearStream();
        super.clearSource();
    }
    
    void clearStream() {
        stop();
        int source = getSource();
        if (source==-1)
            return;
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
        while (queued > 0) {
            AL10.alSourceUnqueueBuffers(source, buffer);
            queued--;
        }
        Soundly.setSourceAudio(source, 0);
    }
    
    public boolean isFinished() {
        if (modPlayer!=null)
            return modPlayer.done();
        else if (streamPlayer!=null)
            return streamPlayer.done();
        return false;
    }
    
    void startStream() {
        stopped = false;
        if (streamPlayer!=null) {
            try {
                streamPlayer.play();
            } catch (IOException e) {
                stopped = true;
                Log.error("Failed to read OGG source: "+streamPlayer.getSource());
            }
        } else if (module!=null && modPlayer!=null) {
            modPlayer.play(module, getSource(), isLooping(), false);
        }
    }
    
    protected void updateOpenAL(int source, int sourceIndex, int delta, boolean init) {
        super.updateOpenAL(source, sourceIndex, delta, init);
        
        if (!stopped && modPlayer != null) {
            try {
                modPlayer.update();
            } catch (OpenALException e) {
                Log.error("Error with OpenGL MOD Player on this this platform");
                Log.error(e);
            }
        }
        if (!stopped && streamPlayer != null) {
            try {
                streamPlayer.update();
            } catch (OpenALException e) {
                Log.error("Error with OpenGL Streaming Player on this this platform");
                Log.error(e);
                streamPlayer = null;
            }
        }
    }
    
    public void stop() {
        super.stop();
        stopped = true;
    }
    
    public boolean resume() {
        boolean b = super.resume();
        stopped = false;
        return b;
    }
    
    public boolean isPlaying() {
        if (stopped)
            return false;
        return super.isPlaying();
    }
    
    public int queueAtIndex(int sourceIndex) {
        if (modPlayer!=null || streamPlayer!=null) {
            int ret = super.queueAtIndex(sourceIndex);
            if (streamPlayer!=null)
                streamPlayer.setSource(this.source);
            return ret;
        }
        return -1;
    }
    
    public boolean seek(float position) {
        if (streamPlayer!=null) {
            return streamPlayer.setPosition(position);
        } else if (modPlayer!=null && module!=null) {
            throw new RuntimeException("seeking is unsupported with MOD sounds");
        } else
            return false;
    }
    
    public float getSeekPosition() {
        if (streamPlayer!=null) {
            return streamPlayer.getPosition();
        } else if (modPlayer!=null && module!=null) {
            throw new RuntimeException("seeking is unsupported with MOD sounds");
        } else
            return -1;
    }
}
