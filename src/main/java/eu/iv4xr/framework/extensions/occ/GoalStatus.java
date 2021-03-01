package eu.iv4xr.framework.extensions.occ;

/**
 * Runtime status of a goal, see {@link eu.iv4xr.framework.extensions.occ.Goal}.
 */
public class GoalStatus implements Cloneable {
	
	public Goal goal ;
	
	/**
	 * Maximum value for likelihood. Set at 100.
	 */
	public static int maxLikelihood = 100;
	
	/**
	 * The goal likelihood. We will represent this as as an integer in the range of 0..100,
     * with 0 means that the goal is believed, with certainty, not to happen; 100 is the
     * opposite.
	 */
	public int likelihood;
	
	public boolean isAchieved = false;
    public boolean isFailed = false;
    
    public String goalName() { return goal.name ; }
    
    @Override
    public String toString() {
		 return goal.toString() + ", p=" + likelihood 
				 + ", achieved="+ isAchieved
				 + ", fail=" + isFailed; 
	 }
    
    /**
     * Making a deep clone of this object.
     */
    @Override
    public Object clone() {
    	GoalStatus st = new GoalStatus() ;
    	st.goal = this.goal ;
    	st.likelihood = this.likelihood ;
    	st.isAchieved = this.isAchieved ;
    	st.isFailed = this.isFailed ;
    	return st ;
    }

}
