package soundly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioImpl;
import org.newdawn.slick.openal.DeferredSound;
import org.newdawn.slick.openal.NullAudio;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.openal.WaveData;
import org.newdawn.slick.util.Log;

//Every queued sound has an update() method
//Remove play() method, because it doesn't actually PLAY a sound it only queues it
//All other methods can be set with setters, though, then updated on every frame


/**
 *
 * @author Matt
 */
public class Soundly {

    private static class QueuedAudio {
	XSound sound;
    }

    //a low number, but sufficient if the system is well managed
    /**
     * 
     */
    public static final int DEFAULT_MAX_SOURCES = 16;
    public static final Mix GLOBAL_MIX = new Mix("global_mix");
    public static final int ANY_INDEX = -1;

    private static Soundly instance = new Soundly();
    /** The buffer used to set the position of a source */
    private static FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3);

    /**
     * 
     */
    public static final float DEFAULT_SCALE = 0.02f;
    private static float scale = DEFAULT_SCALE;

    public static void setDeferredLoading(boolean deferred) {
	SoundStore.get().setDeferredLoading(deferred);
    }

    public static boolean isDeferredLoading() {
	return SoundStore.get().isDeferredLoading();
    }

    public static AudioData loadSound(String ref) throws SlickException {
	SoundStore.get().init();
	Audio audio = null;
	try {
	    if (ref.toLowerCase().endsWith(".ogg")) {
		audio = SoundStore.get().getOgg(ref);
	    } else if (ref.toLowerCase().endsWith(".wav")) {
		audio = SoundStore.get().getWAV(ref);
	    } else if (ref.toLowerCase().endsWith(".aif")) {
		audio = SoundStore.get().getAIF(ref);
	    } else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
		throw new SlickException("Use XStream for streaming .xm and .mod sounds");
	    } else {
		throw new SlickException("Only .xm, .mod, .aif, .wav and .ogg are currently supported.");
	    }
	} catch (Exception e) {
	    Log.error(e);
	    throw new SlickException("Failed to load sound: " + ref, e);
	}
	return new AudioData(audio.getBufferID());
    }

    public static AudioData loadSound(URL url) throws SlickException {
	SoundStore.get().init();
	String ref = url.getFile();
	Audio audio = null;
	try {
	    if (ref.toLowerCase().endsWith(".ogg")) {
		audio = SoundStore.get().getOgg(url.openStream());
	    } else if (ref.toLowerCase().endsWith(".wav")) {
		audio = SoundStore.get().getWAV(url.openStream());
	    } else if (ref.toLowerCase().endsWith(".aif")) {
		audio = SoundStore.get().getAIF(url.openStream());
	    } else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
		throw new SlickException("Use XStream for streaming .xm and .mod sounds");
	    } else {
		throw new SlickException("Only .xm, .mod, .aif, .wav and .ogg are currently supported.");
	    }
	} catch (Exception e) {
	    Log.error(e);
	    throw new SlickException("Failed to load sound: " + ref, e);
	}
	return new AudioData(audio.getBufferID());
    }

    public static AudioData loadWavSound(String ref, InputStream in) throws SlickException {
	AudioData audio = null;
	try {
	    audio = loadAudioFromMemory(ref, in);
	} catch (Exception e) {
	    Log.error(e);
	    throw new SlickException("Failed to load sound: " + ref, e);
	}

	return audio;
    }    

    private static AudioData loadAudioFromMemory(String ref, InputStream in) throws IOException {

	int buffer = -1;
	try {
	    IntBuffer buf = BufferUtils.createIntBuffer(1);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    int nRead;
	    byte[] data = new byte[16384];

	    while ((nRead = in.read(data, 0, data.length)) != -1) {
		baos.write(data, 0, nRead);
	    }

	    baos.flush();

	    
	    ByteBuffer bb = ByteBuffer.allocateDirect(baos.size());
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.put(baos.toByteArray());
	    bb.rewind();
	    
	    AL10.alGenBuffers(buf);
	    AL10.alBufferData(buf.get(0), AL10.AL_FORMAT_MONO16, bb, 22050);

	    buffer = buf.get(0);
	} catch (Exception e) {
	    Log.error(e);
	    IOException x = new IOException("Failed to load: "+ref);
	    x.initCause(e);

	    throw x;
	}

	if (buffer == -1) {
	    throw new IOException("Unable to load: "+ref);
	}

	return new AudioData(buffer);	
    }

    /**
     * 
     * @param scale
     */
    public static void setScale(float scale) {
	Soundly.scale = scale;
    }

    /**
     * 
     * @return
     */
    public static float getScale() {
	return scale;
    }

    static boolean is2DCollision(float x1, float y1, float r1, float x2, float y2, float r2) {
	final float a = r1 + r2;
	final float dx = x1 - x2;
	final float dy = y1 - y2;
	return a * a > (dx * dx + dy * dy);
    }


    /**
     * 
     * @param sound
     * @return
     */
    public static float calculateDistance(ManagedSound sound) {
	Soundly mgr = Soundly.get();
	return calculateDistance(sound.getX(), sound.getY(), mgr.getListenerX(), mgr.getListenerY());
    }

    /* Returns the distance from the listener in pixels. */
    /**
     * 
     * @param sX
     * @param sY
     * @param mX
     * @param mY
     * @return
     */
    public static float calculateDistance(float sX, float sY, float mX, float mY) {
	// Calculate the source's distance from the listener:
	float dX = sX - mX;
	float dY = sY - mY;
	return (float)Math.sqrt(dX * dX + dY * dY);
    }

    /* Returns the proximity from the center, where 0% means "at center" and
     * 100% means "not at center". If either sound has no radius, 0f is returned. */
    /**
     * 
     * @param sound
     * @param distance
     * @param listenerRadius
     * @return
     */
    public static float calculateFromCenter(ManagedSound sound, float distance, float listenerRadius) {
	float sr = sound.getRadius();
	float msr = listenerRadius;
	if (sr!=-1 && msr!=-1)
	    return (distance)/(sr+listenerRadius);
	else
	    return 0f;
    }

    /**
     * 
     * @param sound
     * @param distance
     * @return
     */
    public static float calculateFromCenter(ManagedSound sound, float distance) {
	return calculateFromCenter(sound, distance, Soundly.get().getListenerRadius());
    }

    /**
     * 
     * @return
     */
    public static Soundly get() {
	return instance;
    }

    /** The buffer used to set the velocity of a source */
    private FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
    /**
     * Position of the listener in 3D space.
     */
    private FloatBuffer listenerPos = null;
    //private FloatBuffer listenerOri = null;

    private float listenerX, listenerY;
    /**
     * 
     */
    protected float listenerDepth;
    private float radius = 0f;

    //if not specified, the default proximity radius will be used. otherwise this will be the
    //limit at which distance priority is taken into account
    //to disable distance priority altogether and rely only on LEVEL and PRIORITY, 
    //use distanceCheckingEnabled = false
    private float distanceRadius = -1;
    private boolean distanceCheckingEnabled = false;

    private boolean proximity = true;
    private boolean inited = false;
    private int sourceCount = DEFAULT_MAX_SOURCES;

    private XSound[] sources;
    private QueuedAudio[] queue;

    /* Slick's sounds (i.e. non-music) start at index 0. */
    private int musicTrackCount = 1;

    /** 
     * Clears the sound engine (stopping all managed sounds, clearing all 
     * sources) and clears Slick's SoundStore. Both SoundManager and SoundStore
     * will expect an init() call after this.
     * 
     * 
     */
    public void clear() {
	//stop the sources
	for (int i=0; i<sources.length; i++) {
	    if (sources[i]!=null) {
		sources[i].stop();
		sources[i].clearSource();
	    }
	}
	//clear the sound store
	SoundStore.get().clear();
	//delete the sources array
	queue = null;
	sources = null;
	inited = false;
    }

    /**
     * 
     */
    public void init() {
	init(DEFAULT_MAX_SOURCES);
    }

    /**
     * 
     * @param maxSources
     */
    public void init(int maxSources) {
	if (inited)
	    return;

	sourceCount = maxSources;
	SoundStore.get().setMaxSources(maxSources);
	sources = new XSound[maxSources];
	queue = new QueuedAudio[maxSources];
	for (int i=0; i<sourceCount; i++) {
	    queue[i] = new QueuedAudio();
	}

	SoundStore.get().init();

	//setup listener position buffer
	listenerPos = BufferUtils.createFloatBuffer(3).put( 
		new float[] { listenerX, listenerDepth, listenerY } );
	listenerPos.flip();
	AL10.alListener(AL10.AL_POSITION, listenerPos);

	sourceVel.clear();
	sourceVel.put(new float[] { 0, 0, 0 });
	sourceVel.flip();

	//listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
	//listenerOri.flip();

	inited = true;
    }

    /**
     * 
     * @param index
     * @return
     */
    public int getSource(int index) {
	return SoundStore.get().getSource(index);
    }

    /*
     * If both sounds are on the same level, priority is followed as such:
     * 
     */

    public int findBestSourceFor(ManagedSound sound) {
	boolean music = sound.isMusic();
	int musicTracks = getMusicTrackCount();
	int start = music&&musicTracks!=0 ? 0 : musicTracks;
	int end = music&&musicTracks!=0 ? musicTracks : sources.length;
	return findBestSourceFor(sound, start, end);
    }

    /**
     * 
     * @param sound
     * @return
     */
    public int findBestSourceFor(ManagedSound sound, int startIndex, int endIndex) {
	if (startIndex < 0 || endIndex > sources.length)
	    throw new IllegalArgumentException("invalid start/end indices for findBestSource "+startIndex+" "+endIndex);

	int bestSoundIndex = -1;
	int bestSoundDistanceIndex = -1;

	float lowestPriority = sound.getPriority();
	int lowestLevel = sound.getLayer();
	float highestProximity = -1;

	//TODO: do one more loop through with paused sounds? maybe, maybe not.

	for (int i=startIndex; i<endIndex; i++) {
	    //if there is no sound queued for this source, then we check the
	    //sound last occupied here.
	    //if there is a sound queued for this source, we compare against
	    //that.
	    ManagedSound queuedSource = queue[i].sound;

	    //if the source is NOT in queue, we can check to see if the source
	    //is readily available (i.e. not playing anything). if so, return
	    //immediately
	    if (queuedSource==null) {
		int state = AL10.alGetSourcei(getSource(i), AL10.AL_SOURCE_STATE);
		if ((state != AL10.AL_PLAYING) && (state != AL10.AL_PAUSED)) {
		    return i; //the source is available, so use it
		}
	    }

	    //try to use the queued source, otherwise use the currently playing
	    ManagedSound oSound = queuedSource;
	    if (oSound==null)
		oSound = sources[i];
	    float rad = getDistanceRadius()<=0 ? getListenerRadius() : getDistanceRadius();


	    if (oSound==null) { //if there are no XSounds assigned to the source
		return i;
	    } else if (oSound.isReplaceable()) { //if the sound is being managed...
		//if the incoming sound is of greater or equal level to the other 
		//sound, then we may be able to make a switch
		//(but if the compared sound is a higher level than something 
		//we've already checked, just ignore)
		if (sound.getLayer() >= oSound.getLayer() && oSound.getLayer()<=lowestLevel) {
		    lowestLevel = oSound.getLayer();
		    //if the incoming sound has a greater or equal priority, we may make a switch
		    //but if we've already seen a lower priority than this, just ignore
		    if (sound.getPriority() >= oSound.getPriority() 
			    && oSound.getPriority()<=lowestPriority) {
			lowestPriority = oSound.getPriority();
			bestSoundIndex = i;
		    }
		}


		//check distance: if other sound is out of range, we can use it
		if (distanceCheckingEnabled && rad>0f) {
		    //ignore if relative flag is set
		    boolean ignore = oSound.isRelative() || oSound.getRadius()<=0f;
		    //ignore if currently playing source is relative
		    if (queuedSource==null && sources[i]!=null) {
			//TODO: check?
			ignore = sources[i].isRelative();
		    }
		    if (!ignore) {
			float dist = Soundly.calculateDistance(oSound);
			float prox = Soundly.calculateFromCenter(oSound, dist, rad);
			//if the proximity is out of earshot (100%)
			if (prox>1f && prox>highestProximity) {
			    highestProximity = prox;
			    bestSoundIndex = i;
			}
		    }
		}
	    }
	}

	return bestSoundIndex;
    }

    public void setListenerX(float x) {
	setListenerPosition(x, getListenerY(), getListenerDepth());
    }

    public void setListenerY(float y) {
	setListenerPosition(getListenerX(), y, getListenerDepth());
    }

    public void setListenerDepth(float depth) {
	setListenerPosition(getListenerX(), getListenerY(), depth);
    }

    /**
     * 
     * @param x
     * @param y
     */
    public void setListenerPosition(float x, float y) {
	setListenerPosition(x, y, 0f);
    }

    //moves the listener and updates the position (calling positionChanged())
    /**
     * 
     * @param x
     * @param y
     * @param depth
     */
    public void setListenerPosition(float x, float y, float depth) {
	float oldX = this.listenerX;
	float oldY = this.listenerY;
	float oldZ = this.listenerDepth;
	this.listenerX = x;
	this.listenerY = y;
	this.listenerDepth = depth;

	if (oldX!=x||oldY!=y||oldZ!=depth) {
	    float s = getScale();
	    listenerPos.put( 0, x*s );
	    listenerPos.put( 1, depth*s );
	    listenerPos.put( 2, y*s );
	    //TODO: make float buffer changing more consistent between this and sourcePos
	    AL10.alListener( AL10.AL_POSITION, listenerPos);
	    distanceChanged();
	}
    }

    //call this whenever alListener AL_POSITION has changed
    /**
     * 
     * @return
     */
    public float distanceChanged() {
	//TODO: update ALL audio, or just audio starting after startIndex?

	//Since the listener position has changed, update the rolloffs
	for (int i=getMusicTrackCount(); i<sources.length; i++) {
	    //any static sounds that are playing should be moved with the listener...
	    //notify "positionChanged" which will recalculate with new listener pos
	    if (sources[i]!=null) {
		sources[i].distanceChanged();
	    }
	}
	return 0f;
    }

    /**
     * 
     * @param source
     */
    public static void sourcePlay(int source) {
	if (source==-1)
	    return;
	AL10.alSourcePlay(source);
    }

    /**
     * 
     * @param source
     */
    public static void sourcePause(int source) {
	if (source==-1)
	    return;
	AL10.alSourcePause(source);
    }

    /**
     * 
     * @param source
     */
    public static void sourceStop(int source) {
	if (source==-1)
	    return;
	AL10.alSourceStop(source);
    }

    /**
     * 
     * @param source
     */
    public static void sourceRewind(int source) {
	if (source==-1)
	    return;
	AL10.alSourceRewind(source);
    }

    /**
     * 
     * @param source
     * @param audio
     */
    public static void setSourceAudio(int source, int buffer) {
	if (source==-1)
	    return;
	AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
    }

    /**
     * 
     * @param source
     * @return
     */
    public static int getSourceAudio(int source) {
	if (source==-1)
	    return -1;
	return AL10.alGetSourcei(source, AL10.AL_BUFFER);
    }

    /**
     * 
     * @param source
     * @param pitch
     */
    public static void setSourcePitch(int source, float pitch) {
	if (source==-1)
	    return;
	AL10.alSourcef(source, AL10.AL_PITCH, pitch);
    }

    /**
     * 
     * @param source
     * @param loop
     */
    public static void setSourceLooping(int source, boolean loop) {
	if (source==-1)
	    return;
	AL10.alSourcei(source, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * 
     * @param source
     * @return
     */
    public static boolean isSourceLooping(int source) {
	if (source==-1)
	    return false;
	return AL10.alGetSourcei(source, AL10.AL_LOOPING) == AL10.AL_TRUE;
    }

    /**
     * 
     * @param source
     * @param relative
     */
    public static void setSourceRelative(int source, boolean relative) {
	if (source==-1)
	    return;
	AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * 
     * @param source
     * @return
     */
    public static boolean isSourceRelative(int source) {
	return AL10.alGetSourcei(source, AL10.AL_SOURCE_RELATIVE) == AL10.AL_TRUE;
    }

    /** 
     * Note that the volume that will eventually go the OpenAL source will
     * be multiplied by SoundStore.get().getSoundVolume() -- so do not rely on
     * getSourceVolume to be the same as what you earlier set with setSourceVolume.
     * @param source
     * @param gain  
     */
    public static void setSourceVolume(int source, float gain) {
	if (source==-1)
	    return;
	if (gain <= 0) 
	    gain = 0f;
	AL10.alSourcef(source, AL10.AL_GAIN, gain);
    }

    /** 
     * Note that the volume that will eventually go the OpenAL source will
     * be multiplied by SoundStore.get().getSoundVolume() -- so do not rely on
     * getSourceVolume to be the same as what you earlier set with setSourceVolume.
     * @param source 
     * @return 
     */
    public static float getSourceVolume(int source) {
	if (source==-1)
	    return 1f;
	return AL10.alGetSourcef(source, AL10.AL_GAIN);
    } 

    /**
     * 
     * @param source
     * @param x
     * @param y
     * @param depth
     */
    public static void setSourcePosition(int source, float x, float y, float depth) {
	if (source==-1)
	    return;
	float s = getScale();
	sourcePos.clear();
	sourcePos.put(new float[] { x*s, depth*s, y*s });
	sourcePos.flip();
	AL10.alSource(source, AL10.AL_POSITION, sourcePos);
    }

    public static boolean sourceSeek(int source, float secOffset) {
	if (source==-1)
	    return false;
	AL10.alSourcef(source, AL11.AL_SEC_OFFSET, secOffset);
	if (AL10.alGetError() != 0) {
	    return false;
	}
	return true;
    }

    /**
     * Returns -1 if source does not exist.
     */
    public static float getSourceSeekPosition(int source) {
	if (source==-1)
	    return -1;
	return AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET);
    }

    /**
     * 
     * @param source
     * @return
     */
    public static boolean isSourcePlaying(int source) {
	if (source==-1)
	    return false;
	return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    /**
     * 
     * @param source
     * @return
     */
    public static boolean isSourcePaused(int source) {
	if (source==-1)
	    return false;
	return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED;
    }

    /**
     * 
     * @param source
     * @return
     */
    public static boolean isSourceStopped(int source) {
	if (source==-1)
	    return false;
	return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED;
    }

    public void clearSound(XSound sound) {
	int i = sound.getSourceIndex();
	if (i!=-1) {
	    if (queue[i].sound==sound) {
		queue[i].sound = null;
		sound.inQueue = false;
	    } else if (sources[i]==sound) {
		sources[i] = null;
	    }
	}
	sound.stop();
	sound.clearSource();   
    }

    //USED INTERNALLY
    public int queue(XSound sound) {
	return queueAtIndex(-1, sound);
    }

    //USED INTERNALLY
    public int queueAtIndex(int sourceIndex, XSound sound) {
	if (!inited) 
	    throw new RuntimeException("make sure to init() the SoundManager before trying to use it to play");
	if (SoundStore.get().soundWorks() && SoundStore.get().soundsOn()) {
	    //first check to see if we've globally enabled proximity checking...
	    /*if (isProximityEnabled()) {
                //if so, do another check for the sound...
                //if sound's proximity is enabled, then disable the sound as soon as it's
                //outside of range.
                if (sound instanceof Audio2D
                        && ((Audio2D)sound).isProximityEnabled() 
                        && !((Audio2D)sound).isWithinEarshot()) {
                    //make sure to disable the sound!
                    sound.stop();
                    return -1;
                }
            }*/
	    //TODO: source might be in queue, but not yet playing!
	    //if we are to only play once, and the last source exists and is playing...


	    //find the index of the best openAL source
	    int nextSource = sourceIndex<0 ? findBestSourceFor(sound) : sourceIndex;
	    if (nextSource == -1) { 
		//if no source is available, return -1
		return -1;
	    }
	    queue[nextSource].sound = sound;
	    sound.inQueue = true;

	    //if it's a stream, stop & clear any queued buffers before playing
	    if (sound instanceof XStreamingSound) {
		XStreamingSound x = ((XStreamingSound)sound);
		x.clearStream();
	    }

	    return nextSource;
	}
	return -1;
    }

    //queue all sounds and play them with Update. this would solve
    //many calls to LWJGL. if we try to play 100 sounds from the same source,
    //only 1 of them will actually be played in the end


    //called to update the engine and play any queued sounds
    /**
     * 
     */
    public void update(int delta) {
	//go through the sources:
	//if a sound is queued, load it into OpenAL
	//else if OpenAL is playing a source, update it
	for (int sourceIndex=0; sourceIndex<queue.length; sourceIndex++) {
	    QueuedAudio a = queue[sourceIndex];
	    XSound sound = a.sound;
	    //get the source ID we will be using
	    int source = getSource(sourceIndex);

	    //a sound exists in the queue, so play it
	    if (sound!=null) {
		//if we are replacing a managed source, notify it
		if (sources[sourceIndex]!=null && sources[sourceIndex]!=sound) {
		    sources[sourceIndex].replaceEvent(sound, sourceIndex);
		    sources[sourceIndex].clearSource();
		}

		//stop the source if it is currently playing
		sourceStop(source);

		//set up some OpenAL params that ManagedSound doesn't handle
		if (!(sound instanceof XStreamingSound)) {
		    setSourceAudio(source, sound.getAudio().getBufferID());
		}
		AL10.alSource(source, AL10.AL_VELOCITY, sourceVel);

		//tell the sound to update its own sources (and that we are initializing!)
		sound.updateOpenAL(source, sourceIndex, delta, true);

		//AL10.alSourcei(source, AL10.AL_REFERENCE_DISTANCE, 1);
		//AL10.alSourcei(source, AL10.AL_MAX_DISTANCE, 5);

		if (sound instanceof XStreamingSound) {
		    ((XStreamingSound) sound).startStream();
		} else {
		    sourcePlay(source);
		}
		sources[sourceIndex] = sound;
		sound.inQueue = false;
	    } else if (sources[sourceIndex]!=null) {
		//updates the sound
		//note that sound is told to update even if it's playing...
		//but not a big issue since sounds only call OpenAL when needed
		sources[sourceIndex].updateOpenAL(source, sourceIndex, delta, false);
	    }
	    //clear sound from queue
	    queue[sourceIndex].sound = null;
	}
    }



    //OPTION A:
    //OpenAL calls from ManagedSource... not pretty
    //OpenAL calls from SourceManager... also not pretty


    //TODO: true attenuation
    //TODO: listener orientation
    //TODO: allow for full 3D
    //TODO: doppler effect



    //ISSUE: if we are updating() all the positions, looping, etc.
    //then calls to play(x, y, looping, volume) whatever will be
    //overridden by a following setPosition

    //ISSUE 2: allow for the following:
    //  sound.queue();
    //  sound.setPosition(x, y);
    //  manager.update();
    //using only a SINGLE call to OpenAL on update()

    //ISSUE 3: how do you get isPlaying() with a blind Sound2D?
    // option A: check every frame using update()
    // option B: store one of the lastIndices (but that doesn't cover ALL, just the last!)
    // option C: extend Sound2D for single-source sounds

    //a. remove play(x,y/etc)
    //b. SimpleSound class:
    //does not know its own source, and therefore you can't do isPlaying
    //when you queue it, it plays the sound at another source even if already playing
    //unless setAutoUpdate is true, then it will 
    //all the rest is the same as Sound2D. 
    //c. 



    //Option 1: 
    //ManagedAudio's update() method is also used to init the OpenAL source
    //remove play(x,y/etc) from Sound2D
    //But what about a Playable 
    //Option 2:

    /**
     * 
     * @param radius
     */
    public void setListenerRadius(float radius) {
	this.radius = radius;
	distanceChanged();
    }

    /**
     * 
     * @return
     */
    public float getListenerRadius() {
	return radius;
    }

    /**
     * 
     * @return
     */
    public float getListenerX() {
	return listenerX;
    }

    /**
     * 
     * @return
     */
    public float getListenerY() {
	return listenerY;
    }

    /**
     * 
     * @return
     */
    public float getListenerDepth() {
	return listenerDepth;
    }


    /**
     * 
     * @param index
     * @return
     */
    public XSound getSoundAt(int index) {
	if (index>=0&&index<sources.length)
	    return sources[index];
	return null;
    }

    /** 
     * SoundManager returns true by default,
     * whereas individual sounds return false by default. 
     * @return 
     */
    public boolean isProximityEnabled() {
	return proximity;
    }

    /**
     * 
     * @param proximity
     */
    public void setProximityEnabled(boolean proximity) {
	this.proximity = proximity;
	distanceChanged();
    }

    /**
     * 
     * @param distance
     */
    public void setDistanceRadius(float distance) {
	this.distanceRadius = distance;
    }

    /**
     * 
     * @return
     */
    public float getDistanceRadius() {
	return distanceRadius;
    }

    /**
     * 
     * @param b
     */
    public void setDistanceCheckingEnabled(boolean b) {
	this.distanceCheckingEnabled = b;
    }

    /**
     * 
     * @return
     */
    public boolean isDistanceCheckingEnabled() {
	return distanceCheckingEnabled;
    }

    /**
     * @return the soundIndexStart
     */
    public int getMusicTrackCount() {
	return musicTrackCount;
    }

    /**
     * By default, all sounds are managed.
     * 
     * @param soundIndexStart the soundIndexStart to set
     */
    public void setMusicTrackCount(int musicTrackCount) {
	this.musicTrackCount = musicTrackCount;
    }
}


/*
final int AMBIENCE_LAYER = 0;
final int PLAYER_LAYER = 1;
final int SFX_LAYER = 2;
final int SPEECH_LAYER = 3;

//initialize the sound engine with 16 sources
SoundManager.get().init(16);

//add a looping, ambient sound effect in the background
Sound2D ambience = new Sound2D("res/ambience.wav");
ambience.setLayer(AMBIENCE_LAYER); //a low "priority layer"
ambience.setLooping(true);  //loop it
ambience.setRelative(true); //remove 3D stereo panning/attenuation
ambience.play();

//create a new sound effect somewhere to the right of us
Sound2D explosion = new Sound2D("res/explosion.wav");
explosion.setLayer(SFX_LAYER); //overrides AMBIENCE_LAYER if necessary
explosion.setPosition(80f, -10f); //move it away from us
explosion.play();


SoundManager.get().setRadius(100f);
speech.setRadius(50f);
pX = 150f;
pY = 100f;

speech.setLooping(true);

//play with current flags
speech.play();*/