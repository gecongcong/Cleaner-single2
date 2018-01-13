package util;

import main.Main;

import java.io.*;
import java.util.*;

public class Sampling {
    public static String baseURL = "/home/gcc/experiment/dataSet/";    // source file baseURL
    public static String outFile = baseURL + "customer/join-total-hasID.csv";
    public static String sourceFile_hasID = baseURL + "customer/join-1q-10%error.csv";//customer.tbl
    public static String sampleFile = baseURL + "customer/join-1q.csv";
    public static String trainFile = baseURL + "customer/trainData.csv";
    public static String testFile = baseURL + "customer/testData.csv";
    public static HashMap<String, ArrayList<Integer>> dataMap = new HashMap<>();//存放对应这条String的所有元组ID

    class TmpTuple {
        int tupleID;
        String content;


        TmpTuple(int tupleID, String content) {
            this.tupleID = tupleID;
            this.content = content;
        }
    }

    //求两个数组的差集
    public static int[] minus(int[] arr1, int[] arr2) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        LinkedList<Integer> history = new LinkedList<Integer>();
        int[] longerArr = arr1;
        int[] shorterArr = arr2;
        //找出较长的数组来减较短的数组
        for (int str : longerArr) {
            if (!list.contains(str)) {
                list.add(str);
            }
        }
        for (int str : shorterArr) {
            if (list.contains(str)) {
                history.add(str);
                list.remove(list.indexOf(str));
            } else {
                if (!history.contains(str)) {
                    list.add(str);
                }
            }
        }
        int[] result = new int[arr1.length - arr2.length];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static void convert(String inFile, String outFile) {

        //read
        FileReader reader;
        ArrayList<String> list = new ArrayList<>();
        try {
            reader = new FileReader(inFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                line = line.replaceAll("\t", "|");
                list.add(line);
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write
        File file = new File(outFile);
        FileWriter fw;
        BufferedWriter bw;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("File url: " + outFile);
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
            int i = 0;
            for (String str : list) {
                bw.write(str);
                if (i != list.size() - 1)
                    bw.newLine();
                i++;
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(String sourceFile, String trainFile, String testFile, int sampleNum, int[] ignoredIDs) {

        //read data set from sourceFile
        FileReader reader;
        HashMap<Integer, String> dataSet = new HashMap<>();  //存放原始数据
        ArrayList<TmpTuple> trainData = new ArrayList<>();  //存放采样后的训练数据
        ArrayList<TmpTuple> testData = new ArrayList<>();  //存放采样后的测试数据
        String header = "";
        try {
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
//            int lineID = 0;
            int lineID;
            header = br.readLine();   //ignore header line
            while ((line = br.readLine()) != null && line.length() != 0) {
                lineID = Integer.parseInt(line.substring(0, line.indexOf("|")));
                String content = line.substring(line.indexOf("|") + 1);
                String[] tuple = content.split("|");
//                String content = Arrays.toString(tuple).replaceAll("[\\[\\]]", "").replaceAll(" ", "");
                dataSet.put(lineID, content);

                String[] newTuple = new String[tuple.length - ignoredIDs.length];

                int[] ids = new int[tuple.length];
                for (int i = 0; i < tuple.length; i++) {
                    ids[i] = i;
                }
                int[] new_ids = minus(ids, ignoredIDs);
                for (int i = 0; i < new_ids.length; i++) {
                    newTuple[i] = tuple[new_ids[i]];
                }

                String newLine = "";
                for (String t : newTuple) {
                    newLine += t + "|";
                }
                if (!dataMap.containsKey(newLine)) {
                    ArrayList<Integer> linkIDs = new ArrayList<>();
                    linkIDs.add(lineID);
                    dataMap.put(newLine, linkIDs);
                } else {
                    ArrayList<Integer> linkIDs = dataMap.get(newLine);
                    linkIDs.add(lineID);
                    dataMap.put(newLine, linkIDs);
                }
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //do sampling
        double ratio = (double) sampleNum / dataSet.size();
        ArrayList<Integer> trainIDs = new ArrayList<>();    //训练集tupleIDs
        int minID;
        int maxID;
        Iterator<Map.Entry<String, ArrayList<Integer>>> iter = dataMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = iter.next();
            ArrayList<Integer> linkedIDs = entry.getValue();

            Collections.sort(linkedIDs, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    if (o1 > o2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

            int size = linkedIDs.size();
            minID = linkedIDs.get(0);
            maxID = minID + size - 1;
            int sample_size = (int) Math.round(Math.floor(size * ratio));

            int i = 0;
            int random_result;

            HashMap<Integer, Boolean> bool = new HashMap(sample_size);
            while (i < sample_size) {
                do {

                    random_result = linkedIDs.get(getRandomIndex(sample_size));//采样得到random_result这个tupleID
                    trainIDs.add(random_result);
                } while (bool.containsKey(random_result));
                bool.put(random_result, true);
                //寻找random result对应的tuple,并存入sampleData中
                trainData.add(new TmpTuple(random_result, dataSet.get(random_result)));
                i++;
            }
        }
        //得到与训练集互斥的测试集
        Iterator<Map.Entry<Integer, String>> iter2 = dataSet.entrySet().iterator();
        while (iter2.hasNext()) {
            boolean flag = false;
            Map.Entry<Integer, String> entry = iter2.next();
            int tupleID = entry.getKey();
            for (int trainID : trainIDs) {
                if (tupleID == trainID) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                testData.add(new TmpTuple(tupleID, dataSet.get(tupleID)));
            }
        }

        Collections.sort(testData, new Comparator<TmpTuple>() {
            @Override
            public int compare(TmpTuple o1, TmpTuple o2) {
                if (o1.tupleID > o2.tupleID) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        trainData.sort(new Comparator<TmpTuple>() {
            @Override
            public int compare(TmpTuple o1, TmpTuple o2) {
                if (o1.tupleID > o2.tupleID) {
                    return 1;
                } else return -1;
            }
        });
        writeToFile(trainData, trainFile, header);//采样得到训练集

        writeToFile(testData, testFile, header);//采样得到测试集
    }

    public static int getRandomIndex(int size) {
        Random random = new Random();
        int result = random.nextInt(size);
//        int result = random.nextInt(size);
        System.out.println("random int = " + result);
        return result;
    }

    public static int getRandomIndex(int minID, int maxID) {
        Random random = new Random();
        int result = random.nextInt(maxID) % (maxID - minID + 1) + minID;
//        int result = random.nextInt(size);
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

    public static void writeToFile(ArrayList<TmpTuple> list, String url, String header) {
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
            bw.write(header);
            bw.newLine();
            for (TmpTuple tuple : list) {
                bw.write(tuple.tupleID + "|" + tuple.content);
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tailSample(int lineNUM, String sourceFile, String outFile) {
        FileReader reader;
        ArrayList<String> outData = new ArrayList<>();  //存放采样后的数据
        try {
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            int i = 0;
            line = br.readLine();
            outData.add(line);
            while ((line = br.readLine()) != null && line.length() != 0) {
                if (i > lineNUM) {
                    outData.add(line);
                }
                i++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("File url: " + outFile);
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
            for (String tuple : outData) {
                bw.write(tuple);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void headSample(int lineNUM, String sourceFile, String outFile) {
        FileReader reader;
        ArrayList<String> outData = new ArrayList<>();  //存放采样后的数据
        try {
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            int i = 0;
            while (i <= lineNUM && (line = br.readLine()) != null && line.length() != 0) {
                outData.add(line);
                i++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("File url: " + outFile);
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
            for (String tuple : outData) {
                bw.write(tuple);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sort(String sourceFile, String outFile) {
        FileReader reader;
        ArrayList<String> outData = new ArrayList<>();
        try {
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                outData.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(outData, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Integer custkey1 = Integer.parseInt(o1.substring(0, o1.indexOf("|")));
                Integer custkey2 = Integer.parseInt(o2.substring(0, o2.indexOf("|")));
                return custkey1.compareTo(custkey2);
            }
        });

        File file = new File(outFile);
        FileWriter fw;
        BufferedWriter bw;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("File url: " + outFile);
            } else if (!file.getParentFile().exists()) {
                System.out.println("create file!");
                if (!file.getParentFile().mkdirs()) {
                    System.out.println("failed to create file");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (String tuple : outData) {
                bw.write(tuple);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeLastSplit(String file, String outFile, String split) {
        ArrayList<String> outData = new ArrayList<>();  //存放采样后的数据
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                int index = line.lastIndexOf(split);
                /*line = line.substring(0, index);
                index = line.lastIndexOf(split);*/
                String newLine = line.substring(0, index);
                outData.add(newLine);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File outfile = new File(outFile);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (outfile.exists()) {// 判断文件是否存在
                System.out.println("File url: " + outFile);
            } else if (!outfile.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("create file!");
                if (!outfile.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("failed to create file");
                }
            } else {
                outfile.createNewFile();
            }
            fw = new FileWriter(outFile);
            bw = new BufferedWriter(fw);
            int i = 0;
            for (String tuple : outData) {
                bw.write(tuple);
                if (i != outData.size())
                    bw.newLine();
                i++;
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        removeLastSplit(sourceFile_hasID,outFile,"|");
//        Main.setLineID(sampleFile,sampleFile.replaceAll("\\.csv","\\-hasID.csv"),"|");
//        Main.setLineID(sourceFile_hasID,outFile,"|");
//        headSample(1000, outFile, sampleFile);
//        convert(sampleFile, outFile);
//        sort(sourceFile_hasID,outFile);
        int sampleNum = 500;
        int[] ignoredIDs = {0, 2, 4, 5, 6, 7};
        new Sampling().run(sourceFile_hasID, trainFile, testFile, sampleNum, ignoredIDs);
    }
}
