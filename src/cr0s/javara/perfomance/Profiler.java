package cr0s.javara.perfomance;

import java.util.HashMap;

import org.newdawn.slick.Color;

public class Profiler {
    HashMap<String, ProfilingSection> profilers;
    
    private Color[] colors = { Color.red, Color.green, Color.blue, Color.cyan, Color.magenta, Color.orange, Color.yellow, Color.lightGray, Color.white };
    private int nextColor;
    
    private static Profiler instance;
    private Profiler() {
	this.profilers = new HashMap<String, ProfilingSection>();
    }
    
    public static Profiler getInstance() {
	if (instance == null) {
	    instance = new Profiler();
	}
	
	return instance;
    }
    
    private ProfilingSection getSection(final String name) {
	if (this.profilers.containsKey(name)) {
	    return this.profilers.get(name);
	} else {
	    ProfilingSection newSection = new ProfilingSection(chooseColorForNewSection());
	    this.profilers.put(name, newSection);
	    
	    return newSection;
	}
    }
    
    private Color chooseColorForNewSection() {
	Color choosen = this.colors[nextColor];
	nextColor = (nextColor + 1) % this.colors.length; // Get next color in cycle of colors
	
	return choosen;
    }

    public void startForSection(final String name) {
	this.getSection(name).startTimer();
    }
    
    public void stopForSection(final String name) {
	this.getSection(name).stopTimer();
    }
    
    public int getMsPassedForSection(final String name) {
	return this.getSection(name).getMsPassed();
    }
}
