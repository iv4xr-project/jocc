package eu.iv4xr.framework.extensions.occ;

import java.util.*;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;

import static eu.iv4xr.framework.extensions.occ.EmotionRelatedFunctions.*;

public class EmotionAppraisalSystem {

    /**
     * The name of the agent to which this appraisal system is attached to.
     * @author sansari
     */
    String agentName;

    /** 
     * A model of a user (or a type of users) in terms of e.g. how they generally
     * appraise events towards their goals (e.g. is an event desirable, or
     * undesirable).
     */
    public UserCharacterization userModel;

    // State-components:
    public BeliefBase beliefbase;
    public Set<Emotion> emo = new HashSet<>();
    
    public boolean turnOnDebugPrinting = false ;
    
    public HashSet<Emotion> newEmotions;

    // memory ... emotion e will be added when it emerges. Its t0 and w0 already
    // contains the
    // time when it emerges and its initial intensity; its w would be its current
    // intensity.
    public EmotionMemory ememory = new EmotionMemory();
    
    public long currentTime = 0;

    // private bool found = false;

    public EmotionAppraisalSystem(String agentName) {
        this.agentName = agentName;
    }

    public EmotionAppraisalSystem withUserModel(UserCharacterization userModel) {
        this.userModel = userModel;
        return this;
    }

    public EmotionAppraisalSystem attachEmotionBeliefBase(BeliefBase bbs) {
        this.beliefbase = bbs;
        return this;
    }
    
    /**
     * Set how long moments of stimulated emotions will stay in the memory. The number k is expressed
     * in time-unit that is assumed to be used by the appraisal system (e.g. second, or milli-second).
     * The default is 1000 units.
     */
    public EmotionAppraisalSystem setEmotionMemoryHorizon(long k) {
    	ememory.memoryHorizon = k ;
    	return this ;
    }

    public EmotionAppraisalSystem addGoal(Goal g, double likelihood) {
        GoalStatus status = new GoalStatus();
        status.goal = g;
        status.likelihood = likelihood;
        beliefbase.getGoalsStatus().statuses.put(g.name, status);
        return this;
    }
    
    /**
     * Remove a goal from the system, e.g. if it no longer matters, or achieveable .
     */
    public EmotionAppraisalSystem removeGoal(Goal g) {
    	beliefbase.getGoalsStatus().statuses.remove(g.name) ;
    	return this ;
    }
    		

    public Emotion getEmotion(String goalName, Emotion.EmotionType ety) {
        for (Emotion e : emo) {
            if (e.g.name.equals(goalName) && e.etype == ety)
                return e;
        }
        return null;
    }

    /**
     * Repeatedly applying goalTowardsGoal-rule for all pairs of goals, until goals
     * status (well, more precisely until goals' likelihood) does not change
     * anymore.
     */
    void applyGoalTowardsGoalRule() {
        Goals_Status previous = null;
        // a do-while loop instead of repeat-until loop, because C# does not have
        // repeat-until
        do {
            previous = (Goals_Status) beliefbase.getGoalsStatus().clone();

            // for each pair of goals, apply the goalTowardsGoal-rule to update newBelief:
            for (GoalStatus cause : beliefbase.getGoalsStatus().statuses.values()) {
                for (GoalStatus consequent : beliefbase.getGoalsStatus().statuses.values()) {
                    if (cause == consequent)
                        continue;
                    // consider the goal-pair (cause,consequent). Invoke the goal2goal-rule to see
                    // if
                    // the new likelihood of the cause-goal (if it changes at all) would influence
                    // the agent's belief on the likelihood of the consequence-goal:
                    Double newLikelihood = userModel.goalTowardsGoalRule.apply(beliefbase).apply(cause.goal.name)
                            .apply(consequent.goal.name);

                    if (newLikelihood != null) {
                        consequent.likelihood = newLikelihood;
                    }
                }
            }
        }
        // keep repeating while previous and new-belief are not likelihoods-equal:
        while (!beliefbase.getGoalsStatus().equals(previous));
    }

