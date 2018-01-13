package util;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by gcc on 17-12-31.
 */
public class DataAnalysis {
    public static String baseURL = "/home/gcc/experiment/dataSet/";
    public static String cleanFile_hasID = baseURL + "synthetic-car/ground_sampleData.csv";
    public static String dirtyFile_hasID = baseURL + "RDBSCleaner_cleaned.txt";
    public static String outFile = baseURL + "synthetic-car/WhereIsDirtyData.csv";




    public static void analyseErrorLine(String[] args) {
        ArrayList<String[]> cleanList = new ArrayList<>();
        ArrayList<String[]> dirtyList = new ArrayList<>();
        try {
            FileReader file = new FileReader(cleanFile_hasID);
            BufferedReader br = new BufferedReader(file);
            br.readLine();  //header
            String str;
            while ((str = br.readLine()) != null && str.length() != 0) {
                String[] splitStr = str.split(",");
                cleanList.add(splitStr);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileReader file = new FileReader(dirtyFile_hasID);
            BufferedReader br = new BufferedReader(file);
            br.readLine();  //header
            String str;
            while ((str = br.readLine()) != null && str.length() != 0) {
                String[] splitStr = str.split(",");
                dirtyList.add(splitStr);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        class Tmp {
            String tupleID;
            int AttrIndex;
            String clean_value;
            String dirty_value;
        }
        ArrayList<Tmp> list = new ArrayList<>();
        for (int i = 0; i < cleanList.size(); i++) {
            String[] clean_curr = cleanList.get(i);
            String[] dirty_curr = dirtyList.get(i);
            for (int k = 1; k < clean_curr.length; k++) { //ignore k=0, because it is Tuple ID
                if (!clean_curr[k].equals(dirty_curr[k])) {
                    Tmp tmp = new Tmp();
                    tmp.tupleID = clean_curr[0];
                    tmp.AttrIndex = k;
                    tmp.clean_value = clean_curr[k];
                    tmp.dirty_value = dirty_curr[k];
                    list.add(tmp);
                }
            }
        }

        try {
            FileWriter fw = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < list.size(); i++) {
                Tmp tmp = list.get(i);
                String tupleID = tmp.tupleID;
                int AttrIndex = tmp.AttrIndex;
                String clean_value = tmp.clean_value;
                String dirty_value = tmp.dirty_value;
                bw.write("ID = " + tupleID + "; AttrIndex = " + AttrIndex + "; clean_value = " + clean_value + "; dirty_value = " + dirty_value);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
