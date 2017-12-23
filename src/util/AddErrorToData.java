package util;


import java.io.*;
import java.util.*;

/**
 * Created by gcc on 17-9-27.
 */
public class AddErrorToData {
    public static HashMap<Integer,String[]> dataSet = new HashMap<>();
    public static ArrayList<HashMap<String,Integer>> groupByValue = new ArrayList<>();
    public static float errorRate = 0.30f;
    static String[] header = null;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String fileURL = "/home/gcc/experiment/dataSet/HAI/HAI-1q.csv";
        String outURL = "/home/gcc/experiment/dataSet/HAI/HAI-1q-30%error.csv";
        FileReader reader;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            String current = "";
            int key = 0; //tuple index
            header = br.readLine().split(",");
//	        groupByValue = new ArrayList<HashMap<String,Integer>>(header.length);

            for(int i=0;i<header.length;i++){
                HashMap<String,Integer> map = new HashMap<>();
                groupByValue.add(map);
            }

            while((line = br.readLine()) != null && line.length()!=0){  //The data has header
                String[] tuple = line.split(",");
                for(int i=0;i<header.length;i++){
                    groupByValue.get(i).put(tuple[i], key);
                }
                dataSet.put(key, tuple);
                key++;
            }
            int totalSIZE = dataSet.size();
            int errorSIZE = Math.round(totalSIZE * errorRate);
            Random random = new Random();
            ArrayList<Integer> errorKeyList = new ArrayList<Integer>();
            System.out.println("ERROR SIZE = "+errorSIZE);
            while(errorKeyList.size()<errorSIZE){
                for(int i=0;i<errorSIZE;i++){
                    int curr_key = random.nextInt(totalSIZE);
                    if(!errorKeyList.contains(curr_key)){
                        errorKeyList.add(curr_key);
                        if(errorKeyList.size()==errorSIZE)break;
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
            for(int i: errorKeyList){
                System.out.print(i+" ");
            }
            System.out.println("\nCHECK SIZE = "+errorKeyList.size());

            for(int i=0;i<errorKeyList.size();i++){
                String[] currTuple = dataSet.get(errorKeyList.get(i));
                String value = currTuple[0];
                System.out.print("change value : "+value);

                while(true){
                    String[] keys = groupByValue.get(0).keySet().toArray(new String[0]);
                    Random rand = new Random();
                    String randKey = keys[random.nextInt(keys.length)];
                    if(!randKey.equals(value)){
                        currTuple[0] = randKey;
                        System.out.print(" -> "+randKey+"\n");
                        break;
                    }
                }
            }

            for(int i=0;i<errorKeyList.size();i++){
                String[] currTuple = dataSet.get(errorKeyList.get(i));
                String value = currTuple[1];
                System.out.print("change value : "+value);

                while(true){
                    String[] keys = groupByValue.get(1).keySet().toArray(new String[0]);
                    Random rand = new Random();
                    String randKey = keys[random.nextInt(keys.length)];
                    if(!randKey.equals(value)){
                        currTuple[1] = randKey;
                        System.out.print(" -> "+randKey+"\n");
                        break;
                    }
                }
            }

            writeToFile(outURL,dataSet,header);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //写文件
    public static void writeToFile(String cleanedFileURL, HashMap<Integer, String[]> dataSet, String[] header){
        File file = new File(cleanedFileURL);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("文件已存在: " + cleanedFileURL);
            }
            else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("目标文件所在目录不存在，准备创建它！");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("创建目标文件所在的目录失败！");
                }
            }
            else{
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            Iterator<Map.Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
            writer.write(Arrays.toString(header).replaceAll("[\\[\\]]","").replaceAll(" ",""));
            writer.newLine();//换行
            while(iter.hasNext()){
                Map.Entry<Integer, String[]> entry = iter.next();
                String line = Arrays.toString(entry.getValue()).replaceAll("[\\[\\]]","").replaceAll(" ","");
                writer.write(line);
                writer.newLine();//换行
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally{

        }
    }
}
