package main;

import data.Domain;
import data.GroundRule;
import data.Rule;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static main.Main.cleanedFileURL;
import static main.Main.setLineID;

/**
 * Created by gcc on 17-7-25.
 */
public class Test {

    public static ArrayList<String> loadFileNoID(String url) {//去除TupleID
        FileReader reader;
        ArrayList<String> list = new ArrayList<>();
        try {
            reader = new FileReader(url);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            br.readLine();//去除header
            while ((line = br.readLine()) != null && line.length() != 0) {
                line = line.substring(line.indexOf(",") + 1);
                list.add(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static HashMap<String, GroundRule> calTupleNum(String mlnURL, String dataURL) {//计算每条规则对应的tuple数量
        ArrayList<String> list = Rule.readFileNoHeader(dataURL);
        HashMap<String, GroundRule> map = new HashMap<>();
        try {
            FileReader reader;
            reader = new FileReader(mlnURL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;

            while ((line = br.readLine()) != null && line.length() != 0) {
                int index = line.indexOf(",");
                String prob = line.substring(0, index);
                String rawMLN = line.substring(index + 1);
                String mln = rawMLN.replaceAll("\"", "");
//                            .replaceAll(".*\\(","")
//                            .replaceAll("\\)","");
                String[] values = mln.split(" v ");
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].replaceAll(" ", "")
                            .replaceAll(".*\\(", "")
                            .replaceAll("\\)", "");
                }
                Arrays.sort(values);//排序 方便比较
                int num = 0;
                for (int i = 0; i < list.size(); i++) {
                    String[] tuple = list.get(i).replaceAll(" ", "").split(",");
                    Arrays.sort(tuple);//排序 方便比较

                    int count = 0;
                    int key = 0;
                    for (int k = 0; k < values.length; k++) {//检验ifContains
                        while (key < tuple.length) {
                            if (values[k].equals(tuple[key])) {
                                count++;
                                key++;
                                break;
                            } else {
                                key++;
                            }
                        }
                    }
                    boolean flag = false;
                    if (count == values.length)
                        flag = true;
                    if (flag) {
                        num++;
                    }
                }
                map.put(rawMLN, new GroundRule(prob, num));//储存规则与它对应的Tuple的数量
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void normalizationMLN(ArrayList<String> newMLNs, ArrayList<String> dataURLs, String writeURL) {
        ArrayList<HashMap<String, GroundRule>> mapList = new ArrayList<>();
        for (int i = 0; i < dataURLs.size(); i++) {
            String newMLN = newMLNs.get(i);
            String dataURL = dataURLs.get(i);
            HashMap<String, GroundRule> map = calTupleNum(newMLN, dataURL);
            mapList.add(map);
        }
        HashMap<String, Double> avgMAP = new HashMap<>(mapList.get(0).size());
        for (int k = 0; k < mapList.size(); k++) {
            HashMap<String, GroundRule> map = mapList.get(k);

            Iterator<Map.Entry<String, GroundRule>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, GroundRule> entry = iter.next();
                String clause = entry.getKey();
                GroundRule gr = entry.getValue();
                Double prob = Double.parseDouble(gr.weight);
                int num = gr.number;
                if (num == 0) {
                    avgMAP.put(clause, prob);
                } else {
                    int count = num;
                    double avgPROG = prob * num;
                    for (int i = k + 1; i < mapList.size(); i++) {
                        GroundRule gr2 = mapList.get(i).get(clause);

                        if (gr2 != null) {
                            Double prob2 = Double.parseDouble(gr2.weight);
                            int num2 = gr2.number;
                            avgPROG += prob2 * num2;
                            count += num2;
                            mapList.get(i).remove(clause);
                        }
                    }
                    avgPROG = avgPROG / count;
                    avgMAP.put(clause, avgPROG);
                }
            }
        }


        //write updated clauses to file
        try {
            File writefile = new File(writeURL);
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);

            Iterator<Map.Entry<String, Double>> new_iter = avgMAP.entrySet().iterator();
            while (new_iter.hasNext()) {
                Map.Entry<String, Double> entry = new_iter.next();
                String clause = entry.getKey();
                Double prob = entry.getValue();
                DecimalFormat format = new DecimalFormat("#0.0000");
                String content = format.format(prob) + ",\t" + clause;
                bw.write(content);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> read(String URL) {
        ArrayList<String> context = new ArrayList<String>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            String current = "";
            int key = 0; //tuple index
            br.readLine();
            while ((line = br.readLine()) != null && line.length() != 0) {
                context.add(line.replaceAll(" ", ""));
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }

    //根据sample的tupleID,从ground truth File中提取对应的数据，用于evaluate()
    public static ArrayList<String> pickData(HashMap<Integer, String[]> dataSet, String groundURL) {

        ArrayList<String> ground_data = read(groundURL);
        ArrayList<String> sample_ground_data = new ArrayList<>(dataSet.size());
        Iterator<Map.Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, String[]> entry = iter.next();
            int tupleID = entry.getKey();
            sample_ground_data.add(ground_data.get(tupleID - 1));
        }
        return sample_ground_data;
    }

    /**
     * Recall is the ratio of correctly updated attributes to the total number of errors.
     * Precision is the ratio of correctly updated attributes (exact matches) to the total number of updates
     */
    public static void evaluate(ArrayList<String> ground_data, String cleanedURL, String dirtyURL) {
        ArrayList<String> cleaned_data = read(cleanedURL);
        ArrayList<String> dirty_data = read(dirtyURL);
        int correct_update_num = 0;
        int total_error_num = 0;
        int total_update_num = 0;
        int over_correct_num = 0;
        double recall;
        double precision;

        System.err.print("no cleaned Line: \n[");
        for (int i = 0; i < ground_data.size(); i++) {
            String current_ground = ground_data.get(i);
            String current_dirty = dirty_data.get(i);
            String current_clean = cleaned_data.get(i);
            if (!current_ground.equals(current_dirty)) {
                total_error_num++;
                if (!current_clean.equals(current_ground)) {
                    System.err.print((i + 2) + " ");  //no cleaned tuple:
//                    System.out.println("current_ground = " + current_ground);
//                    System.out.println("current_dirty = " + current_dirty);
//                    System.out.println("current_clean = " + current_clean);

                }
            }
            if (!current_clean.equals(current_dirty)) {
                total_update_num++;
                if (current_clean.equals(current_ground)) {
                    correct_update_num++;
                } else {
                    over_correct_num++;
//                    System.err.println("over correct id = " + (i + 1));
                }
            }
        }
        System.err.println("]");
//        System.err.println("over correct number = " + over_correct_num);
        System.out.println("\ntotal error number = " + total_error_num);
        recall = (double) correct_update_num / total_error_num;
        precision = (double) correct_update_num / total_update_num;
        System.out.println("\nRecall = " + recall);
        System.out.println("\nPrecision = " + precision);
        System.out.println("\nF1 = " + 2 * (precision * recall) / (precision + recall));
    }

    /**
     * Recall is the ratio of correctly updated attributes to the total number of errors.
     * Precision is the ratio of correctly updated attributes (exact matches) to the total number of updates
     */
    public static void evaluate(String groundURL, String cleanedURL, String dirtyURL) {
        ArrayList<String> ground_data = read(groundURL);
        ArrayList<String> cleaned_data = read(cleanedURL);
        ArrayList<String> dirty_data = read(dirtyURL);
        int correct_update_num = 0;
        int total_error_num = 0;
        int total_update_num = 0;
        int over_correct_num = 0;
        double recall;
        double precision;

        for (int i = 0; i < ground_data.size(); i++) {
            String current_ground = ground_data.get(i);
            String current_dirty = dirty_data.get(i);
            String current_clean = cleaned_data.get(i);
            if (!current_ground.equals(current_dirty)) {
                total_error_num++;
                if (!current_clean.equals(current_ground)) {
//                    System.out.println("no cleaned tuple: " + (i + 1));
//                    System.out.println("current_ground = " + current_ground);
//                    System.out.println("current_dirty = " + current_dirty);
//                    System.out.println("current_clean = " + current_clean);

                }
            }
            if (!current_clean.equals(current_dirty)) {
                total_update_num++;
                if (current_clean.equals(current_ground)) {
                    correct_update_num++;
                } else {
                    over_correct_num++;
//                    System.err.println("over correct id = " + (i + 1));
                }
            }
        }
        System.out.println();
//        System.err.println("over correct number = " + over_correct_num);
        recall = (double) correct_update_num / total_error_num;
        precision = (double) correct_update_num / total_update_num;
        System.out.println("\nRecall = " + recall);
        System.out.println("\nPrecision = " + precision);
    }


    /**
     * precision is the ratio of correctly updated attributes
     * (exact matches) to the total number of updates
     */
    public static void calPrecision(String groundURL, String cleanedURL, String dirtyURL) {
        ArrayList<String> ground_file = read(groundURL);
        ArrayList<String> cleaned_file = read(cleanedURL);
        ArrayList<String> dirty_file = read(dirtyURL);
        HashMap<Integer, Integer> markLine1 = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> markLine2 = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> markLine3 = new HashMap<Integer, Integer>();

        for (int i = 0; i < ground_file.size(); i++) {
            String ground_line = ground_file.get(i);
            String dirty_line = dirty_file.get(i);
            if (!ground_line.equals(dirty_line)) {
                markLine1.put(i, 1);     //记录ground-dirty file不匹配的行号，从0开始
            }
        }

        for (int i = 0; i < ground_file.size(); i++) {
            String ground_line = ground_file.get(i);
            String cleaned_line = cleaned_file.get(i);
            if (!ground_line.equals(cleaned_line)) {
                markLine2.put(i, 1);     //记录ground-cleaned file不匹配的行号，从0开始
            }
        }

        for (int i = 0; i < dirty_file.size(); i++) {
            String dirty_line = dirty_file.get(i);
            String cleaned_line = cleaned_file.get(i);
            if (!dirty_line.equals(cleaned_line)) {
                markLine3.put(i, 1);     //记录dirty-cleaned file不匹配的行号，从0开始
            }
        }

        ArrayList<Integer> matchLine = new ArrayList<Integer>();
        Iterator iter = markLine1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Integer key = (Integer) entry.getKey();

            if (null != markLine2.get(key)) {
                matchLine.add(key);
            }
        }
        int correct_num = markLine1.size() - matchLine.size();
        double rate = correct_num / (double) markLine3.size();
        System.out.println("Precision = " + rate);
    }

    public static void main(String[] args) throws Exception {

        int partitionNum = Integer.parseInt(args[3]);
        ArrayList<String> rules = new ArrayList<>();
        ArrayList<HashMap<Integer, String[]>> dataSetList = new ArrayList<>();
        FileReader reader;
        try {
            //Read first-order-logic rules from file
            reader = new FileReader("/home/gcc/experiment/dataSet/" + args[0] + "/rules-first-order.txt");
            BufferedReader br = new BufferedReader(reader);

            String line = null;
            while ((line = br.readLine()) != null && line.length() != 0) {
                rules.add(line);
            }
            br.close();
            /*Rule.partitionMLN("/home/gcc/experiment/dataSet/" + args[0] + "/" + args[2],
                    "/home/gcc/experiment/dataSet/" + args[0] + "/" + args[1],
                    rules, partitionNum, args[0]);*/
            System.out.println("Begin Partition MLNs into '" + partitionNum + "' parts.");
            Rule.partitionMLN("/home/gcc/experiment/dataSet/" + args[0] + "/" + args[1], rules, partitionNum, args[0]);

            ArrayList<String> newMLNs = new ArrayList<>();
            ArrayList<String> dataURLs = new ArrayList<>();

            //训练阶段
            double startTime = System.currentTimeMillis();    //获取开始时间
            for (int i = 0; i < partitionNum; i++) {
                System.out.println("************ PARTITION" + i + " ************");
                String dataWriteFile = "/home/gcc/experiment/dataSet/" + args[0] + "/data-new" + i + ".txt";
                String rulesWriteFile = "/home/gcc/experiment/dataSet/" + args[0] + "/rules-new" + i + ".txt";
                String outFile = "/home/gcc/experiment/dataSet/" + args[0] + "/out-" + i + ".txt";
                String mlnArgs[] = {args[0],dataWriteFile, rulesWriteFile, outFile};
//                Main.learnwt(mlnArgs); //参数训练，最后生成[n=partitionNum]个out.txt文件
                newMLNs.add(outFile);
                dataURLs.add(dataWriteFile);
            }

            normalizationMLN(newMLNs, dataURLs, "/home/gcc/experiment/dataSet/" + args[0] + "/out.txt");
            //清洗阶段
            String mlnArgs[] = {args[0], args[2]};
            HashMap<Integer, String[]> dataSet = Main.main(mlnArgs);
            dataSetList.add(dataSet);
//            for(int i = 0; i < partitionNum; i++) {
//                String rulesWriteFile = "/home/gcc/experiment/dataSet/HAI/rules-new"+i+".txt";
//                String dataWriteFile = "/hom e/gcc/experiment/dataSet/HAI/data-new"+i+".txt";
//                String mlnArgs[] = {dataWriteFile, rulesWriteFile, args[2]};
//                HashMap<Integer,String[]> dataSet = Main.main(mlnArgs);
//                dataSetList.add(dataSet);
//            }

//            for(int i=0;i<dataSetList.size();i++){
//                HashMap<Integer,String[]> dataSet = dataSetList.get(i);
//                newDataset.putAll(dataSet);
//            }
//            Collections.sort(new ArrayList<>(newDataset.entrySet()), new Comparator<Map.Entry<Integer, String[]>>() {
//                @Override
//                public int compare(Map.Entry<Integer, String[]> o1, Map.Entry<Integer, String[]> o2) {
//                    return o1.getKey().compareTo(o2.getKey());
//                }
//            });
            Main.writeToFile(cleanedFileURL, dataSet, Domain.header);
            System.out.println("cleanedDataSet.txt stored in=" + cleanedFileURL);

            double endTime = System.currentTimeMillis();    //获取结束时间
            double totalTime = (endTime - startTime) / 1000;
            DecimalFormat df = new DecimalFormat("#.00");
            System.out.println("Total Time: " + df.format(totalTime) + "s");
//            setLineID("/home/gcc/experiment/dataSet/" + args[0] + "/" + "ground_truth.csv", "/home/gcc/experiment/dataSet/" + args[0] + "/" + "HAI-hasID.csv");
            ArrayList<String> ground_data = pickData(dataSet, "/home/gcc/experiment/dataSet/" + args[0] + "/" + "ground_truth-hasID.csv");
            ground_data.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int index1 = o1.indexOf(",");
                    int index2 = o2.indexOf(",");
                    int id1 = Integer.parseInt(o1.substring(0, index1));
                    int id2 = Integer.parseInt(o2.substring(0, index2));
                    if (id1 > id2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            Main.writeToFile(Domain.header, ground_data, "/home/gcc/experiment/dataSet/" + args[0] + "/" + "ground_sampleData.csv");

            evaluate(ground_data, cleanedFileURL, "/home/gcc/experiment/dataSet/" + args[0] + "/" + args[2]);
            /*evaluate("/home/gcc/experiment/dataSet/" + args[0] + "/" + args[2], cleanedFileURL,
                     "/home/gcc/experiment/dataSet/" + args[0] + "/" + args[2]);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Main.updateprogMLN("/home/gcc/experiment/dataSet/HAI/out.txt" , "/home/gcc/experiment/dataSet/HAI/HAI-1q-test.txt");
    }
}
