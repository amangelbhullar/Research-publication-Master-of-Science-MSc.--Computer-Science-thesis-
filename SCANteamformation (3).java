import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLOutput;
import java.util.*;

public class SCANteamformation {

    public static void main(String arg[])throws Exception {
        String folderName = "C:\\Users\\AmangelB\\IdeaProjects\\Finalthesis\\src\\50K";
        String twoHopFileName = "2HopCoverDist_1000.0_G1_50K.txt";
        String filename = "invertedTermMap.txt";
        String graphG1 = "G1.txt";
        String filePath = folderName + "/" + filename;
        String filepath1 = folderName + "/" + graphG1;
        String line;

        MethodsForSCAN mds = new MethodsForSCAN();

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

//        System.out.println(reqSkill);

        List<Expertskills> PoolofExperts = mds.CollectAllRequiredExpert(reqSkill, invertedmap);

        System.out.println("initial Pool:");
        for (Expertskills ex : PoolofExperts) {
            System.out.print(ex.skills + ": ");
            for (int j = 0; j < ex.experts.length; j++) {
                System.out.print(ex.experts[j] + ",");
            }
            System.out.println();
        }


        //store all experts without skill into a list

        List<Integer> Expertsonly = new ArrayList<Integer>();

        for (Expertskills es : PoolofExperts) {
            for (int j = 0; j < es.experts.length; j++) {
                Expertsonly.add(es.experts[j]);
            }
        }

        FileReader fr = new FileReader(filepath1);
        BufferedReader br = new BufferedReader(fr);
        List<Expertskills> ExpertCounting = new ArrayList<Expertskills>();
        String lineE = br.readLine();
        int count = 0;

        while (lineE != null) {
            String[] parts = lineE.split("\\s+");
            Expertskills ex = new Expertskills();
            int Count = parts.length;
            int tempExpertid = Integer.parseInt(parts[0]);

            for (String w : parts) {
                count++;
            }

            for (int j = 0; j < Expertsonly.size(); j++) {
                if (tempExpertid == Expertsonly.get(j)) {
                    ex.expertId = tempExpertid;
                    ex.totconn = count;
                    ExpertCounting.add(ex);
                }

            }

            lineE = br.readLine();
            count = 0;
        }


        for (Expertskills ex : ExpertCounting) {
            System.out.print(ex.expertId + ": " + ex.totconn);
            System.out.println();
        }

        Collections.sort(ExpertCounting, Expertskills.DegreeComparator());
        System.out.println("After short");
        int highdegree = ExpertCounting.get((ExpertCounting.size()) - 1).totconn;

        // if more than one node have the highest degree, collect all into list
        List<Integer> CoreGroup = new ArrayList<Integer>();
        for (int i = 0; i < ExpertCounting.size(); i++) {
            if (ExpertCounting.get(i).totconn == highdegree) {
                CoreGroup.add(ExpertCounting.get(i).expertId);
            }
        }

        // then choose random node from all core nodes
        int CORE = 0;
        for (int i = 0; i < CoreGroup.size(); i++) {
            CORE = mds.getRandomList(CoreGroup);
        }
        System.out.println("CORE:" + CORE);


       String skillofcore="";
          for (Expertskills ex : PoolofExperts) {
              for (int j = 0; j < ex.experts.length; j++) {
                  if(ex.experts[j]==CORE)
                      skillofcore=ex.skills;
              }

          }

          reqSkill.remove(skillofcore);

       System.out.println(reqSkill);
        PoolofExperts = mds.CollectAllRequiredExpert(reqSkill, invertedmap);
        for (Expertskills ex : PoolofExperts) {
            System.out.print(ex.skills + ": ");
            for (int j = 0; j < ex.experts.length; j++) {
                System.out.print(ex.experts[j] + ",");
            }
            System.out.println();
        }

        GraphIndexing gi = new GraphIndexing();
        Map<Integer, TwoHop[]> twoHopMap = gi.readTwoHopIndexArray(twoHopFileName, folderName);
        List<Expert> DistwithCORE = new ArrayList<>();
        double[] allDist = new double[ExpertCounting.size()];
        for (int i = 0; i < ExpertCounting.size() - 1; i++) {
            double dist = gi.queryTwoHopArray(twoHopMap, ExpertCounting.get(i).expertId, CORE);
            Expert ex = new Expert();
            ex.distance = dist;
            ex.expertId = ExpertCounting.get(i).expertId;
            DistwithCORE.add(ex);
            allDist[i] = dist;
        }

        Collections.sort(DistwithCORE, Expert.DistComparator());
    }


    // group by distance
    Map<Double, List<Expert>> expertByDist = new HashMap<>();
            for (Expert p : DistwithCORE) {
        if (!expertByDist.containsKey(p.getDistance())) {
            expertByDist.put(p.getDistance(), new ArrayList<>());
        }
        expertByDist.get(p.getDistance()).add(p);
    }

    List<Integer> clusterwDist=new ArrayList<Integer>();
    List<Expertskills> teamofExperts = new ArrayList<Expertskills>();
    List<Integer> clusterwDist1=new ArrayList<Integer>();
               for (Map.Entry<Double, List<Expert>> entry : expertByDist.entrySet()) {
        Double key = entry.getKey();
        List<Expert> value = entry.getValue();
        for (int i = 0; i < value.size(); i++) {
            if (key == 0.0) {
                clusterwDist.add(value.get(i).getExpertId());
            }
            if(key<=3.0)
            {
                clusterwDist1.add(value.get(i).getExpertId());
            }
        }

        // find the skill of experts who have communication cost 1.0
        List<Expertskills> skillofCluster=new ArrayList<Expertskills>();
        for(int k=0; k<clusterwDist.size();k++) {
            for (Expertskills ex : PoolofExperts) {
                for (int j = 0; j < ex.experts.length; j++) {
                    if (clusterwDist.get(k) == ex.experts[j]) {
                        Expertskills exx = new Expertskills();
                        exx.expertId = ex.experts[j];
                        exx.skills = ex.skills;
                        skillofCluster.add(exx);
                        teamofExperts.add(exx);
                    }
                }
            }
        }
        List<Expertskills> skillofCluster1=new ArrayList<Expertskills>();
        for(int k=0; k<clusterwDist1.size();k++) {
            for (Expertskills ex : PoolofExperts) {
                for (int j = 0; j < ex.experts.length; j++) {
                    if (clusterwDist1.get(k) == ex.experts[j]) {
                        Expertskills exx = new Expertskills();
                        exx.expertId = ex.experts[j];
                        exx.skills = ex.skills;
                        skillofCluster1.add(exx);
                    }
                }

    }



}
