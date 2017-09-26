package main;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.sun.org.apache.xpath.internal.operations.Bool;
import data.Domain;
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
    public static String rulesURL = baseURL + "/dataSet/HAI/rules.txt";
    public static String dataURL = baseURL + "/dataSet/HAI/HAI-1q-10%-error.txt";
    public static String groundURL = baseURL + "/dataSet/HAI/HAI-1q.csv";


    public static void main(String[] args) throws SQLException, IOException {

        double startTime = System.currentTimeMillis();    //��ȡ��ʼʱ��
        Rule rule = new Rule();
        Domain domain = new Domain();
        String evidence_outFile = baseURL + "/dataSet/HAI/evidence.db";


        //System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";//�����ϴ������ݼ�
        System.out.println("dataURL = " + dataURL);
        String splitString = ",";
        boolean ifHeader = true;
        List<Tuple> rules = rule.loadRules(dataURL, rulesURL, splitString);
        rule.initData(dataURL, splitString, ifHeader);
        ArrayList<Tuple> newTupleList = rule.tupleList;
        //dataSet���������ݵļ��ϣ���Ҫ�������ó�
        ignoredIDs = rule.findIgnoredTuples(rules);
        domain.header = rule.header;
        header = rule.header;

        domain.createMLN(rule.header, rulesURL);
        //����MLN��ص��������
        ArrayList<String> list = new ArrayList<String>();
        String marginal_args = "-marginal";
        //list.add(marginal_args);
        String learnwt_args = "-learnwt";
        list.add(learnwt_args);
        String nopart_args = "-nopart";
        //list.add(nopart_args);
        String mln_args = "-i";
        list.add(mln_args);
        String mlnFileURL = baseURL + "/dataSet/HAI/prog-new.mln";//prog.mln
        list.add(mlnFileURL);
        String evidence_args = "-e";
        list.add(evidence_args);
        String evidenceFileURL = baseURL + "/dataSet/HAI/evidence.db"; //samples/smoke/
        list.add(evidenceFileURL);
        String queryFile_args = "-queryFile";
        list.add(queryFile_args);
        String queryFileURL = baseURL + "/dataSet/HAI/query.db";
        list.add(queryFileURL);
        String outFile_args = "-r";
        list.add(outFile_args);
        String weightFileURL = baseURL + "/dataSet/HAI/out.txt";
        list.add(weightFileURL);
        String noDropDB = "-keepData";
        //list.add(noDropDB);
        String maxIter_args = "-dMaxIter";
        list.add(maxIter_args);
        String maxIter = "400";
        list.add(maxIter);
        String[] learnwt = list.toArray(new String[list.size()]);
        List<HashMap<String,Double>> attributesPROBList =new ArrayList<HashMap<String, Double>>();


        int batch = 1; // �ɵ���
        int sampleSize = 5000; //�ε���

        for (int i=0;i<batch;i++) {
            rule.resample(newTupleList,sampleSize);
            rule.formatEvidence(evidence_outFile);
            MLNmain.main(learnwt);    //��ڣ�����ѧϰ weight learning����using 'Diagonal Newton discriminative learning'
            //��ȡ����ѧϰ�õ�����Ȩ�أ�����HashMap
            HashMap<String, Double> attributesPROB = Rule.loadRulesFromFile("/home/gcc/experiment/dataSet/HAI/out.txt");
            attributesPROBList.add(attributesPROB);
        }


        //���򻮷� �γ�Domains
        domain.init(dataURL, splitString, ifHeader, rules);
        // domain.printDataSet(dataSet);
        //��ÿ��Domainִ��group by key����
        domain.groupByKey(domain.domains, rules);
        //����MLN�ĸ���������������
        domain.correctByMLN(domain.Domain_to_Groups, attributesPROBList, domain.header, domain.domains);
        //��ӡ�������Domain
         domain.printDomainContent(domain.domains);

        System.out.println(">>> Find Duplicate Values...");
        List<List<Integer>> keysList = domain.combineDomain(domain.Domain_to_Groups);    //���������ظ������tupleID,����¼�ظ�Ԫ��
        //��ӡ�ظ����ݵ�Tuple ID
        if (null == keysList || keysList.isEmpty()) System.out.println("\tNo duplicate exists.");
        else {
            System.out.println("\n>>> Delete duplicate tuples");
            // ����keysList ����Ķ���domain1 ��ÿ��group �����keyֵ����Щ���п��ܻ��ظ��ģ���ȥ��
            // domain.printDataSet(domain.dataSet);
            // domain.deleteDuplicate(keysList, domain.dataSet);	//ִ��ȥ�ز���
            // domain.printDataSet(domain.dataSet);
            System.out.println(">>> completed!");
        }

        domain.printConflicts(domain.conflicts);
        domain.findCandidate(domain.conflicts, domain.Domain_to_Groups, domain.domains, attributesPROBList, ignoredIDs);


        //print dataset after cleaning
        //  domain.printDataSet(domain.dataSet);

        writeToFile(cleanedFileURL, domain.dataSet, domain.header);
//      cleanedFileURL = httpSession.getServletContext().getContextPath()+ "/out/cleanedDataSet.data";//�޸�Ϊ���·��;
        System.out.println("cleanedDataSet.txt stored in=" + cleanedFileURL);
        double endTime = System.currentTimeMillis();    //��ȡ����ʱ��
        double totalTime = (endTime - startTime) / 1000;
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("��������ʱ�䣺 " + df.format(totalTime) + "s");
        //     String cleanFileURL ="C:\\Users\\zmx\\Desktop\\Cleaner-single\\HAI-1q.csv";

    }






    public static void writeToFile(String cleanedFileURL, HashMap<Integer, String[]> dataSet, String[] header) {
        File file = new File(cleanedFileURL);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {// �ж��ļ��Ƿ����
                System.out.println("�ļ��Ѵ���: " + cleanedFileURL);
            } else if (!file.getParentFile().exists()) {// �ж�Ŀ���ļ����ڵ�Ŀ¼�Ƿ����
                // ���Ŀ���ļ����ڵ��ļ��в����ڣ��򴴽����ļ���
                System.out.println("Ŀ���ļ�����Ŀ¼�����ڣ�׼����������");
                if (!file.getParentFile().mkdirs()) {// �жϴ���Ŀ¼�Ƿ�ɹ�
                    System.out.println("����Ŀ���ļ����ڵ�Ŀ¼ʧ�ܣ�");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            Iterator<Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
            writer.write(Arrays.toString(header).replaceAll("[\\[\\]]", ""));
            writer.newLine();//����
            while (iter.hasNext()) {
                Entry<Integer, String[]> entry = iter.next();
                String line = Arrays.toString(entry.getValue()).replaceAll("[\\[\\]]", "");
                writer.write(line);
                writer.newLine();//����
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

