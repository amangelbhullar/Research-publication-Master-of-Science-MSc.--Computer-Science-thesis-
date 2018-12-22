import java.io.*;
import java.util.*;

public class GraphIndexing {





        public static double MaxCalDouble = 100000;



        //Algorithm 2
        public void create2HopCover(
                String edgeHashFileName, double maxRDistance, int maxNumberOfNodesToBeIndexed, boolean isWeightedGraph, String folderLocation) {

            System.out.println("Start edgeHash");
            Map<Integer, Map<Integer, Double>> edgeHash = readGraphEdgeHash(edgeHashFileName, folderLocation);
            System.out.println("End edgeHash");
            int numberOfNodes = edgeHash.size();

            System.out.println("create2HopCover maxRDistance : " + maxRDistance);

            Map<Integer, List<Integer>> edgeSortedList = new HashMap<Integer, List<Integer>>();
            for (int i = 1; i <= numberOfNodes; i++) {
                List<Integer> eList = new ArrayList<Integer>(edgeHash.get(i).keySet());
                Collections.sort(eList);
                edgeSortedList.put(i, eList);
            }

            if (maxNumberOfNodesToBeIndexed < 0)
                maxNumberOfNodesToBeIndexed = numberOfNodes;

            try {
                //GraphNodeDegree

                Map<Integer, List<TwoHop>> twoHopMap = new HashMap<Integer, List<TwoHop>>();

                for (int i = 1; i <= numberOfNodes; i++) {
                    //Line 2
                    twoHopMap.put(i, new ArrayList<TwoHop>());
                }

                long startT = System.currentTimeMillis();

                //for(int j = 0; j < maxTitleRunResult; j ++){
                for (int i = 1; i <= maxNumberOfNodesToBeIndexed; i++) {

                    //int nodeId = titleList.get(j);
                    int nodeId = i;

                    //Lines 3-4
                    if (isWeightedGraph)
                        createPrunedDijkstra(edgeHash, edgeSortedList, twoHopMap, maxRDistance, nodeId);
                    else
                        createPrunedBFS(edgeHash, edgeSortedList, twoHopMap, maxRDistance, nodeId);

                    if (i % 100 == 1) {
                        //System.out.println("j : " + i + " vk " + (nodeTitleId) + " : " + twoHopMap.get(nodeTitleId).size() + " MilliSecond : " + ((System.currentTimeMillis() - startT)) ) ;
                        System.out.println("iii : " + i + " MilliSecond : " + ((System.currentTimeMillis() - startT)));
                        startT = System.currentTimeMillis();
                    }
                }

                //Write down the index
                BufferedWriter bwSDist1 = new BufferedWriter(new FileWriter(folderLocation + "/2HopCoverDist_" + maxNumberOfNodesToBeIndexed + "_" + maxRDistance + "_" + edgeHashFileName));
                for (int nodeId : twoHopMap.keySet()) {

                    bwSDist1.write(nodeId + " ");

                    List<TwoHop> tList = twoHopMap.get(nodeId);
                    for (TwoHop th : tList) {
                        bwSDist1.write(th.nodeId + "#" + th.dist + "#" + th.parentId + " ");
                    }

                    bwSDist1.write("\n");
                    bwSDist1.flush();
                }
                bwSDist1.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return;
        }

        public Map<Integer, Map<Integer, Double>> readGraphEdgeHash(String edgeHashFileName, String folderLocation) {
            String line = null;

            Map<Integer, Map<Integer, Double>> edgeHash = new HashMap<Integer, Map<Integer, Double>>();

            int edgeCount = 0;

            try {
                BufferedReader input = new BufferedReader(new FileReader(new File(folderLocation + "/" + edgeHashFileName)));

                while (((line = input.readLine()) != null)) {
                    line = line.trim();
                    String[] row = line.split("\t");

                    edgeHash.put(Integer.parseInt(row[0]), new HashMap<Integer, Double>());

                    for (int i = 1; i < row.length; i++) {
                        String[] neighborDist = row[i].split("#");
                        edgeHash.get(Integer.parseInt(row[0])).put(Integer.parseInt(neighborDist[0]), Double.parseDouble(neighborDist[1]));
                        edgeCount++;
                    }
                }

                input.close();
            } catch (Exception ex) {
                System.out.print(ex.getMessage() + "\n");
            }

            return edgeHash;
        }

        //Algorithm 1
        public void createPrunedBFS(Map<Integer, Map<Integer, Double>> edgeHash, Map<Integer, List<Integer>> edgeSortedList,
                                    Map<Integer, List<TwoHop>> twoHopMap, double maxRDistance, int vkId) {
            int numberOfNodes = edgeHash.size();

            //Line 2
            Queue<Integer> queue = new LinkedList<Integer>();
            queue.add(vkId);

            //Line 3
            Map<Integer, Double> PMap = new HashMap<Integer, Double>();
            Map<Integer, Integer> PMapNodePath = new HashMap<Integer, Integer>();
            for (int i = 1; i <= numberOfNodes; i++) {
                PMap.put(i, MaxCalDouble);
                PMapNodePath.put(i, -1);
            }
            PMap.put(vkId, 0.0);
            PMapNodePath.put(vkId, vkId);

            //Lines 5-12
            while (!queue.isEmpty()) {
                //Line 6
                int uId = queue.remove();

                //Lines 7-8
                double uPDist = PMap.get(uId);
                double queryDist = queryTwoHop(twoHopMap, vkId, uId);
                if (queryDist <= uPDist)
                    continue;

                //Line 9
                //add in sorted order, based on the paper, it is automatically sorted
                twoHopMap.get(uId).add(new TwoHop(vkId, uPDist, PMapNodePath.get(uId)));

                //Line 10
                for (int wId : edgeSortedList.get(uId)) {
                    if (PMap.get(wId) == MaxCalDouble) {

                        //Line 11
                        double wDist = uPDist + 1.0;
                        if (wDist <= maxRDistance) {
                            PMap.put(wId, wDist); //1.0 will be replaced by the actual edge weight from edgeHash
                            PMapNodePath.put(wId, uId);
                            //Line 12
                            queue.add(wId);
                        }
                    }
                }
            }
        }

        //Algorithm 1 (modified for weighted graphs)
        Map<Integer, Double> shortestDistances;

        public void createPrunedDijkstra(Map<Integer, Map<Integer, Double>> edgeHash, Map<Integer, List<Integer>> edgeSortedList,
                                         Map<Integer, List<TwoHop>> twoHopMap, double maxRDistance, int vkId) {

            int numberOfNodes = edgeHash.size();

            Set<Integer> settledNodes = new HashSet<Integer>();
            shortestDistances = new HashMap<Integer, Double>();

            NavigableSet<Integer> unsettledNodesTS =
                    new TreeSet<Integer>(
                            new Comparator<Integer>() {
                                public int compare(Integer a1, Integer a2) {

                                    double result = getShortestDistanceDijkstra(a1, shortestDistances) - getShortestDistanceDijkstra(a2, shortestDistances);

                                    if (result > 0)
                                        return +1;
                                    else
                                        return -1;
                                }
                            }
                    );


            //Line 2
            //Queue<Integer> queue = new LinkedList<Integer>();
            //queue.add(vkId);
            setShortestDistanceDijkstra(vkId, 0, unsettledNodesTS, shortestDistances);
            unsettledNodesTS.add(vkId);

            //Line 3
            Map<Integer, Double> PMap = new HashMap<Integer, Double>();
            Map<Integer, Integer> PMapNodePath = new HashMap<Integer, Integer>();
            for (int i = 1; i <= numberOfNodes; i++) {
                PMap.put(i, MaxCalDouble);
                PMapNodePath.put(i, -1);
            }
            PMap.put(vkId, 0.0);
            PMapNodePath.put(vkId, vkId);

            //Lines 5-12
            while (!unsettledNodesTS.isEmpty()) {
                //Line 6
                //int uId = unsettledNodesQueue.remove();
                int uId = unsettledNodesTS.pollFirst();

                //Lines 7-8
                double uPDist = PMap.get(uId);
                double queryDist = queryTwoHop(twoHopMap, vkId, uId);
                if (queryDist <= uPDist)
                    continue;

                //Line 9
                //add in sorted order, based on the paper, it is automatically sorted
                twoHopMap.get(uId).add(new TwoHop(vkId, uPDist, PMapNodePath.get(uId)));

                //Line 10
                for (int wId : edgeSortedList.get(uId)) {

                    if (settledNodes.contains(wId)) continue;

                    if (PMap.get(wId) == MaxCalDouble) {

                        double wDist = getShortestDistanceDijkstra(uId, shortestDistances) + edgeHash.get(uId).get(wId);
                        wDist = Math.rint(wDist * 10000.0d) / 10000.0d;

                        //Line 11
                        if (wDist <= maxRDistance) {
                            PMap.put(wId, wDist);
                            PMapNodePath.put(wId, uId);
                            //Line 12
                            setShortestDistanceDijkstra(wId, wDist, unsettledNodesTS, shortestDistances);
                        }
                    }
                }
            }
        }

        public double queryTwoHop(Map<Integer, List<TwoHop>> twoHopMap, int sId, int tId) {

            double resultDist = MaxCalDouble;

            List<TwoHop> sList = twoHopMap.get(sId);
            List<TwoHop> tList = twoHopMap.get(tId);

            int si = 0, ti = 0;

            while (si < sList.size() && ti < tList.size()) {

                int nodeSId = sList.get(si).nodeId;

                int nodeTId = tList.get(ti).nodeId;

                if (nodeSId < nodeTId) { // s < t
                    si++;
                } else if (nodeSId > nodeTId) {    // s > t
                    ti++;
                } else {
                    double currentDistS = sList.get(si).dist;
                    double currentDistT = tList.get(ti).dist;
                    double currentDistTotal = currentDistS + currentDistT;

                    if (currentDistTotal < resultDist)
                        resultDist = currentDistTotal;
                    si++;
                    ti++;
                }
            }

            return resultDist;
        }

        public double getShortestDistanceDijkstra(int nodeId, Map<Integer, Double> shortestDistances) {
            Double d = shortestDistances.get(nodeId);
            return (d == null) ? MaxCalDouble : d;
        }

        private void setShortestDistanceDijkstra(int nodeId, double distance, NavigableSet<Integer> unsettledNodesTS, Map<Integer, Double> shortestDistances) {
            unsettledNodesTS.remove(nodeId);

            shortestDistances.put(nodeId, distance);

            //Re-balance the queue according to the new shortest distance
            unsettledNodesTS.add(nodeId);
        }

        public double queryTwoHopArray(Map<Integer, TwoHop[]> twoHopMap, int sId, int tId) {

            double resultDist = MaxCalDouble;

            TwoHop[] sList = twoHopMap.get(sId);
            TwoHop[] tList = twoHopMap.get(tId);

            int si = 0, ti = 0;

            while (si < sList.length && ti < tList.length) {

                int nodeSId = sList[si].nodeId;

                int nodeTId = tList[ti].nodeId;

                if (nodeSId < nodeTId) { // s < t
                    si++;
                } else if (nodeSId > nodeTId) {    // s > t
                    ti++;
                } else {
                    double currentDistS = sList[si].dist;
                    double currentDistT = tList[ti].dist;
                    double currentDistTotal = currentDistS + currentDistT;

                    if (currentDistTotal < resultDist)
                        resultDist = currentDistTotal;
                    si++;
                    ti++;
                }
            }

            return resultDist;
        }

        public Map<Integer, TwoHop[]> readTwoHopIndexArray(String twoHopIndexFileName, String folderName) {
            String lineD = null;

            Map<Integer, TwoHop[]> twoHopMap = new HashMap<Integer, TwoHop[]>();

            try {
                BufferedReader inputDist = new BufferedReader(new FileReader(new File(folderName + "/" + twoHopIndexFileName)));

                while (((lineD = inputDist.readLine()) != null)) {

                    lineD = lineD.trim();

                    String[] rowD = lineD.split(" ");

                    int currentId = Integer.parseInt(rowD[0]);

                    twoHopMap.put(currentId, new TwoHop[rowD.length - 1]);

                    for (int i = 1; i < rowD.length; i++) {
                        String[] neighborDist = rowD[i].split("#");
                        twoHopMap.get(currentId)[i - 1] = new TwoHop(Integer.parseInt(neighborDist[0]), Double.parseDouble(neighborDist[1]), Integer.parseInt(neighborDist[2]));
                    }
                }

                inputDist.close();
            } catch (Exception ex) {
                System.out.print(ex.getMessage() + "\n");
            }

            return twoHopMap;
        }


        public Map<Integer, String> createFixedValuesNodeHash(Map<Integer, String> nodeHash) {
            Map<Integer, String> fixedValuesNodeHash = new HashMap<Integer, String>();

            for (int nodeId : nodeHash.keySet()) {
                String nodeValue = nodeHash.get(nodeId);

                nodeValue = nodeValue.replaceAll("'", "");
                nodeValue = nodeValue.replaceAll("=", "");

                nodeValue = nodeValue.replaceAll("[{]", "");
                nodeValue = nodeValue.replaceAll("[}]", "");

                nodeValue = nodeValue.replaceAll("\\[", "");
                nodeValue = nodeValue.replaceAll("]", "");

                if (nodeValue.startsWith("cast")) {
                    nodeValue = "cast";
                }

                if (nodeValue.startsWith("name")) {
                    nodeValue = nodeValue.replaceAll(",", "");
                    String[] nn = nodeValue.split(" ");
                    if (nn.length > 2)
                        nodeValue = "name: " + nn[2].trim() + " " + nn[1].trim();
                    else
                        nodeValue = "name: " + nn[1].trim();
                }

                if (nodeValue.startsWith("title")) {
                    String[] nn = nodeValue.split(" ");
                    nodeValue = "title: ";
                    boolean meetNull = false;
                    for (int i = 1; i < nn.length - 1; i++) {
                        if (nn[i].equals("null")) {
                            meetNull = true;
                        } else {
                            if (!meetNull)
                                nodeValue += " " + nn[i];
                        }
                    }
                }

                fixedValuesNodeHash.put(nodeId, nodeValue);
            }

            return fixedValuesNodeHash;
        }

        public static Set<String> getStopWordsSet() {

            Set<String> notIndexSet = new HashSet<String>();

        /*
        notIndexSet.add("of");
        notIndexSet.add("for");
        notIndexSet.add("and");
        notIndexSet.add("in");
        notIndexSet.add("the");
        notIndexSet.add("on");
        notIndexSet.add("with");
        notIndexSet.add("to");
        notIndexSet.add("using");
        notIndexSet.add("an");
        */

            return notIndexSet;
        }


        //sId--parentIdS--.....--connectorNodeId--.....--parentIdT--tId
        public List<Integer> getFullPathTwoHopArray(
                Map<Integer, TwoHop[]> twoHopMap,
                Map<Integer, String> nodeHash,
                int sId,
                int tId,
                List<Integer> currentList,
                int prevSId,
                int prevTId
        ) {

            if (prevSId == sId && prevTId == tId) {
                return currentList;
            }

            if (currentList == null) {
                currentList = new ArrayList<Integer>();
                currentList.add(sId);
                if (sId != tId)
                    currentList.add(tId);
            }

            String sText = nodeHash.get(sId);
            String tText = nodeHash.get(tId);

            int[] connectorParentArray = pathTwoHopArrayNew(twoHopMap, sId, tId);

            int connectorNodeId = connectorParentArray[0];
            int parentIdS = connectorParentArray[1];
            int parentIdT = connectorParentArray[2];

            String connectorNodeIdText = nodeHash.get(connectorNodeId);
            String parentIdSText = nodeHash.get(parentIdS);
            String parentIdTText = nodeHash.get(parentIdT);

            //build the s path
            if (connectorNodeId == sId) {
                //do nothing and return.
            } else {
                if (parentIdS == sId) {
                    //only insert connectorNodeId after sId
                    if (!currentList.contains(connectorNodeId))
                        currentList = insertOneElementAfterGivenOne(currentList, sId, connectorNodeId);

                    //call between sId(==parentIdS) and connectorNodeId
                    currentList = getFullPathTwoHopArray(twoHopMap, nodeHash, sId, connectorNodeId, currentList, sId, tId);
                } else {
                    //first, insert parentIdS after sId
                    if (!currentList.contains(parentIdS))
                        currentList = insertOneElementAfterGivenOne(currentList, sId, parentIdS);

                    //second, insert connectorNodeId after parentIdS
                    if (!currentList.contains(connectorNodeId))
                        currentList = insertOneElementAfterGivenOne(currentList, parentIdS, connectorNodeId);

                    //third, call method between parentIdS and connectorNodeId
                    currentList = getFullPathTwoHopArray(twoHopMap, nodeHash, parentIdS, connectorNodeId, currentList, sId, tId);
                }
            }

            //build the t path
            if (connectorNodeId == tId) {
                //do nothing and return.
            } else {
                if (parentIdT == tId) {

                    //only insert connectorNodeId before tId
                    if (!currentList.contains(connectorNodeId))
                        currentList = insertOneElementBeforeGivenOne(currentList, tId, connectorNodeId);

                    //call between connectorNodeId and tId(==parentIdT)
                    currentList = getFullPathTwoHopArray(twoHopMap, nodeHash, connectorNodeId, tId, currentList, sId, tId);
                } else {
                    //first, insert parentIdT before tId
                    if (!currentList.contains(parentIdT))
                        currentList = insertOneElementBeforeGivenOne(currentList, tId, parentIdT);

                    //second, insert connectorNodeId before parentIdT
                    if (!currentList.contains(connectorNodeId))
                        currentList = insertOneElementBeforeGivenOne(currentList, parentIdT, connectorNodeId);

                    //third, call method between connectorNodeId and parentIdT
                    currentList = getFullPathTwoHopArray(twoHopMap, nodeHash, connectorNodeId, parentIdT, currentList, sId, tId);
                }
            }

            //this is the last line
            return currentList;
        }

        public int[] pathTwoHopArrayNew(Map<Integer, TwoHop[]> twoHopMap, int sId, int tId) {

            double resultDist = MaxCalDouble;
            int connectorNodeId = -1;
            int parentIdS = -1;
            int parentIdT = -1;

            TwoHop[] sList = twoHopMap.get(sId);
            TwoHop[] tList = twoHopMap.get(tId);

            int si = 0, ti = 0;

            while (si < sList.length && ti < tList.length) {

                int nodeSId = sList[si].nodeId;

                int nodeTId = tList[ti].nodeId;

                if (nodeSId < nodeTId) { // s < t
                    si++;
                } else if (nodeSId > nodeTId) {    // s > t
                    ti++;
                } else {
                    double currentDistS = sList[si].dist;
                    double currentDistT = tList[ti].dist;
                    double currentDistTotal = currentDistS + currentDistT;

                    if (currentDistTotal < resultDist) {
                        resultDist = currentDistTotal;
                        connectorNodeId = nodeSId;

                        parentIdS = sList[si].parentId;
                        parentIdT = tList[ti].parentId;
                    }
                    si++;
                    ti++;
                }
            }

            //the first element is nodeId, the second element is parentId
            int[] result = new int[3];
            result[0] = connectorNodeId;
            result[1] = parentIdS;
            result[2] = parentIdT;

            return result;
        }

        public List<Integer> insertOneElementBeforeGivenOne(List<Integer> inputList, int insertBeforeElement, int newElement) {

            int newIndex = -1;
            for (int i = 0; i < inputList.size(); i++) {
                if (inputList.get(i) == insertBeforeElement) {
                    newIndex = i;
                }
            }

            List<Integer> outputList = new ArrayList<Integer>();
            for (int i = 0; i < newIndex; i++) {
                outputList.add(inputList.get(i));
            }

            outputList.add(newElement);

            for (int i = newIndex; i < inputList.size(); i++) {
                outputList.add(inputList.get(i));
            }

            return outputList;
        }

        public List<Integer> insertOneElementAfterGivenOne(List<Integer> inputList, int insertAfterElement, int newElement) {

            int newIndex = -1;
            for (int i = 0; i < inputList.size(); i++) {
                if (inputList.get(i) == insertAfterElement) {
                    newIndex = i;
                }
            }

            List<Integer> outputList = new ArrayList<Integer>();
            for (int i = 0; i < newIndex + 1; i++) {
                outputList.add(inputList.get(i));
            }

            outputList.add(newElement);

            for (int i = newIndex + 1; i < inputList.size(); i++) {
                outputList.add(inputList.get(i));
            }

            return outputList;
        }

    }




