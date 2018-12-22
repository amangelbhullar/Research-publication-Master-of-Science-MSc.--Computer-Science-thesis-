import java.util.*;

public class GeneticAndCulturalonly {
    public static void main(String arg[]) throws Exception {
        String folderName = "C:\\Users\\AmangelB\\IdeaProjects\\Finalthesis\\src\\50K";
        String twoHopFileName = "2HopCoverDist_1000.0_G1_50K.txt";
        String filename = "invertedTermMap.txt";
        String graphG1 = "G1.txt";
        String filePath = folderName + "/" + filename;
        String filepath1 = folderName + "/" + graphG1;
        String line;
        int population = 100;
        int IterationNum = 20;
        int TopAnswersToBeUsedInCrossOver = (20 * population) / 100;
        int TopEliteToMoveToNextIteration = (2 * population) / 100;

        MethodsForSCAN mds = new MethodsForSCAN();
        GraphIndexing gi = new GraphIndexing();
        Map<Integer, TwoHop[]> twoHopMap = gi.readTwoHopIndexArray(twoHopFileName, folderName);

        Map<String, List<Integer>> invertedmap = mds.getInvertedIndexMap(folderName, filename);

        for (Map.Entry<String, List<Integer>> entry : invertedmap.entrySet()) {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();

//            System.out.println(" Skill:"+ key +"-"+ value);

        }

        //create a list to store list of required skill for the project
        List<String> reqSkill = new ArrayList<String>();

        reqSkill.add("technique");
        reqSkill.add("workflows");
        reqSkill.add("activities");
        reqSkill.add("communities");
        reqSkill.add("recursive");
        List<Expertskills> teamofExperts = new ArrayList<Expertskills>();

        List<List<Expertskills>> populationlist = new ArrayList<>();
        int index = 0;
        int k = 0;
        populationlist.add(new ArrayList<Expertskills>());
        for (int i = 0; i < population; i++) {
            Expertskills ga = new Expertskills();

            for (int j = 0; j < reqSkill.size(); j++) {
                index = mds.randInt(0, PoolofExperts.get(k).experts.length - 1, -1);
                ga.experts[j] = PoolofExperts.get(k).experts[index];
            }
            populationlist.get(0).add(ga);
            k = 0;
        }
        for (int i = 0; i < IterationNum; i++) {
            for (int j = 0; j < populationlist.get(i).size(); j++) {
                double fitness = mds.calculateFitness(populationlist.get(i).get(j).experts, twoHopMap);
                populationlist.get(i).get(j).fitnessScore = fitness;
            }
            Collections.sort(populationlist.get(i), Expertskills.FitnessScoreComparator());
            for (int J = 0; J < population; J++) {
                System.out.println(Arrays.toString(populationlist.get(0).get(J).experts) + "  " + populationlist.get(0).get(J).fitnessScore);
            }

//Add elite to the new population
            List<Expertskills> newIterationList = new ArrayList<Expertskills>();
            for (int j = 0; j < TopEliteToMoveToNextIteration; j++) {
                newIterationList.add(populationlist.get(i).get(j));
            }

            for (int g = TopEliteToMoveToNextIteration; g < population; g++) {
//Create list of selected experts for each skill to be used in cultural algorithm
                List<List<Integer>> selectedExpertsList = new ArrayList<List<Integer>>();
                for (int s = 0; s < reqSkill.size(); s++) {

                    List<Integer> SelectedExperts = new ArrayList<Integer>();
                    for (int f = 0; f < TopAnswersToBeUsedInCrossOver; f++) {
                        SelectedExperts.add(populationlist.get(i).get(f).experts[s]);
                    }
                    selectedExpertsList.add(SelectedExperts);
                }
                System.out.println(selectedExpertsList);
//        if(1==1)
//            return;
                for (int p = TopEliteToMoveToNextIteration; p < population; p++) {
                    int rand1 = mds.randInt(0, 100, -1);
                    if (rand1 <= 80) {
                        //CULTURAL ALGORITHM


                        int[] children = null;
                        int maxAttempt = 0;

                        children = new int[reqSkill.size()];
                        for (int s = 0; s < reqSkill.size(); s++) {
                            List<Integer> SelectedExperts = selectedExpertsList.get(s);
                            int randomIndex = mds.randInt(0, SelectedExperts.size() - 1, -1);
                            children[s] = SelectedExperts.get(randomIndex);
                        }
                        Expertskills newGA1 = new Expertskills();
                        newGA1.experts = children;
                        newIterationList.add(newGA1);

                    } else {


                        //GENETIC ALGORITHM
                        int rand2 = mds.randInt(1, 100, -1);
                        if (rand2 <= 0) {


                            //CROSSOVER

                            int[] children = null;
                            int maxAttempt = 0;


                            int G1 = -1;
                            int G2 = -1;

                            G1 = mds.randInt(0, TopAnswersToBeUsedInCrossOver - 1, -1);
                            G2 = mds.randInt(0, TopAnswersToBeUsedInCrossOver - 1, -1);

                            children = mds.crossoverFunction1(populationlist.get(i).get(G1).experts, populationlist.get(i).get(G2).experts);
//            System.out.println("gene1");
//            System.out.println(Arrays.toString(populationlist.get(i).get(G1).experts));
//            System.out.println("gene2");
//            System.out.println(Arrays.toString(populationlist.get(i).get(G2).experts));
//            System.out.println("children");
//            System.out.println(Arrays.toString(children));
//
//         if(1==1)
//         return;

                            Expertskills newGA = new Expertskills();
                            newGA.experts = children;
                            newIterationList.add(newGA);

                        }
//
                        else {

                            int[] newGene = null;
                            int G = mds.randInt(0, TopAnswersToBeUsedInCrossOver - 1, -1);

//            System.out.println(Arrays.toString(populationlist.get(i).get(G).experts));
                            int randomSkill = mds.randInt(0, reqSkill.size() - 1, -1);
                            int randomIndex = mds.randInt(0, PoolofExperts.get(randomSkill).experts.length - 1, -1);
                            int randomExpert = PoolofExperts.get(randomSkill).experts[randomIndex];


//            System.out.println(PoolofExperts.get(randomSkill).experts[randomIndex]);

                            newGene = populationlist.get(i).get(G).experts.clone();
                            newGene[randomSkill] = randomExpert;

//            System.out.println(Arrays.toString(newGene));

                            Expertskills newGA = new Expertskills();
                            newGA.experts = newGene;
                            newIterationList.add(newGA);
                        }
                    }

                    //Add new population
                    populationlist.add(newIterationList);
                }


            }
        }
    }
