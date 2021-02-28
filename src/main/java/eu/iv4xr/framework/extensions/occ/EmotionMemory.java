package eu.iv4xr.framework.extensions.occ;

import java.util.*;

public class EmotionMemory {
	
	public static class SingleEmotionMemory {
		Emotion emotion ;
		int timeStamp ;
	}
	
	List<SingleEmotionMemory> memory = new LinkedList<>() ;
	
	public static int memoryHorizon = 1000 ;
	
	/**
	 * Add this emotion e to the memory, with the given time-stamp.
	 */
	public void register(Emotion e, int currentTime) {
		SingleEmotionMemory mem = new SingleEmotionMemory() ;
		mem.emotion = e.shallowClone() ;
		mem.timeStamp = currentTime ;
		memory.add(mem) ;
	}
	
	/**
	 * Remove memories which are too old.
	 */
	public void cleanup(int currentTime) {
		int oldestAllowed = Math.max(0,currentTime - 1000) ;
		memory.removeIf(mem -> mem.timeStamp < oldestAllowed) ;
	}
	
	/**
	 * Check the memory registered an emotion of the given type, towards the given goal.
	 */
	public boolean contains(Emotion.EmotionType etype, String goal) {
		return memory.stream().anyMatch(mem -> mem.emotion.etype == etype && mem.emotion.g.name.equals(goal)) ;
	}

}
