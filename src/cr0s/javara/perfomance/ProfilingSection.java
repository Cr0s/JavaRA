package cr0s.javara.perfomance;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Color;

public class ProfilingSection {
    private static final int MAX_HISTORY_SIZE = 100;
    private static final int SAMPLING_FREQUENCY = 10; // Save current time to history every SAMPLING_HISTORY measurements
    
    private int numMeasured = 0;
    
    private int msPassed;
    private long currentTimeMillis;
    private boolean isTimerStarted;
    
    public LinkedList<Integer> history;
    public Color color;
    
    public ProfilingSection(Color color) {
	this.history = new LinkedList<Integer>();
	
	this.color = color;
    }
    
    public void startTimer() {
	this.isTimerStarted = true;
	this.currentTimeMillis = System.currentTimeMillis();
    }
    
    public int stopTimer() {
	if (this.isTimerStarted) {
	    this.msPassed = (int) (System.currentTimeMillis() - this.currentTimeMillis);
	    this.isTimerStarted = false;
	    
	    if (++this.numMeasured >= this.SAMPLING_FREQUENCY) {
		this.numMeasured = 0;
		
		this.pushToHistory();
	    }
	}
	
	return this.msPassed;
    }
    
    public int getMsPassed() {
	return this.msPassed;
    }
    
    private void pushToHistory() {
	this.history.addLast(this.msPassed);
	
	// Roll list to preserve fixed history size
	if (this.history.size() > MAX_HISTORY_SIZE) {
	    this.history.removeFirst();
	}
    }

    public float getAvgMsPassed() {
	float avgRes = 0;
	for (Integer i : this.history) {
	    avgRes += i;
	}
	
	if (!this.history.isEmpty()) {
	    avgRes /= this.history.size();
	}
	
	return avgRes;
    }
}
