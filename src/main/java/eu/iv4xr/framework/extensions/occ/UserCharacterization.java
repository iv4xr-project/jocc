package eu.iv4xr.framework.extensions.occ;

import java.util.function.Function;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;

/*** @author sansari
 * This class models a user, or a type of users, in terms of how they appraise
 * incoming events towards e.g. goals that matter for them.
 */
public abstract class UserCharacterization {

    /**
     * This method should model the semantic of an Event in terms of an update to
     * the given belief-base.
     */
    public abstract void eventEffect(Event e, BeliefBase beliefbase);

    // appraisal rules, currently only desirability towards goals is supported:

    /**
     * Appraise the desirability of an event towards a goal. Positive is desirable,
     * negative is the opposite. 0 means the event is considered as not relevant
     * towards the goal. 1 id the event is very desirable.
     * 
     * <p>Desirability should be a value in [-1..1]
     */
    public abstract double desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName);

    /*
     * Commenting this out. These are not really goal-oriented appraisal... public
     * abstract int praiseWorthinessAppraisalRule(... String eventName ...)
     * 
     * public abstract int desirabilityTowardsOtherAppraisalRule(... String
     * eventName ...)
     * 
     * public abstract int likingAppraisalRule(... String eventName ...)
     */

    // Various rules :

    /**
     * This rule specifies for each emotion type, what the corresponding
     * decay-factor. This should be a number >= 1. The bigger the factor, the faster the emotion
     * decay over time. The definition of the actual decay function used can be found here:
     * {@link EmotionRelatedFunctions#decayedIntesity(UserCharacterization, eu.iv4xr.framework.extensions.occ.Emotion.EmotionType, double, long, long)}.
     */
    public abstract double emotionIntensityDecayRule(Emotion.EmotionType etype);

    public abstract double intensityThresholdRule(Emotion.EmotionType etyp);

    /**
     * Representing a rule (actually, a combined set of rules) describing how the
     * likelihood of a given goal affects the likelihood of another goal
     * (consequence-goal). When applied, the rule returns the new estimated/believed
     * likelihood of the consequence goal. Null is returned if the causing-goal is
     * considered as irrelevant for the consequent-goal.
     * 
     * <p>The default is that this rule is just empty.
     */
    public Function<BeliefBase, Function<String, Function<String, Double>>> goalTowardsGoalRule = bbs -> cause -> consequent -> null;

}
