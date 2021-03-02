package eu.iv4xr.framework.extensions.occ;

import java.util.*;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Rules.* ;
import static eu.iv4xr.framework.extensions.occ.EmotionRelatedFunctions.* ;

public class AppraisalTransitionSystem {
	
	/**
	 * The name of the agent to which this appraisal system is attached to.
	 */
	String agentName ;
	
	// Various rules :

    public AppraisalRules appraisal;
    public EmotionIntensityDecayRule decayrule;
    public EmotionIntensityThresholdRule intensityThresholdRule;
    public GoalTowardsGoalLikelihoodRule goalTowardsGoalRule;
    

    // State-components:
    public BeliefBase beliefbase ;
    public Set<Emotion> emo = new HashSet<>() ;
    
    // memory ... emotion e will be added when it emerges. Its t0 and w0 already contains the
    // time when it emerges and its initial intensity; its w would be its current intensity.
    public EmotionMemory ememory= new EmotionMemory();
    
    public int currentTime = 0;
    
    //private bool found = false;
    
    public AppraisalTransitionSystem(String agentName)
    {
        this.agentName = agentName ;
    }
    
    public void addGoal(Goal g,int likelihood){
    	GoalStatus status = new GoalStatus() ;
    	status.goal = g ;
    	status.likelihood = likelihood ;
        beliefbase.getGoalsStatus().statuses.put(g.name,status) ; 
    }
    
    /**
     * Repeatedly applying goalTowardsGoal-rule for all pairs of goals, until goals status
     * (well, more precisely until goals' likelihood) does not change anymore.
     */
    void applyGoalTowardsGoalRule()
    {
    	Goals_Status previous = null ;
        // a do-while loop instead of repeat-until loop, because C# does not have repeat-until
        do
        {
            previous = (Goals_Status) beliefbase.getGoalsStatus().clone() ;
        	
            // for each pair of goals, apply the goalTowardsGoal-rule to update newBelief:
            for (GoalStatus cause : beliefbase.getGoalsStatus().statuses.values())
            {
            	for (GoalStatus consequent : beliefbase.getGoalsStatus().statuses.values())
                {
                    if (cause == consequent) continue;
                    // consider the goal-pair (cause,consequent). Invoke the goal2goal-rule to see if
                    // the new likelihood of the cause-goal (if it changes at all) would influence
                    // the agent's belief on the likelihood of the consequence-goal:
                    Integer newLikelihood = goalTowardsGoalRule.rule
                    		                .apply(beliefbase)
                    		                .apply(cause.goal.name)
                    		                .apply(consequent.goal.name);                    
                    
                    if (newLikelihood != null)
                    {
                        consequent.likelihood = newLikelihood ;
                    }
                }
            }
        }
        // keep repeating while previous and new-belief are not likelihoods-equal:
        while (! beliefbase.getGoalsStatus().equals(previous)) ;
    }
    
    
    /**
     * Apply decay on the emotions. Those whose intensity drop to zero will be discarded. 
     * Be careful... this method directly change the emotions maintained by this Transition
     * Syatem.
     */
    private void applyTimeDecayToEmotions(int newtime)
    {
        for(Emotion e : emo) {
            // use the decay-intensity-function to obtain the new intensity:
        	e.intensity = decayedIntesity(decayrule, e.etype, e.intensity0, e.t0, newtime);
        }
        // remove those whose intensity drops to 0:
    }
    
