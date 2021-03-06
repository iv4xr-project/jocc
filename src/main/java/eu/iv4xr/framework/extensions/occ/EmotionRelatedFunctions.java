package eu.iv4xr.framework.extensions.occ;

import java.util.*;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;

public class EmotionRelatedFunctions {
	
	public static int EmotionFunction(UserCharacterization userModel,
			EmotionType ety,
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
                case Joy: return joy(userModel, goalsStatusAfter, e, g);
                case Distress: return distress(userModel, goalsStatusAfter, e, g);
                case Hope: return hope(userModel, goalsStatusBefore, goalsStatusAfter, g);
                case Fear: return fear(userModel, goalsStatusBefore, goalsStatusAfter, g);
                case Satisfaction: return satisfaction(userModel, goalsStatusBefore, goalsStatusAfter, g, emotionset,ememory);
                case Disappointment: return disappointment(userModel, goalsStatusBefore, goalsStatusAfter, g, emotionset,ememory);
            }
            throw new IllegalArgumentException() ; // should not reach this point
        }

        public static int joy(UserCharacterization userModel,
            Goals_Status goalsStatusAfter,
            Event e,
            Goal g)
        {
            if (userModel.desirabilityAppraisalRule(goalsStatusAfter, e.name, g.name) > 0 
                && goalsStatusAfter.goalStatus(g.name).likelihood == GoalStatus.maxLikelihood) 
            	
            	return intensity(userModel,EmotionType.Joy, goalsStatusAfter, e, g.name);
            
            else return 0;
        }

        public static int distress(UserCharacterization userModel,
            Goals_Status goalsStatusAfter,
            Event e,
            Goal g)
        {
            if (userModel.desirabilityAppraisalRule(goalsStatusAfter, e.name, g.name) < 0 
            	// this is from C#, seems to be a mistake. Should be 0:	
            	// && goalsStatusAfter.goalStatus(g.name).likelihood == GoalStatus.maxLikelihood) 
            	&& goalsStatusAfter.goalStatus(g.name).likelihood == 0 ) 
            	
            	return intensity(userModel, EmotionType.Distress, goalsStatusAfter, e, g.name);
            
            else return 0;
        }

        public static int hope(UserCharacterization userModel,
            Goals_Status goalsStatusBefore,
            Goals_Status goalsStatusAfter,
            Goal g
            )
        {
            var p_before = goalsStatusBefore.goalStatus(g.name).likelihood ;
            var gStatusAfter = goalsStatusAfter.goalStatus(g.name) ;
            var p_after = gStatusAfter.likelihood ;
            //System.out.println("%%%%% checking HOPE, pold=" + p_before + ", pnew=" + p_after) ;
            if (p_after > p_before 
            	&& p_after < GoalStatus.maxLikelihood
            	&& ! gStatusAfter.isAchieved) {
            	
            	var isy = intensity(userModel, EmotionType.Hope, goalsStatusAfter, null, g.name);
            	//System.out.println("%%%%% checking HOPE " + isy) ;
            	return isy ;
            }
            
            	
            
            else return 0;
        }

        public static int fear(UserCharacterization userModel,
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
            	
                return intensity(userModel, EmotionType.Fear, goalsStatusAfter, null, g.name);
            
            else return 0;
        }

        public static int satisfaction(UserCharacterization userModel,
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
            	
            	return intensity(userModel, EmotionType.Satisfaction, goalsStatusAfter, null, g.name);
            
            
            else return 0;
        }
        

        public static int disappointment(UserCharacterization userModel,
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
            
            	return intensity(userModel, EmotionType.Disappointment, goalsStatusAfter, null, g.name);
            
            else return 0;
        }

        /**
         * Intensity function, for determining the initial intensity of an emotion towards 
         * a given target, when this emotion is triggered for the first time. The trigger 
         * is an event.
         */ 
        public static int intensity(UserCharacterization userModel, EmotionType ety, Goals_Status goalsStatus, Event e, String targetName)
        {
            return potential(userModel, ety, goalsStatus, e , targetName) - userModel.intensityThresholdRule(ety) ;
        }

        /**
         * Potential function is used to determine the raw initial intensity of emotion towards 
         * a given goal, when it is triggered by an event. This is used inside the 
         * intensity-function.
         */ 
        public static int potential(UserCharacterization userModel, EmotionType ety, Goals_Status goalsStatus,  Event e, String targetName)
        {
        	Goal g ;
            switch(ety)
            {
                case Joy     : return Math.abs(userModel.desirabilityAppraisalRule(goalsStatus,e.name, targetName));
                case Distress: return Math.abs(userModel.desirabilityAppraisalRule(goalsStatus,e.name, targetName));
                case Hope:
                    g = goalsStatus.getGoal(targetName);
                    return goalsStatus.goalStatus(targetName).likelihood * g.significance ;
                case Fear:
                    g = goalsStatus.getGoal(targetName);
                    return (GoalStatus.maxLikelihood - goalsStatus.goalStatus(targetName).likelihood) * g.significance;
                case Satisfaction:
                	g = goalsStatus.getGoal(targetName);
                    // return goalsStatus.goalStatus(targetName).likelihood * g.significance;  likelihood would then be 1
                    // return g.significance;
                	// HAck:
                    return 80*g.significance;
                case Disappointment:
                	g = goalsStatus.getGoal(targetName);
                    // return (GoalStatus.maxLikelihood - goalsStatus.goalStatus(targetName).likelihood) * g.significance; likelihood would be 0
                    return g.significance;
            }
            throw new IllegalArgumentException() ; // should not reach this point
        }

        /**
         * The ifun_time function. 
         */ 
        public static int decayedIntesity(UserCharacterization userModel, EmotionType ety, int initialIntensity, int t0, int newtime)
        {

        	assert newtime >= t0 ;
            if (newtime == t0) return initialIntensity;
            // emotion dependent decay-factor (determines how fast the decay)
            double decayfactor = userModel.emotionIntensityDecayRule(ety);
            double c = -0.5 ;
            double w0 = (double)initialIntensity;
            //I believe the linear function works good enough and we dont need exp function until there is a valid reason 
            //return (int) Math.Round(w0 * Math.Exp(c * decayfactor * (newtime - t0)));
            // WP: but below is not a linear function.. in fact it will decay very fast
            //return (int) (w0 / (decayfactor * (newtime - t0)));
            // this is linear:
            return (int) w0 - (int)(decayfactor * (double) (newtime - t0));
            

        }

}
