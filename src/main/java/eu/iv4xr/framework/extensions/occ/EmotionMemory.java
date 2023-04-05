package eu.iv4xr.framework.extensions.occ;

import java.util.*;

public class EmotionMemory {

    List<Emotion> memory = new LinkedList<>();

    public long memoryHorizon = 50;    

    /**
     * Add this emotion e to the memory.
     * @author sansari
     */
    public void register(Emotion e) {
        memory.add(e);
    }

    /**
     * Remove memories which are too old.
     */
    public void cleanup(long currentTime) {
        long oldestAllowed = Math.max(0, currentTime - memoryHorizon);
        memory.removeIf(e -> e.t0 < oldestAllowed);
    }

    /**
     * Check the memory registered an emotion of the given type, towards the given
     * goal.
     */
    public boolean contains(Emotion.EmotionType etype, String goal) {
        return memory.stream().anyMatch(e -> e.etype == etype && e.g.name.equals(goal));
    }

}
