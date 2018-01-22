package util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by gcc on 17-12-31.
 */
public class DataAnalysis {
    public static String baseURL = "/home/gcc/experiment/dataSet/";
    public static String dirtyFile_hasID = baseURL + "RDBSCleaner_cleaned.txt";
    public static String cleanFile_hasID = baseURL + "HAI/rawData/ground_sampleData.csv";
    public static String outFile = baseURL + "HAI/rawData/WhereIsDirtyData.txt";

    public static void changeValue(String inFile, String outFile) {
//        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String[]> data = new ArrayList<>();
        //read and sub data
        try {
            FileReader file = new FileReader(inFile);
            BufferedReader br = new BufferedReader(file);
            String str;
            String[] headerName = br.readLine().split(",");
            data.add(headerName);
            while ((str = br.readLine()) != null && str.length() != 0) {
                String[] splitStr = str.split(",");
                if (splitStr[4].equals(splitStr[7])) {
                    System.out.println("ID = " + splitStr[0]);
                    splitStr[7] += " County";
//                    ids.add(splitStr[0]);
                }
                data.add(splitStr);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write to file
        try {
            FileWriter fw = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String[] t : data) {
                String line = "";
                for (int i = 0; i < t.length; i++) {
                    String tmp = t[i];
                    line += tmp;
                    if (i != t.length - 1) {
                        line += ",";
                    }
                }
                bw.write(line + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void subDataSet(String inFile, String outFile, int[] ignorIDs) {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        String[] headerName = {};
        ArrayList<String> newHeaderName = new ArrayList<>();

        //read and sub data
        try {
            FileReader file = new FileReader(inFile);
            BufferedReader br = new BufferedReader(file);
            String str;
            headerName = br.readLine().split(",");

            for (int i = 0; i < headerName.length; i++) {
                newHeaderName.add(headerName[i]);
            }
            //remove useless Attribute Name
            for (int i = 0; i < ignorIDs.length; i++) {
                newHeaderName.remove(headerName[ignorIDs[i]]);
            }

            while ((str = br.readLine()) != null && str.length() != 0) {
                String[] splitStr = str.split(",");
                ArrayList<String> tuple = new ArrayList<>();
                for (int i = 0; i < splitStr.length; i++) {
                    tuple.add(splitStr[i]);
                }
                //remove useless Attribute Value
                for (int i = 0; i < ignorIDs.length; i++) {
                    tuple.remove(tuple.get(ignorIDs[i]));
                }
                list.add(tuple);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write to file
        try {
            FileWriter fw = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(fw);
            String ss = "";
            for (int i = 0; i < newHeaderName.size(); i++) {
                if (i != newHeaderName.size() - 1) {
                    ss += newHeaderName.get(i) + ",";
                } else
                    ss += newHeaderName.get(i);
            }
            bw.write(ss);
            bw.newLine();
            for (int i = 0; i < list.size(); i++) {
                String ll = "";
                for (int j = 0; j < list.get(i).size(); j++) {
                    if (j != list.get(i).size() - 1) {
                        ll += list.get(i).get(j) + ",";
                    } else
                        ll += list.get(i).get(j);
                }
                bw.write(ll);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void analyWhereIsError() {
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
                bw.write("ID = " + tupleID + "; AttrIndex = " + (AttrIndex - 1) + "; clean_value = " + clean_value + "; dirty_value = " + dirty_value);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String baseURL = "/home/gcc/experiment/dataSet/HAI/rawData/";
        String inFile = baseURL + "HAI-3q-10%error.csv";
        String outFile = baseURL + "HAI-3q-10%error(1).csv";
        changeValue(inFile, outFile);
//        analyWhereIsError();
        /*
        int[] ignoredIDs = {10,12};
        subDataSet(inFile, outFile, ignoredIDs);*/
    }
}
