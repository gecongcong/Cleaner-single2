package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Sampling {
    public static String baseURL = "/Users/gecongcong/experiment/dataSet";    // source file baseURL
    public static String sourceFile = baseURL + "";
    public static HashMap<String, Integer> dataMap = new HashMap<>();

    class dataLink { //存放对应这条String的所有元组ID
        String keyword;
        ArrayList<Integer> IDlist = new ArrayList<>();
    }

    public static void run(String sourceFile, int sampleNum, int[] ignoredIDs) {
        //read data set from sourceFile
        FileReader reader;
        ArrayList<String> dataSet = new ArrayList<>();
        try {
            reader = new FileReader(sourceFile);

            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null && line.length() != 0) {
                dataSet.add(line);
                String[] tuple = line.split(",");
                String[] newTuple = new String[tuple.length - ignoredIDs.length];
                int old_i = 0;
                for (int k = 0; k < ignoredIDs.length; k++) {
                    for (int new_i = 0; new_i < newTuple.length; new_i++) {
                        if(new_i!=ignoredIDs[k]){
                            newTuple[new_i] = tuple[old_i];
                        }else{
                            old_i++;
                        }
                    }
                }

                if (!dataMap.containsKey(line)) {
                    dataMap.put(line, 1);
                } else {
                    dataMap.put(line, dataMap.get(line) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        int sampleNum = 100;
        int[] ignoredIDs = {2, 4};
        run(sourceFile, sampleNum, ignoredIDs);
        return;
    }

}
