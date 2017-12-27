package util;


import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by gcc on 17-9-27.
 */
public class AddErrorToData {
    public static HashMap<Integer, String[]> dataSet = new HashMap<>();
    public static ArrayList<HashMap<String, Integer>> groupByValue = new ArrayList<>();
    public static float errorRate = 0.10f;
    static String[] header = null;

    class Data {
        int id;
        String content;

        Data(int id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    public void run() {
        String fileURL = "/home/gcc/experiment/dataSet/HAI/HAI-100q-tail10q.csv";
        String outURL = "/home/gcc/experiment/dataSet/HAI/HAI-100q-tail10q-10%error.csv";
        FileReader reader;
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
                String[] tuple = line.substring(line.indexOf(",") + 1).split(",");
                for (int i = 0; i < header.length; i++) {
                    groupByValue.get(i).put(tuple[i], tupleID);
                }
                dataSet.put(tupleID, tuple);
            }

            int totalSIZE = dataSet.size();


            int maxID = minID + totalSIZE;

            int errorSIZE = Math.round(totalSIZE * errorRate);
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

            //println
            for (int i : errorKeyList) {
                System.out.print(i + " ");
            }
            System.out.println("\nCHECK SIZE = " + errorKeyList.size());

            for (int i = 0; i < errorKeyList.size(); i++) {
                String[] currTuple = dataSet.get(errorKeyList.get(i));
                String value = currTuple[1];
                System.out.print("change value : " + value);

                while (true) {
                    String[] keys = groupByValue.get(1).keySet().toArray(new String[0]);
                    Random rand = new Random();
                    String randKey = keys[random.nextInt(keys.length)];
                    if (!randKey.equals(value)) {
                        currTuple[1] = randKey;
                        System.out.print(" -> " + randKey + "\n");
                        break;
                    }
                }
            }

            for (int i = 0; i < errorKeyList.size(); i++) {
                String[] currTuple = dataSet.get(errorKeyList.get(i));
                String value = currTuple[3];
                System.out.print("change value : " + value);

                while (true) {
                    String[] keys = groupByValue.get(3).keySet().toArray(new String[0]);
                    Random rand = new Random();
                    String randKey = keys[random.nextInt(keys.length)];
                    if (!randKey.equals(value)) {
                        currTuple[3] = randKey;
                        System.out.print(" -> " + randKey + "\n");
                        break;
                    }
                }
            }

            for (int i = 0; i < errorKeyList.size(); i++) {
                String[] currTuple = dataSet.get(errorKeyList.get(i));
                String value = currTuple[4];
                System.out.print("change value : " + value);

                while (true) {
                    String[] keys = groupByValue.get(4).keySet().toArray(new String[0]);
                    Random rand = new Random();
                    String randKey = keys[random.nextInt(keys.length)];
                    if (!randKey.equals(value)) {
                        currTuple[4] = randKey;
                        System.out.print(" -> " + randKey + "\n");
                        break;
                    }
                }
            }


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

    public static void main(String[] args){
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
