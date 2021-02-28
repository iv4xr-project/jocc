package eu.iv4xr.framework.extensions.occ;

/**
 * 
 * Representing a goal. The name is assumed to be unique. Furthermore, a goal
 * is assumed to be unique in the sense that we should not have two instances
 * of the same goal, existing at the same time in the state of an emotion agent.
 *
 */
public class Goal {
		
	public String name ;
	public int significance ;
	
	public Goal() { }
	
	public Goal(String name) {
		this.name = name ;
	}
	
	 public Goal(String name, int significane) {
         this.name = name;
         this.significance = significane;
     }
	 
	 public Goal withSignificance(int significance) {
		 this.significance = significance ;
		 return this ;
	 }
	 
	
	 @Override
	 public String toString() {
		 return "Goal: " + name + " (sig. " + significance + ")" ; 
	 }
	

}
