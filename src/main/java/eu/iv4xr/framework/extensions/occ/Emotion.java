package eu.iv4xr.framework.extensions.occ;

import java.util.*;

/**
 * Representing a single instance of the state of some emotion type.
 * @author sansari
 */
public class Emotion {

    /**
     * Different types of emotion supported by this model.
     */
    public enum EmotionType {
        Joy, Distress, Hope, Fear, Disappointment, Satisfaction
    }

    /**
     * Just a constant array holding all possible emotion types, so that we can
     * quantify over it.
     */
    public static EmotionType[] emotionTypes = { EmotionType.Joy, EmotionType.Distress, EmotionType.Hope,
            EmotionType.Fear, EmotionType.Disappointment, EmotionType.Satisfaction };

    //
    // Bunch of mutual exclusion axioms:
    //

    // static List<EmotionType> exclusionAxiom1 = Arrays.asList(EmotionType.Joy,
    // EmotionType.Distress) ;
    // staticList<EmotionType> exclusionAxiom2 = Arrays.asList(EmotionType.Hope,
    // EmotionType.Fear) ;
    // static List<EmotionType> exclusionAxiom3 =
    // Arrays.asList(EmotionType.Satisfaction, EmotionType.Disappointment);
    // static List<EmotionType> exclusionAxiom4 = Arrays.asList(EmotionType.Joy,
    // EmotionType.Hope, EmotionType.Satisfaction);
    // staticList<EmotionType> exclusionAxiom5 = Arrays.asList(EmotionType.Distress,
    // EmotionType.Fear, EmotionType.Disappointment);
    static List<EmotionType> exclusionAxiomessential1 = Arrays.asList(EmotionType.Joy, EmotionType.Hope);
    static List<EmotionType> exclusionAxiomessential2 = Arrays.asList(EmotionType.Distress, EmotionType.Fear);

    /**
     * Return true if the two emotions (towards the same target) should be mutually
     * exclusive. That is, if one is present, the other should not.
     */
    public static boolean exclusion(EmotionType ety1, EmotionType ety2) {
        if (ety1 == ety2)
            return false;

        /*
         * return exclusionAxiom1.contains(ety1) && exclusionAxiom1.contains(ety2) ||
         * exclusionAxiom2.contains(ety1) && exclusionAxiom2.contains(ety2) ||
         * exclusionAxiom3.contains(ety1) && exclusionAxiom3.contains(ety2) ||
         * exclusionAxiom4.contains(ety1) && exclusionAxiom4.contains(ety2) ||
         * exclusionAxiom5.contains(ety1) && exclusionAxiom5.contains(ety2);
         */
        return exclusionAxiomessential1.contains(ety1) && exclusionAxiomessential1.contains(ety2)
                || exclusionAxiomessential2.contains(ety1) && exclusionAxiomessential2.contains(ety2);
    }

    public EmotionType etype;
    public Goal g;

    /**
     * Representing the time when the emotion is triggered.
     */
    public long t0;

    /**
     * A number in the range of 0..100 representing this emotion's current
     * intensity.
     */
    public double intensity;

    /**
     * The initial intensity of this emotion when it first triggered at time t0.
     */
    public double intensity0;

    public Emotion(EmotionType ety, Goal g, long timeWhenTrigerred, double initialIntensity) {
        this.etype = ety;
        this.g = g;
        this.t0 = timeWhenTrigerred;
        this.intensity = initialIntensity;
        this.intensity0 = initialIntensity;
    }

    @Override
    public String toString() {
        return "" + etype + "-> " + g.name + " (" + t0 + "): " + intensity;
    }

    /**
     * Make a copy of this Emotion. The goal is NOT deep-copied.
     */
    public Emotion shallowClone() {
        Emotion e = new Emotion(this.etype, this.g, this.t0, this.intensity0);
        e.intensity = this.intensity;
        return e;
    }

}
