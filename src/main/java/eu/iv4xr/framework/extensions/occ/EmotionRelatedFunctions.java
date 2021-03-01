package eu.iv4xr.framework.extensions.occ;

import java.util.*;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Rules.*;

public class EmotionRelatedFunctions {
	
	public static int EmotionFunction(EmotionType ety,
			AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Event e,
            String targetName,
            Set<Emotion> emotionset,
            EmotionMemory ememory
            )
        {
            // in our case, all emotions so far are towards a goal. So, the target is always a goal:
            Goal g = goalsStatusAfter.getGoal(targetName) ;
            switch(ety)
            {
                case Joy: return joy(appr, threshold, goalsStatusAfter, e, g);
                case Distress: return distress(appr, threshold, goalsStatusAfter, e, g);
                case Hope: return hope(appr, threshold, goalsStatusBefore, goalsStatusAfter, g);
                case Fear: return fear(appr, threshold, goalsStatusBefore, goalsStatusAfter, g);
                case Satisfaction: return satisfaction(appr, threshold, goalsStatusBefore, goalsStatusAfter, g, emotionset,ememory);
                case Disappointment: return disappointment(appr, threshold, goalsStatusBefore, goalsStatusAfter, g, emotionset,ememory);
            }
            throw new IllegalArgumentException() ; // should not reach this point
        }

        public static int joy(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusAfter,
            Event e,
            Goal g)
        {
            if (appr.desirability.apply(goalsStatusAfter, e.name, g.name) > 0 
                && goalsStatusAfter.goalStatus(g.name).likelihood == GoalStatus.maxLikelihood) 
            	
            	return intensity(EmotionType.Joy, appr, threshold, goalsStatusAfter, e, g.name);
            
            else return 0;
        }

        public static int distress(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusAfter,
            Event e,
            Goal g)
        {
            if (appr.desirability.apply(goalsStatusAfter, e.name, g.name) < 0 
            	// this is from C#, seems to be a mistake. Should be 0:	
            	// && goalsStatusAfter.goalStatus(g.name).likelihood == GoalStatus.maxLikelihood) 
            	&& goalsStatusAfter.goalStatus(g.name).likelihood == 0 ) 
            	
            	return intensity(EmotionType.Distress, appr, threshold, goalsStatusAfter, e, g.name);
            
            else return 0;
        }

        public static int hope(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Goal g
            )
        {
            var p_before = goalsStatusBefore.goalStatus(g.name).likelihood ;
            var gStatusAfter = goalsStatusAfter.goalStatus(g.name) ;
            var p_after = gStatusAfter.likelihood ;
            if (p_after > p_before 
            	&& p_after < GoalStatus.maxLikelihood
            	&& ! gStatusAfter.isAchieved)
            
            	return intensity(EmotionType.Hope, appr, threshold, goalsStatusAfter, null, g.name);
            
            else return 0;
        }

        public static int fear(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Goal g
            )
        {
        	var p_before = goalsStatusBefore.goalStatus(g.name).likelihood ;
            var gStatusAfter = goalsStatusAfter.goalStatus(g.name) ;
            var p_after = gStatusAfter.likelihood ;
            if (p_after < p_before
            	&& p_after < GoalStatus.maxLikelihood
            	&& ! gStatusAfter.isAchieved)
            	
                return intensity(EmotionType.Fear, appr, threshold, goalsStatusAfter, null, g.name);
            
            else return 0;
        }

        public static int satisfaction(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Goal g,
            Set<Emotion> emotionset,
            EmotionMemory ememory
            )
        {
            //according to Fatima math
            /*var p_before = currentBelief.Likelihood(g.name);
            var p_after = newBelief.Likelihood(g.name);
            if (GoalStatus.maxLikelihood/2 < p_before
                &&(p_before < GoalStatus.maxLikelihood
                && p_after == GoalStatus.maxLikelihood))
                return intensity(EmotionType.Satisfaction, appr, threshold, newBelief, null, g.name);*/

            if (emotionset.stream().anyMatch(emotion -> emotion.g.name.equals(g.name) && emotion.etype == EmotionType.Joy)
            	&& ememory.contains(EmotionType.Hope,g.name)            	
            	&& goalsStatusAfter.goalStatus(g.name).isAchieved) 
            	
            	return intensity(EmotionType.Satisfaction, appr, threshold, goalsStatusAfter, null, g.name);
            
            
            else return 0;
        }
        

