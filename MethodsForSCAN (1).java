import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MethodsForSCAN {

    public HashMap<String,List<Integer>> getInvertedIndexMap(String foldername, String filename)
    {
        String filePath =foldername+"\\"+filename;
        String line;

        HashMap<String,List<Integer>> map = new HashMap<String, List<Integer>>();
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\\s");

                List<Integer> alternateList = new ArrayList<Integer>();

                for(int i=1;i<columns.length;i++) {
                    alternateList.add(Integer.parseInt(columns[i].trim()));
                }
                map.put(columns[0],alternateList);

            }
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" +filePath + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '"+ filePath + "'");

        }

        return map;
    }


    public  List<Expertskills> CollectAllRequiredExpert(List<String> reqSkill,Map<String,List<Integer>> map)
    {
        List<Expertskills> PoolofExperts = new ArrayList<Expertskills>();

        for (int i=0;i<reqSkill.size();i++)
        {
            Expertskills ex=new Expertskills();
            for(Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<Integer> value = entry.getValue();
                if ( reqSkill.get(i).equals(key)){
                    ex.skills = key;

                    int l= value.size();
                    int x=0;
                    ex.experts=new int[l];
                    for(int j=0;j<l;j++) {
                        ex.experts[j]=value.get(j);
                    }
                }
            }
            PoolofExperts.add(ex);
        }
        return PoolofExperts;
    }

    public Integer getRandomList(List<Integer> list) {

        int index = ThreadLocalRandom.current().nextInt(list.size());

        return list.get(index);

    }



}
