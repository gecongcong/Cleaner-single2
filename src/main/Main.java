package main;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import com.sun.org.apache.xpath.internal.operations.Bool;
import data.Domain;
import data.GroundRule;
import data.Rule;
import data.Tuple;
import tuffy.main.MLNmain;

import javax.print.attribute.HashAttributeSet;


public class Main {
    static String[] header = null;
    static String baseURL = "/home/gcc/experiment";    // experiment baseURL
    //static String rootURL = System.getProperty("user.dir"); //Project BaseURL
    static String cleanedFileURL = null;
    static ArrayList<Integer> ignoredIDs = null;
    public static String rulesURL = baseURL + "/dataSet/synthetic-car/rules.txt";
    public static String dataURL = baseURL + "/dataSet/synthetic-car/fulldb-1q.txt";
    //public static String dataURL = "/home/gcc/experiment/RDBSCleaner_cleaned.txt";
    //public static String groundURL = baseURL + "/dataSet/synthetic-car/synthetic-car-1q.csv";


    public static void updateprogMLN(String oldMLNfile, String dataFile){

        ArrayList<String> rules = new ArrayList<String>();
        ArrayList<String> newerRules = new ArrayList<String>();
        try {
            FileReader reader = new FileReader("/home/gcc/experiment/dataSet/synthetic-car/rules.txt");
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while((line = br.readLine()) != null && line.length()!=0) {
                rules.add(line);
            }
            br.close();
            HashMap<String, GroundRule> new_results = Rule.createMLN(dataFile, rules);//读取rules模板，创建ground rules
            HashMap<String,String> old_results = readMLNFile(oldMLNfile);

            Iterator<Map.Entry<String,GroundRule>> iter = new_results.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,GroundRule> entry = (Map.Entry<String,GroundRule>) iter.next();
                String new_tuple = entry.getKey();

                if(null == old_results.get(new_tuple)){
                    //old_results.put(new_tuple, entry.getValue().weight);
                    newerRules.add(entry.getValue().weight+",\t"+new_tuple);
                }
            }
            File writefile = new File(oldMLNfile);
            FileWriter fw = new FileWriter(writefile, true);
            BufferedWriter bw = new BufferedWriter(fw);

            for(int i=0;i<newerRules.size();i++){
                bw.write(newerRules.get(i));
                bw.newLine();
            }
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static HashMap<String,String> readMLNFile(String mlnFile){
        HashMap<String,String> result = new HashMap<String,String>();
        try {
            FileReader reader = new FileReader(mlnFile);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            //escape predicate
//            while((line = br.readLine()) != null) {
//                if(line.length()==0)break;
//            }
            while((line = br.readLine()) != null && line.length()!=0) {
                String rule_noWeight = line.substring(line.indexOf(",")+1).trim();
                String weight = line.substring(0,line.indexOf(","));
                result.put(rule_noWeight,weight);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws SQLException, IOException {

        double startTime = System.currentTimeMillis();    //获取开始时间
        Rule rule = new Rule();
        Domain domain = new Domain();
        String evidence_outFile = baseURL + "/dataSet/synthetic-car/evidence.db";


        //System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";//存放清洗后的数据集
        System.out.println("dataURL = " + dataURL);
        String splitString = ",";
        boolean ifHeader = true;
        List<Tuple> rules = rule.loadRules(dataURL, rulesURL, splitString);
        rule.initData(dataURL, splitString, ifHeader);
        ArrayList<Tuple> newTupleList = rule.tupleList;
        //dataSet是所有数据的集合，我要从里面拿出
        ignoredIDs = rule.findIgnoredTuples(rules);
        domain.header = rule.header;
        header = rule.header;

        //domain.createMLN(rule.header, rulesURL);
        //调用MLN相关的命令参数
        ArrayList<String> list = new ArrayList<String>();
        String marginal_args = "-marginal";
        //list.add(marginal_args);
        String learnwt_args = "-learnwt";
        list.add(learnwt_args);
        String nopart_args = "-nopart";
        //list.add(nopart_args);
        String mln_args = "-i";
        list.add(mln_args);
        String mlnFileURL = baseURL + "/dataSet/synthetic-car/prog-new.mln";//prog.mln
        list.add(mlnFileURL);
        String evidence_args = "-e";
        list.add(evidence_args);
        String evidenceFileURL = baseURL + "/dataSet/synthetic-car/evidence.db"; //samples/smoke/
        list.add(evidenceFileURL);
        String queryFile_args = "-queryFile";
        list.add(queryFile_args);
        String queryFileURL = baseURL + "/dataSet/synthetic-car/query.db";
        list.add(queryFileURL);
        String outFile_args = "-r";
        list.add(outFile_args);
        String weightFileURL = baseURL + "/dataSet/synthetic-car/out.txt";
        list.add(weightFileURL);
        String noDropDB = "-keepData";
        //list.add(noDropDB);
        String maxIter_args = "-dMaxIter";
        list.add(maxIter_args);
        String maxIter = "400";
        list.add(maxIter);
        String mcsatSamples_args = "-mcsatSamples";
        //list.add(mcsatSamples_args);
        String mcsatSamples = "100";
        //list.add(mcsatSamples);
        String[] learnwt = list.toArray(new String[list.size()]);
        List<HashMap<String,Double>> attributesPROBList =new ArrayList<HashMap<String, Double>>();


        int batch = 1; // 可调节
        int sampleSize = 1000; //课调节

        for (int i=0;i<batch;i++) {
            //rule.resample(newTupleList,sampleSize);
            //rule.formatEvidence(evidence_outFile);
            //MLNmain.main(learnwt);    //入口：参数学习 weight learning——using 'Diagonal Newton discriminative learning'
            //updateprogMLN("/home/gcc/experiment/dataSet/synthetic-car/out.txt" , dataURL);
            //读取参数学习得到的团权重，存入HashMap
            HashMap<String, Double> attributesPROB = Rule.loadRulesFromFile("/home/gcc/experiment/dataSet/synthetic-car/out.txt");
            attributesPROBList.add(attributesPROB);
        }


        //区域划分 形成Domains
        domain.init(dataURL, splitString, ifHeader, rules);
        // domain.printDataSet(dataSet);
        //对每个Domain执行group by key操作
        domain.groupByKey(domain.domains, rules);
        //根据MLN的概率修正错误数据
        domain.correctByMLN(domain.Domain_to_Groups, attributesPROBList, domain.header, domain.domains);
        //打印修正后的Domain
        //domain.printDomainContent(domain.domains);

        System.out.println(">>> Find Duplicate Values...");
        List<List<Integer>> keysList = domain.combineDomain(domain.Domain_to_Groups);    //返回所有重复数组的tupleID,并记录重复元组
        //打印重复数据的Tuple ID
        if (null == keysList || keysList.isEmpty()) System.out.println("\tNo duplicate exists.");
        else {
            System.out.println("\n>>> Delete duplicate tuples");
            // 根据keysList 保存的对于domain1 中每个group 保存的key值（这些是有可能会重复的）来去重
            // domain.printDataSet(domain.dataSet);
            // domain.deleteDuplicate(keysList, domain.dataSet);	//执行去重操作
            // domain.printDataSet(domain.dataSet);
            System.out.println(">>> completed!");
        }

        domain.printConflicts(domain.conflicts);
        domain.findCandidate(domain.conflicts, domain.Domain_to_Groups, domain.domains, attributesPROBList, ignoredIDs);


        //print dataset after cleaning
        //  domain.printDataSet(domain.dataSet);

        writeToFile(cleanedFileURL, domain.dataSet, domain.header);
//      cleanedFileURL = httpSession.getServletContext().getContextPath()+ "/out/cleanedDataSet.data";//修改为相对路径;
        System.out.println("cleanedDataSet.txt stored in=" + cleanedFileURL);
        double endTime = System.currentTimeMillis();    //获取结束时间
        double totalTime = (endTime - startTime) / 1000;
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("程序运行时间： " + df.format(totalTime) + "s");
        //     String cleanFileURL ="C:\\Users\\zmx\\Desktop\\Cleaner-single\\synthetic-car-1q.csv";

    }






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
            Iterator<Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
            writer.write(Arrays.toString(header).replaceAll("[\\[\\]]", ""));
            writer.newLine();//换行
            while (iter.hasNext()) {
                Entry<Integer, String[]> entry = iter.next();
                String line = Arrays.toString(entry.getValue()).replaceAll("[\\[\\]]", "");
                writer.write(line);
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

