package main;

import data.Rule;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by gcc on 17-9-27.
 */
public class AddErrorToData {
    public String sourceDataFile = "/home/gcc/experiment/dataSet/synthetic-car/ground_truth-1q.txt";
    public String errorRate = "10";//for example： 10, 30, 50
    public String errorDataFile = "/home/gcc/experiment/dataSet/synthetic-car/fulldb-1q-"+errorRate+"%-error.txt";

    public void addErrorRate(String sourceDataFile, String errorDataFile){
        FileReader reader;
        try {
            reader = new FileReader("/home/gcc/experiment/dataSet/HAI/rules-test.txt");

            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while((line = br.readLine()) != null && line.length()!=0) {

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
