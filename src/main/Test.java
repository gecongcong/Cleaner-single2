package main;

import data.Rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by gcc on 17-7-25.
 */
public class Test {


    public static void main(String[] args) throws Exception{
        ArrayList<String> rules = new ArrayList<String>();

        FileReader reader;
        try {
            reader = new FileReader("/home/gcc/experiment/dataSet/synthetic-car/rules.txt");
            //reader = new FileReader("/home/gcc/experiment/dataSet/synthetic-car/rules.txt");

            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while((line = br.readLine()) != null && line.length()!=0) {
                rules.add(line);
            }
            Rule.createMLN("/home/gcc/experiment/dataSet/synthetic-car/ground_truth-1q.txt",rules);
            //Rule.createMLN("/home/gcc/experiment/dataSet/synthetic-car/fulldb-1q.txt",rules);
        }catch (Exception e){
            e.printStackTrace();
        }
        //Main.updateprogMLN("/home/gcc/experiment/dataSet/synthetic-car/out.txt" , "/home/gcc/experiment/dataSet/HAI/HAI-1q-test.txt");
    }
}
