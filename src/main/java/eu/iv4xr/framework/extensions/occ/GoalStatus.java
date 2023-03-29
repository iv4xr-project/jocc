package eu.iv4xr.framework.extensions.occ;

/**
 * Runtime status of a goal, see {@link eu.iv4xr.framework.extensions.occ.Goal}.
 */
public class GoalStatus implements Cloneable {

    public Goal goal;

    /**
     * Maximum value for likelihood, which is 1.
     */
    public static double maxLikelihood = 1 ;

    /**
     * The goal likelihood, a number in [0..1]
     */
    public double likelihood;

    public boolean isAchieved = false;
    public boolean isFailed = false;

    public String goalName() {
        return goal.name;
    }

    public void setAsFailed() {
        isAchieved = false;
        isFailed = true;
        likelihood = 0;
    }

    public void setAsAchieved() {
        isAchieved = true;
        isFailed = false;
        likelihood = 1 ;
    }

    @Override
    public String toString() {
        return goal.toString() + ", p=" + likelihood + ", achieved=" + isAchieved + ", fail=" + isFailed;
    }

    /**
     * Making a deep clone of this object.
     */
    @Override
    public Object clone() {
        GoalStatus st = new GoalStatus();
        st.goal = this.goal;
        st.likelihood = this.likelihood;
        st.isAchieved = this.isAchieved;
        st.isFailed = this.isFailed;
        return st;
    }

}
