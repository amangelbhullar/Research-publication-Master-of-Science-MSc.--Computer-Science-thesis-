import java.util.Comparator;
import java.util.Map;

public class GeneAnswer {


        //Genetic
        public int[] gene;
        public double fitnessScore;

        //Greedy
        public double sumDistWeight;
        public double diameterWeight;
        public int rootId;
        Map<String, Integer> answerMap;

        public static Comparator<GeneAnswer> FitnessScoreComparator(){
            Comparator comp = new Comparator<GeneAnswer>(){
                public int compare(GeneAnswer s1, GeneAnswer s2){
                    if(s1.fitnessScore > s2.fitnessScore)
                        return 1;
                    if(s1.fitnessScore < s2.fitnessScore)
                        return -1;
                    return 0;
                }
            };
            return comp;
        }

        public static Comparator<GeneAnswer> SumDistWeightComparator(){
            Comparator comp = new Comparator<GeneAnswer>(){
                public int compare(GeneAnswer s1, GeneAnswer s2){
                    if(s1.sumDistWeight > s2.sumDistWeight)
                        return 1;
                    if(s1.sumDistWeight < s2.sumDistWeight)
                        return -1;
                    return 0;
                }
            };
            return comp;
        }

        public static Comparator<GeneAnswer> DiameterWeightComparator(){
            Comparator comp = new Comparator<GeneAnswer>(){
                public int compare(GeneAnswer s1, GeneAnswer s2){
                    if(s1.diameterWeight > s2.diameterWeight)
                        return 1;
                    if(s1.diameterWeight < s2.diameterWeight)
                        return -1;
                    return 0;
                }
            };
            return comp;
        }


}