    /**
     * Apply decay on the emotions. Those whose intensity drop to zero will be
     * discarded. Be careful... this method directly change the emotions maintained
     * by this Transition Syatem.
     */
    private void applyTimeDecayToEmotions(long newtime) {
        for (Emotion e : emo) {
        	// Debug print? -->
        	if(e.etype==EmotionType.Fear && newtime>=120 && e.g.name=="quest is completed")
        	{
        		System.out.print("last intensity:"+ e.intensity);
        	} 

        	// use the decay-intensity-function to obtain the new intensity:
        	e.intensity = decayedIntesity(userModel, e.etype, e.intensity0, e.t0, newtime);
            
            if(e.etype==EmotionType.Fear && newtime>=120 && e.g.name=="quest is completed")
        	{
        		System.out.print("new intensity:"+ e.intensity);
        	} 
        }
        // remove those whose intensity drops to 0:
    }
    
    /**
     * As {@link #addInitialEmotions(boolean)}, but will always add initial
     * fear.
     */
    public void addInitialEmotions() {
    	addInitialEmotions(false) ;
    }

    /**
     * This will configure the emotion-state to contain initial-emotions towards
     * every goal in the agent's belief. Only prospect-based emotions (hope and
     * fear) will be initialized. The rationale is that having a goal implicitly
     * also means that the agent must have some hope of achieving it, however small
     * this hope could be. And similarly, it would have some fear as well, that the
     * goal might fail.
     * 
     * <p> If the flag withFear is true, the initial-fear is added, otherwise not.
     * 
     * TODO: this will crash, as the code pass a null-event. --> Update; should be
     * solved now as hope and fear do not actually inspect the triggering
     * event. So, null is ok.
     */
    public void addInitialEmotions(boolean withFear) {
        // adding initial prospect-based emotions, if configured to do so:
        Goals_Status goals = beliefbase.getGoalsStatus();
        for (GoalStatus gstat : goals.statuses.values()) {
            double initialIntensity = intensity(userModel, EmotionType.Hope, goals, null, gstat.goal.name);
            Emotion initialHope = new Emotion(EmotionType.Hope, gstat.goal, currentTime, initialIntensity);
            emo.add(initialHope);
            if (withFear) {
            	initialIntensity = intensity(userModel, EmotionType.Fear, goals, null, gstat.goal.name);
                Emotion initialFear = new Emotion(EmotionType.Fear, gstat.goal, currentTime, initialIntensity);
                emo.add(initialFear);
            }
        }
    }
    
