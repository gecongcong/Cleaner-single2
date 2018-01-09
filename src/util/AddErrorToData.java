package util;


import main.Main;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by gcc on 17-9-27.
 */
public class AddErrorToData {
    public static String fileURL = "/home/gcc/experiment/dataSet/synthetic-car/ground_truth-hasID.csv";
    public static String outURL = "/home/gcc/experiment/dataSet/synthetic-car/fulldb-hasID-5%error.csv";
    public static HashMap<Integer, String[]> dataSet = new HashMap<>();
    public static ArrayList<HashMap<String, Integer>> groupByValue = new ArrayList<>();
    public static float replaceRate = 0.03f;
    public static float substrRate = 0.02f;
    public static int discardNum = 1;   //丢弃的字符数量
    static String[] header = null;

    class Data {
        int id;
        String content;

        Data(int id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    public void run() { //添加error: 替换值 + 残缺值
        FileReader reader;
        HashMap<String, ArrayList<Integer>> convertDataSet = new HashMap<>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            String current = "";
            int roundTime = 0;
//            int key = 0; //tuple index

            int tupleID;
            int minID = 0;
            header = line.substring(line.indexOf(",") + 1).split(",");

            for (int i = 0; i < header.length; i++) {
                HashMap<String, Integer> map = new HashMap<>();
                groupByValue.add(map);
            }

            while ((line = br.readLine()) != null && line.length() != 0) {  //The data has header
                tupleID = Integer.parseInt(line.substring(0, line.indexOf(",")));
                if (roundTime == 0) {
                    minID = tupleID;
                    roundTime++;
                }
                String str_tuple = line.substring(line.indexOf(",") + 1);
                String[] tuple = str_tuple.split(",");
                for (int i = 0; i < header.length; i++) {
                    groupByValue.get(i).put(tuple[i], tupleID);
                }
                dataSet.put(tupleID, tuple);
                if (convertDataSet.get(str_tuple) == null) {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(tupleID);
                    convertDataSet.put(str_tuple, list);
                } else {
                    ArrayList<Integer> list = convertDataSet.get(str_tuple);
                    list.add(tupleID);
                    convertDataSet.put(str_tuple, list);
                }
            }

            int totalSIZE = dataSet.size();
            int maxID = minID + totalSIZE;

            int errorSIZE = Math.round(totalSIZE * replaceRate);
            Random random = new Random();
            ArrayList<Integer> errorKeyList = new ArrayList<>();
            System.out.println("ERROR SIZE = " + errorSIZE);
            while (errorKeyList.size() < errorSIZE) {
                for (int i = 0; i < errorSIZE; i++) {
                    int curr_key = random.nextInt(maxID) % (maxID - minID + 1) + minID;
                    if (!errorKeyList.contains(curr_key)) {
                        errorKeyList.add(curr_key);
                        if (errorKeyList.size() == errorSIZE) break;
                    }
                }
            }
            //从小到大排序
            Collections.sort(errorKeyList, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return new Double((int) o1).compareTo(new Double((int) o2));
                }
            });

            /*for (int i = 0; i < errorKeyList.size(); i++) {
                int errorKey = errorKeyList.get(i);
                String[] currTuple = dataSet.get(errorKey);
                String str_currTuple = Arrays.toString(currTuple)
                        .replaceAll("\\[","")
                        .replaceAll("]","")
                        .replaceAll(" ","");
                int size = convertDataSet.get(str_currTuple).size();
                if(size < 5 ){//相同数目＜3的不添加error
                    continue;
                }
                String value = currTuple[1];
                Random rand = new Random();
                while (true) {
                    String[] keys = groupByValue.get(1).keySet().toArray(new String[0]);
                    String randKey = keys[rand.nextInt(keys.length)];
                    if (!randKey.equals(value)) {
                        currTuple[1] = randKey;
                        break;
                    }
                }
            }*/

            for (int i = 0; i < errorKeyList.size(); i++) {
                int errorKey = errorKeyList.get(i);
                String[] currTuple = dataSet.get(errorKey);
                String str_currTuple = Arrays.toString(currTuple)
                        .replaceAll("\\[","")
                        .replaceAll("]","")
                        .replaceAll(" ","");
                ArrayList<Integer> list = convertDataSet.get(str_currTuple);
                if(list == null){
                    continue;
                }
                int size = list.size();
                if(size < 5 ){//相同数目＜3的不添加error
                    continue;
                }
                String value = currTuple[6];
                Random rand = new Random();
                while (true) {
                    String[] keys = groupByValue.get(6).keySet().toArray(new String[0]);
                    String randKey = keys[rand.nextInt(keys.length)];
                    if (!randKey.equals(value)) {
                        currTuple[6] = randKey;
                        break;
                    }
                }
            }

