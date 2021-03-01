package eu.iv4xr.framework.extensions.occ;

import java.util.function.Function;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;

/**
 * Provides some classes representing different kind of rules for configuring how emotions
 * are calculated.
 */

public class Rules {
	
	
	/**
	 * An appraisal rule takes a belief-base, an event, and a goal, and returns some 
	 * appraisal value of the event with respect to the goal, given the context of
	 * the belief.
	 */
	public static class AppraisalRule {
		Function<Goals_Status, Function<String, Function<String,Integer>>> rule ;
		
		public AppraisalRule(Function<Goals_Status, Function<String, Function<String,Integer>>> rule) {
			this.rule = rule ;
		}
		
		int apply(Goals_Status goalsStatus, String eventName, String targetName) {
			return rule.apply(goalsStatus).apply(eventName).apply(targetName) ;
		}
	}
	
	public static class AppraisalRules{
	        public AppraisalRule desirability;
	        public AppraisalRule praiseWorthiness;
	        public AppraisalRule desirabilityTowardsOther;
	        public AppraisalRule liking;
	        public AppraisalRules(){ }
	}
	
	/**
	 * This rule specifies for each emotion type, what the corresponding decay-factor. 
	 * This should be an int >= 1. Emotion with decay-factor 2 will decay, roughly, 
	 * twice as fast.
	 */
	public static class EmotionIntensityDecayRule {
		
		Function<Emotion.EmotionType,Integer> rule ;
		
		public EmotionIntensityDecayRule(Function<Emotion.EmotionType,Integer> rule) {
			this.rule = rule ;
		}	
	}
	
	public static class EmotionIntensityThresholdRule {
		
		Function<Emotion.EmotionType,Integer> rule ;
		
		public EmotionIntensityThresholdRule(Function<Emotion.EmotionType,Integer> rule) {
			this.rule = rule ;
		}
	}
	
	
	/**
	 * Representing a rule (actually, a combined set of rules) describing how the likelihood of
	 * a given goal affects the likelihood of another goal (consequence-goal). When applied, 
	 * the rule returns the new estimated/believed likelihood of the consequence goal. Null is 
	 * returned if the causing-goal is considered as irrelevant for the consequent-goal.
	 */
	public static class GoalTowardsGoalLikelihoodRule {
		
		Function<BeliefBase, Function<String, Function<String,Integer>>> rule ;
		
		public GoalTowardsGoalLikelihoodRule(Function<BeliefBase, Function<String, Function<String,Integer>>> rule) {
			this.rule = rule ;
		}		
	}
	
	
	

}
