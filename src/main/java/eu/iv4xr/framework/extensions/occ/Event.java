package eu.iv4xr.framework.extensions.occ;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Representing an event. 
 */
public abstract class Event {
	
	public String name ;
	
	
	public Event() { }
	public Event(String name) { this.name = name ; }
	
	
	@Override
	public String toString() { return "Event " + name ; }
	
	public static class Tick extends Event {
		
		public Tick() { super("tick") ; }
	}
		
}
