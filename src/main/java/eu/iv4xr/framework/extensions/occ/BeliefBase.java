package eu.iv4xr.framework.extensions.occ;

import java.util.*;

/**
 * Representing the belief-base (state) of an emotional agent. In this base
 * class the belief-base only contains goal-statuses. When actually using
 * this you may want to extend this class with other information.
 */
public class BeliefBase {
	
	/**
	 * Maintaining a set of active goals, and their statuses.
	 */
	public static class GoalStatuses implements Cloneable {
		
		public HashMap<String,GoalStatus> statuses =  new HashMap<>() ;
		
		Goal getGoal(String goalName) {
			return statuses.get(goalName).goal ;
		}
		
		GoalStatus goalStatus(String goalName) {
			return statuses.get(goalName) ;
		}
		
		public Object clone() {
			GoalStatuses gsts = new GoalStatuses() ;
			gsts.statuses = (HashMap<String,GoalStatus>) this.statuses.clone() ;
			return gsts ;
		}
		
	}
	
	
	GoalStatuses goalstatuses = new GoalStatuses() ;
	
}
