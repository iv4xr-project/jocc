package eu.iv4xr.framework.extensions.occ;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Representing an event. 
 */
public abstract class Event {
	
	public String name ;
	
	/**
	 * This method implements the semantic of this Event in terms of an update 
	 * to the given belief-base.
	 */
	public abstract void applyEffectOnBeliefBase(BeliefBase beliefbase) ;
	
	public Event() { }
	public Event(String name) { this.name = name ; }
	
	
	@Override
	public String toString() { return "Event " + name ; }
	
	public static class Tick extends Event {
		
		 // a tick event does not change the beliefbase...
		@Override
		public void applyEffectOnBeliefBase(BeliefBase beliefbase) { }
	}
		
}