        public static int disappointment(AppraisalRules appr,
            EmotionIntensityThresholdRule threshold,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Goal g,
            Set<Emotion> emotionset,
            EmotionMemory ememory
            )
        {
            //according to original math (Fatima)
            /*var p_before = currentBelief.Likelihood(g.name);
            var p_after = newBelief.Likelihood(g.name);
            if (p_before >= GoalStatus.maxLikelihood / 2 
                && p_after == 0)
                return intensity(EmotionType.Disappointment, appr, threshold, newBelief, null, g.name);*/
            
            if (emotionset.stream().anyMatch(emotion -> emotion.g.name.equals(g.name) && emotion.etype == EmotionType.Distress)
                && ememory.contains(EmotionType.Hope,g.name)      
                && goalsStatusAfter.goalStatus(g.name).isFailed) 
            
            	return intensity(EmotionType.Disappointment, appr, threshold, goalsStatusAfter, null, g.name);
            
            else return 0;
        }

        /**
         * Intensity function, for determining the initial intensity of an emotion towards 
         * a given target, when this emotion is triggered for the first time. The trigger 
         * is an event.
         */ 
        public static int intensity(EmotionType ety, AppraisalRules appraisal, EmotionIntensityThresholdRule threshold, Goals_Status goalsStatus, Event e, String targetName)
        {
            return potential(ety, appraisal, goalsStatus, e , targetName) - threshold.rule.apply(ety) ;
        }

        /**
         * Potential function is used to determine the raw initial intensity of emotion towards 
         * a given goal, when it is triggered by an event. This is used inside the 
         * intensity-function.
         */ 
        public static int potential(EmotionType ety, AppraisalRules appraisal, Goals_Status goalsStatus,  Event e, String targetName)
        {
        	Goal g ;
            switch(ety)
            {
                case Joy     : return Math.abs(appraisal.desirability.apply(goalsStatus,e.name, targetName));
                case Distress: return Math.abs(appraisal.desirability.apply(goalsStatus,e.name, targetName));
                case Hope:
                    g = goalsStatus.getGoal(targetName);
                    return goalsStatus.goalStatus(targetName).likelihood * g.significance ;
                case Fear:
                    g = goalsStatus.getGoal(targetName);
                    return (GoalStatus.maxLikelihood - goalsStatus.goalStatus(targetName).likelihood) * g.significance;
                case Satisfaction:
                	g = goalsStatus.getGoal(targetName);
                    return goalsStatus.goalStatus(targetName).likelihood * g.significance;
                case Disappointment:
                	g = goalsStatus.getGoal(targetName);
                    return (GoalStatus.maxLikelihood - goalsStatus.goalStatus(targetName).likelihood) * g.significance;
            }
            throw new IllegalArgumentException() ; // should not reach this point
        }

        /**
         * The ifun_time function. 
         */ 
        public static int decayedIntesity(EmotionIntensityDecayRule decayrule, EmotionType ety, int initialIntensity, int t0, int newtime)
        {

        	assert newtime >= t0 ;
            if (newtime == t0) return initialIntensity;
            // emotion dependent decay-factor (determines how fast the decay)
            double decayfactor = decayrule.rule.apply(ety);
            double c = -0.5 ;
            double w0 = (double)initialIntensity;
            //I believe the linear function works good enough and we dont need exp function until there is a valid reason 
            //return (int) Math.Round(w0 * Math.Exp(c * decayfactor * (newtime - t0)));
            return (int) (w0 / (decayfactor * (newtime - t0)));

        }

}
