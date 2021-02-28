package eu.iv4xr.framework.extensions.occ;

import java.util.function.Function;

import eu.iv4xr.framework.extensions.occ.BeliefBase.GoalStatuses;

public class Rules {
	
	
	/**
	 * An appraisal rule takes a belief-base, an event, and a goal, and returns some 
	 * appraisal value of the event with respect to the goal, given the context of
	 * the belief.
	 */
	public static class AppraisalRule {
		Function<GoalStatuses, Function<String, Function<String,Integer>>> rule ;
		
		public AppraisalRule(Function<GoalStatuses, Function<String, Function<String,Integer>>> rule) {
			this.rule = rule ;
		}
		
		int apply(GoalStatuses goalsStatus, String eventName, String targetName) {
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
	public static class EmptionIntensityDecayRule {
		
		Function<Emotion.EmotionType,Integer> rule ;
		
		public EmptionIntensityDecayRule(Function<Emotion.EmotionType,Integer> rule) {
			this.rule = rule ;
		}	
	}
	
	public static class EmotionIntensityThresholdRule {
		
		Function<Emotion.EmotionType,Integer> rule ;
		
		public EmotionIntensityThresholdRule(Function<Emotion.EmotionType,Integer> rule) {
			this.rule = rule ;
		}
	}
	

}