            System.out.println("errorKey: ");
            for (int errorKey : errorKeyList) {
                System.out.print(errorKey + " ");
            }
            System.out.println("\n");

            /**
             * 添加残缺值的error
             */
            int substrSIZE = Math.round(totalSIZE * substrRate);
            Random random2 = new Random();
            ArrayList<Integer> substrKeyList = new ArrayList<>();
            System.out.println("SUBSTR SIZE = " + substrSIZE);
            while (substrKeyList.size() < errorSIZE) {
                while (true) {
                    int curr_key = random2.nextInt(maxID) % (maxID - minID + 1) + minID;
                    if(errorKeyList.contains(curr_key))continue;
                    if (!substrKeyList.contains(curr_key)) {
                        substrKeyList.add(curr_key);
                        if (substrKeyList.size() == errorSIZE) break;
                    }
                }
            }

            for (int i = 0; i < substrKeyList.size()/2; i++) {
                String[] currTuple = dataSet.get(substrKeyList.get(i));
                String value = currTuple[2];
                Random rand = new Random();
                int randomNum = rand.nextInt(discardNum);
                String newValue;
                if (value.length() -1 > randomNum) {
                    newValue = value.substring(0, value.length() - randomNum - 1);
                } else {
                    newValue = value.substring(0, randomNum - value.length() +1);
                }
                currTuple[2] = newValue;
            }

            for (int i = substrKeyList.size()/2; i < substrKeyList.size(); i++) {
                String[] currTuple = dataSet.get(substrKeyList.get(i));
                String value = currTuple[1];
                Random rand = new Random();
                int randomNum = rand.nextInt(discardNum);
                String newValue;
                if (value.length() -1 > randomNum) {
                    newValue = value.substring(0, value.length() - randomNum - 1);
                } else {
                    newValue = value.substring(0, randomNum - value.length() +1);
                }
                currTuple[1] = newValue;
            }


            System.out.println("substrKey: ");
            for(int substrKey:substrKeyList){
                System.out.print(substrKey+" ");
            }
            System.out.println("\n");

            //从小到大排序
            Collections.sort(substrKeyList, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return new Double((int) o1).compareTo(new Double((int) o2));
                }
            });

            //sort dataSet by TupleID
            Iterator<Map.Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
            ArrayList<Data> dataList = new ArrayList<>(dataSet.size());
            while (iter.hasNext()) {
                Map.Entry<Integer, String[]> entry = iter.next();
                int id = entry.getKey();
                String[] value = entry.getValue();
                String content = Arrays.toString(value).replaceAll("[\\[\\]]", "").replaceAll(" ", "");
                dataList.add(new Data(id, content));
            }

            Collections.sort(dataList, new Comparator<Data>() {
                @Override
                public int compare(Data o1, Data o2) {
                    if (o1.id > o2.id) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

            writeToFile(outURL, dataList, header);

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        Main.setLineID(fileURL,outURL);
        new AddErrorToData().run();
    }

    //写文件
    public static void writeToFile(String cleanedFileURL, ArrayList<Data> dataList, String[] header) {
        File file = new File(cleanedFileURL);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("文件已存在: " + cleanedFileURL);
            } else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("目标文件所在目录不存在，准备创建它！");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("创建目标文件所在的目录失败！");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            writer.write("ID," + Arrays.toString(header).replaceAll("[\\[\\]]", "").replaceAll(" ", ""));
            writer.newLine();//换行
            for (int i = 0; i < dataList.size(); i++) {
                Data data = dataList.get(i);
                String line = data.id + "," + data.content;
                writer.write(line);
                writer.newLine();//换行
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    //写文件
    public static void writeToFile(String cleanedFileURL, HashMap<Integer, String[]> dataSet, String[] header) {
        File file = new File(cleanedFileURL);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("文件已存在: " + cleanedFileURL);
            } else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("目标文件所在目录不存在，准备创建它！");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("创建目标文件所在的目录失败！");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            Iterator<Map.Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
            writer.write("ID," + Arrays.toString(header).replaceAll("[\\[\\]]", "").replaceAll(" ", ""));
            writer.newLine();//换行
            while (iter.hasNext()) {
                Map.Entry<Integer, String[]> entry = iter.next();
                String line = entry.getKey() + "," + Arrays.toString(entry.getValue()).replaceAll("[\\[\\]]", "").replaceAll(" ", "");
                writer.write(line);
                writer.newLine();//换行
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