    /**
     * Update this Transition System state, upon receiving an event e. Also specify when the new
     * time now.
     */ 
    public void update(Event e, int newtime) {
    	
    	if(newtime < currentTime) throw new IllegalArgumentException() ;
    	
    	Goals_Status goalsStatusBeforeUpdate = (Goals_Status) beliefbase.getGoalsStatus().clone() ;
    	
	
    	// Updating the current-time:
        currentTime = newtime;
        
        // Updating the beliefbase, after this update the current beliefbase is K_plus:
        e.applyEffectOnBeliefBase(beliefbase);
        
        // applying R* to apply chain-updates to goals-likelihood:
        applyGoalTowardsGoalRule() ;

        List<Goal> goals = beliefbase.getGoalsStatus().statuses.values().stream()
        		           . map(status -> status.goal)
        		           . collect(Collectors.toList()) ;

        // first calculate new emotions that would emerge:
        
        HashSet<Emotion> newEmotions = new HashSet<Emotion>();
        Goals_Status goalsCurrentStatus = beliefbase.getGoalsStatus() ;
        for(Goal g : goals) {

        	for(var etype : Emotion.emotionTypes) {
             
                // calculate the intensity:
                int w = EmotionFunction(etype, 
                		      appraisal, 
                		      intensityThresholdRule, 
                		      goalsStatusBeforeUpdate, 
                		      goalsCurrentStatus, 
                		      e, 
                		      g.name,
                		      emo,
                		      ememory);
                // add the emotion only if the intestity >0 :
                if (w > 0)
                {
                	Emotion em = new Emotion(etype, g,currentTime, w) ;
                    newEmotions.add(em);
                    // we add emerging emotion to the memory:
                    ememory.register(em);
                }
            }
        }
        
        // now we apply time-decay to old emotions:
        applyTimeDecayToEmotions(currentTime) ;
        
        // Now merge the decayed ols emotions and new emotions:

        // First, obtain the old emotions, after the decay above, that would be retained (not in conflict by newEmotions nor maximized by
        // newEmotions).
        Set<Emotion> retainedOldEmotion = new HashSet<>();
        for(Emotion emoOld : emo)
        {
            // drop if the emotion occurs in the newEmotions:
            if (newEmotions.stream().anyMatch(emoNew -> emoNew.etype == emoOld.etype 
            		&& emoNew.g.name.equals(emoOld.g.name)))
            	
            	// NOTE: we can alternatively drop only drop old emotions whose intensity is lower or equal to new emotion
            	
                continue;
            // drop if a conflicting emotion occurs in the newEmotion:
            if (newEmotions.stream().anyMatch(emoNew -> emoNew.g.name.equals(emoOld.g.name) 
            		&& Emotion.exclusion(emoNew.etype,emoOld.etype)))
                continue;
            retainedOldEmotion.add(emoOld);
        }

        // For each new-emotion, compare its value with the corresponding decayed-emotion. Take the maximum.
        // NOTE: not sure if this logic is sound. If old intensity is higher, then we should take over the 
        // old t0 as well. But in other words this mean we keep the old emotion and drop the new emotion.
        for (Emotion emotion : newEmotions) {
            List<Emotion> results = emo.stream()
            		.filter(emoOld -> emotion.etype == emoOld.etype && emotion.g.name.equals(emoOld.g.name))
                    .collect(Collectors.toList()) ;
            //Debug.Assert(results.Count() <= 1);
            
            if (results.size() == 0) continue;
            Emotion emoOld = results.get(0) ;
            emotion.intensity = Math.max(emotion.intensity, emoOld.intensity);
            // what about if the decayed intenstiy is higher than the new one?
                
        }

        // merging:

        this.emo.clear() ;
        this.emo.addAll(newEmotions) ;
        this.emo.addAll(retainedOldEmotion) ;
       
        // clean up ememory:
        ememory.cleanup(currentTime);

        // cleanup goals that have been achieved:  (i remove those goals that fail and are
        // completed, because the corresponding new-emotions, if there is any,
        // have been taken into account above)
        List<GoalStatus> toBeRemoved = beliefbase.getGoalsStatus().statuses.values().stream()
        		                       . filter(status -> status.isAchieved || status.isFailed)
        		                       . collect(Collectors.toList()) ;
        
        for(var dropThis : toBeRemoved) {
        	beliefbase.getGoalsStatus().statuses.remove(dropThis.goal.name) ;
        }
        
        System.out.println("** Update: " + e.name + ",time=" + currentTime);
        System.out.println("** Belief-base:");
        System.out.println(beliefbase);
        System.out.println(("** Emotion state:"));
        int k = 0;
        for(Emotion emotion : emo)
        {
            if(k>0) System.out.println("");
            System.out.println("   " + emotion);
        }
    }

}
