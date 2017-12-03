package util;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.util.*;

public class Sampling {
    public static String baseURL = "/home/gcc/experiment/dataSet/";    // source file baseURL
    public static String sourceFile = baseURL + "test/source.txt";
    public static String trainFile = baseURL + "test/trainData.txt";
    public static String testFile = baseURL + "test/testData.txt";
    public static HashMap<String, ArrayList<Integer>> dataMap = new HashMap<>();//存放对应这条String的所有元组ID

    public static void run(String sourceFile, String trainFile, String testFile, int sampleNum, int[] ignoredIDs) {
        //read data set from sourceFile
        FileReader reader;
        ArrayList<String> dataSet = new ArrayList<>();  //存放原始数据
        ArrayList<String> trainData = new ArrayList<>();  //存放采样后的训练数据
        ArrayList<String> testData = new ArrayList<>();  //存放采样后的测试数据
        try {
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            int lineID = 0;
            br.readLine();   //ignore header line
            while ((line = br.readLine()) != null && line.length() != 0) {
                dataSet.add(line);
                String[] tuple = line.split(",");
                String[] newTuple = new String[tuple.length - ignoredIDs.length];
                int old_i = 0;
                for (int k = 0; k < ignoredIDs.length; k++) {
                    for (int new_i = 0; new_i < newTuple.length; new_i++) {
                        if (new_i != ignoredIDs[k]) {
                            newTuple[new_i] = tuple[old_i];
                            old_i++;
                        } else {
                            old_i++;
                        }
                    }
                }
                String newLine = Arrays.toString(newTuple)
                        .replaceAll("\\[", "")
                        .replaceAll("]", "")
                        .replaceAll(" ", "");
                if (!dataMap.containsKey(newLine)) {
                    ArrayList<Integer> linkIDs = new ArrayList<>();
                    linkIDs.add(lineID);
                    dataMap.put(newLine, linkIDs);
                } else {
                    ArrayList<Integer> linkIDs = dataMap.get(newLine);
                    linkIDs.add(lineID);
                    dataMap.put(newLine, linkIDs);
                }
                lineID++;
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //do sampling
        double ratio = (double) sampleNum / dataSet.size();
        ArrayList<Integer> trainIDs = new ArrayList<>();    //训练集tupleIDs

        Iterator<Map.Entry<String, ArrayList<Integer>>> iter = dataMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = iter.next();
            //String keywords = entry.getKey();
            ArrayList<Integer> linkedIDs = entry.getValue();
            int size = linkedIDs.size();
            int sample_size = (int) Math.round(size * ratio);

            int i = 0;
            int random_result;
            boolean[] bool = new boolean[sample_size];
            int num = 0;
            while (i < sample_size) {
                do {
                    random_result = linkedIDs.get(getRandomIndex(size));//采样得到random_result这个tupleID
                    trainIDs.add(random_result);
                } while (bool[num]);
                bool[num] = true;
                num++;
                //寻找random result对应的tuple,并存入sampleData中
                trainData.add(dataSet.get(random_result));
                i++;
            }
        }
        //得到与训练集互斥的测试集
        for (int i = 0; i < dataSet.size(); i++) {
            boolean flag = false;
            for (int trainID : trainIDs) {
                if (i == trainID) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                testData.add(dataSet.get(i));
            }
        }

        writeToFile(trainData, trainFile);//采样得到训练集

        writeToFile(testData, testFile);//采样得到测试集
    }

    public static void main(String[] args) {
        int sampleNum = 2000;
        int[] ignoredIDs = {7};
        run(sourceFile, trainFile, testFile, sampleNum, ignoredIDs);
    }

    public static int getRandomIndex(int size) {
        Random random = new Random();
        int result = random.nextInt(size);
        System.out.println("random int = " + result);
        return result;
    }

    /**
     * 生成n个不同的随机数，且随机数区间为[0,random_size)
     *
     * @param n
     * @return
     */
    public ArrayList getDiffNum(int n, int random_size) {
        // 生成 [0-n) 个不重复的随机数
        // list 用来保存这些随机数
        ArrayList list = new ArrayList();
        Random rand = new Random();
        boolean[] bool = new boolean[n];
        int num = 0;
        for (int i = 0; i < n; i++) {
            do {
                // 如果产生的数相同继续循环
                num = rand.nextInt(random_size);
            } while (bool[num]);
            bool[num] = true;
            list.add(num);
        }
        return list;
    }

    public static void writeToFile(ArrayList<String> list, String url) {
        File file = new File(url);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("File url: " + url);
            } else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("create file!");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("failed to create file");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (String tuple : list) {
                bw.write(tuple);
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
