package eu.iv4xr.framework.extensions.occ;

import java.util.*;

/**
 * Representing the belief-base (state) of an emotion-aware agent. We will represent
 * this generically as an interface; the key-part of this belief structure is that 
 * it is assumed to keep track of the status of different goals, which is essential 
 * for our emotion appraisal system. 
 * 
 * A "goal" here does not necessarily mean what
 * the agent is programmed to do. Here "goals" represent real life goals, the agent
 * would emotionally cares about.
 * 
 * For example, a software agent simulating a human bus driver might be programmed to 
 * drive a bus to reach a destination B without crashing it. "Reaching B" can 
 * be taken as a goal. But perhaps "Reaching B on time" and "Picking up many
 * passengers" might also be other goals that are emotionally relevant for a
 * human driver.  
 */
public interface BeliefBase {
	
	/**
	 * A structure for maintaining the status of a set of goals.
	 */
	public static class Goals_Status implements Cloneable {
		
		public HashMap<String,GoalStatus> statuses =  new HashMap<>() ;
		
		public Goal getGoal(String goalName) {
			return statuses.get(goalName).goal ;
		}
		
		public GoalStatus goalStatus(String goalName) {
			return statuses.get(goalName) ;
		}
		
		public Object clone() {
			Goals_Status gsts = new Goals_Status() ;
			for(var st : this.statuses.entrySet()) {
				gsts.statuses.put(st.getKey(), (GoalStatus) st.getValue().clone()) ;
			}
			//gsts.statuses = (HashMap<String,GoalStatus>) this.statuses.clone() ;
			return gsts ;
		}
		
		@Override
		public boolean equals(Object o) {
			if (! (o instanceof Goals_Status)) return false ;
			Goals_Status gs2 = (Goals_Status) o ;
			
			for (var st1 : statuses.entrySet())
            {
				var status1 = st1.getValue() ;
				var status2 = gs2.statuses.get(st1.getKey()) ;
				if(! status1.equals(status2)) return false ;
            }
			
			for (var st2 : gs2.statuses.entrySet()) {
				var status2 = st2.getValue() ;
				var status1 = statuses.get(st2.getKey()) ;
				if(! status2.equals(status1)) return false ;
			}
			return true ;
		}
		
		public String toString() {
			String r = "" ;
			for(GoalStatus gs : statuses.values()) r += "    " + gs.toString() + "\n" ;
			return r ;
		}
		
	}
	
	public Goals_Status getGoalsStatus() ;

}