    /**
     * Update this Transition System state, upon receiving an event e. Also specify
     * when the new time now.
     */
    public void update(Event e, long newtime) {

        if (newtime < currentTime)
            throw new IllegalArgumentException();

        Goals_Status goalsStatusBeforeUpdate = (Goals_Status) beliefbase.getGoalsStatus().clone();

        // Updating the current-time:
        currentTime = newtime;

        // Updating the beliefbase, after this update the current beliefbase is K_plus:
        userModel.eventEffect(e, beliefbase);

        // applying R* to apply chain-updates to goals-likelihood:
        // applyGoalTowardsGoalRule() ;

        List<Goal> goals = beliefbase.getGoalsStatus().statuses.values().stream().map(status -> status.goal)
                .collect(Collectors.toList());

        // first calculate new emotions that would emerge:

        newEmotions = new HashSet<Emotion>();
        Goals_Status goalsCurrentStatus = beliefbase.getGoalsStatus();
        for (Goal g : goals) {

            for (var etype : Emotion.emotionTypes) {

                // calculate the intensity:
                double w = EmotionFunction(userModel, etype, goalsStatusBeforeUpdate, goalsCurrentStatus, e, g.name, emo,
                        ememory);
                // add the emotion only if the intestity >0 :
                if (w > 0) {
                    Emotion em = new Emotion(etype, g, currentTime, w);
                    newEmotions.add(em);
                    // we add emerging emotion to the memory:
                    ememory.register(em);
                }
            }       
        }
        
        //store newly activated emotions with the corresponding event
        
        // now we apply time-decay to old emotions:
        applyTimeDecayToEmotions(currentTime);

        // Now merge the decayed ols emotions and new emotions:

        // First, obtain the old emotions, after the decay above, that would be retained
        // (not in conflict by newEmotions nor maximized by
        // newEmotions).
        Set<Emotion> retainedOldEmotion = new HashSet<>();
        for (Emotion emoOld : emo) {
            // drop if the intensity becomes 0
            if (emoOld.intensity <= 0)
                continue;

            // drop if the emotion occurs in the newEmotions:
            if (newEmotions.stream()
                    .anyMatch(emoNew -> emoNew.etype == emoOld.etype && emoNew.g.name.equals(emoOld.g.name)))

                // NOTE: we can alternatively drop only drop old emotions whose intensity is
                // lower or equal to new emotion
            	
                continue;
            // drop if a conflicting emotion occurs in the newEmotion:
            if (newEmotions.stream().anyMatch(
                    emoNew -> emoNew.g.name.equals(emoOld.g.name) && Emotion.exclusion(emoNew.etype, emoOld.etype)))
                continue;
            retainedOldEmotion.add(emoOld);
        }

        // For each new-emotion, compare its value with the corresponding
        // decayed-emotion. Take the maximum.
        // NOTE: not sure if this logic is sound. If old intensity is higher, then we
        // should take over the
        // old t0 as well. But in other words this mean we keep the old emotion and drop
        // the new emotion.
        for (Emotion emotion : newEmotions) {
            List<Emotion> results = emo.stream()
                    .filter(emoOld -> emotion.etype == emoOld.etype && emotion.g.name.equals(emoOld.g.name))
                    .collect(Collectors.toList());
            // Debug.Assert(results.Count() <= 1);

            if (results.size() == 0)
                continue;
            Emotion emoOld = results.get(0);
            emotion.intensity = Math.max(emotion.intensity, emoOld.intensity);
            if(emotion.intensity==emoOld.intensity)
            {
                emotion.intensity0= emoOld.intensity0;
                emotion.t0=emoOld.t0;

            }
            // debug print:
            if(emotion.etype==EmotionType.Fear &&emotion.intensity==emoOld.intensity&&emotion.g.name=="quest is completed")
        	{
        		System.out.print("Old entinisty was higher!!!!!: "+ emotion.intensity);
        	} 
            // what about if the decayed intenstiy is higher than the new one?
        }

        // merging:

        this.emo.clear();
        this.emo.addAll(newEmotions);
        this.emo.addAll(retainedOldEmotion);

        // clean up ememory:
        ememory.cleanup(currentTime);

        // cleanup goals that have been achieved: (i remove those goals that fail and
        // are
        // completed, because the corresponding new-emotions, if there is any,
        // have been taken into account above)
        List<GoalStatus> toBeRemoved = beliefbase.getGoalsStatus().statuses.values().stream()
                .filter(status -> status.isAchieved || status.isFailed).collect(Collectors.toList());

        for (var dropThis : toBeRemoved) {
            beliefbase.getGoalsStatus().statuses.remove(dropThis.goal.name);
        }
        
        if (turnOnDebugPrinting) {
        	System.out.println("** Update: " + e.name + ",time=" + currentTime);
            System.out.println("** Goals:");
            System.out.println(beliefbase.getGoalsStatus().toString());
            System.out.println(("** Emotion state:"));
            int k = 0;
            for (Emotion emotion : emo) {
                if (k > 0)
                    System.out.println("");
                System.out.println("   " + emotion);
            }
        }
    }

}
