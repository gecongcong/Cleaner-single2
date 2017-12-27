package main;

import data.Domain;
import data.Rule;
import data.Tuple;
import tuffy.main.MLNmain;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;


public class Main {
    static String[] header = null;
    static String baseURL = "/home/zju/experiment/dataSet";    // experiment baseURL
    //static String rootURL = System.getProperty("user.dir"); //Project BaseURL
    static String cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";
    static ArrayList<Integer> ignoredIDs = null;
    public static String rulesURL = baseURL + "/HAI/rules.txt";
    //public static String dataURL = baseURL + "/HAI/HAI-5q-10%-error.csv";


//    public static void updateprogMLN(String oldMLNfile, String dataFile){
//
//        ArrayList<String> rules = new ArrayList<String>();
//        ArrayList<String> newerRules = new ArrayList<String>();
//        try {
//            FileReader reader = new FileReader("/home/zju/experiment/dataSet/HAI/rules.txt");
//            BufferedReader br = new BufferedReader(reader);
//            String line = null;
//            while((line = br.readLine()) != null && line.length()!=0) {
//                rules.add(line);
//            }
//            br.close();
//            HashMap<String, GroundRule> new_results = Rule.createMLN(dataFile, rules);//��ȡrulesģ�壬����ground rules
//            HashMap<String,String> old_results = readMLNFile(oldMLNfile);
//
//            Iterator<Map.Entry<String,GroundRule>> iter = new_results.entrySet().iterator();
//            while (iter.hasNext()) {
//                Map.Entry<String,GroundRule> entry = (Map.Entry<String,GroundRule>) iter.next();
//                String new_tuple = entry.getKey();
//
//                if(null == old_results.get(new_tuple)){
//                    //old_results.put(new_tuple, entry.getValue().weight);
//                    newerRules.add(entry.getValue().weight+",\t"+new_tuple);
//                }
//            }
//            File writefile = new File(oldMLNfile);
//            FileWriter fw = new FileWriter(writefile, true);
//            BufferedWriter bw = new BufferedWriter(fw);
//
//            for(int i=0;i<newerRules.size();i++){
//                bw.write(newerRules.get(i));
//                bw.newLine();
//            }
//            bw.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public static HashMap<String, String> readMLNFile(String mlnFile) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            FileReader reader = new FileReader(mlnFile);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            //escape predicate
//            while((line = br.readLine()) != null) {
//                if(line.length()==0)break;
//            }
            while ((line = br.readLine()) != null && line.length() != 0) {
                String rule_noWeight = line.substring(line.indexOf(",") + 1).trim();
                String weight = line.substring(0, line.indexOf(","));
                result.put(rule_noWeight, weight);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Ϊ���ݼ�����TupleID
     */
    public static void setLineID(String readURL, String writeURL) {
        // read file content from file
        FileReader reader = null;
        try {
            reader = new FileReader(readURL);
            BufferedReader br = new BufferedReader(reader);

            // write string to file

            FileWriter writer = new FileWriter(writeURL);
            BufferedWriter bw = new BufferedWriter(writer);

            String str = "";
            int index = 0;
            while ((str = br.readLine()) != null) {
                str = str.replaceAll(" ", "");
                StringBuffer sb = new StringBuffer(str);
                if (index == 0) {
                    sb.insert(0, "ID,");
                    bw.write(sb.toString() + "\n");
                } else {
                    sb.insert(0, index + ",");
                    bw.write(sb.toString() + "\n");
                }
                index++;
                //System.out.println(sb.toString());
            }
            br.close();
            reader.close();
            bw.close();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void learnwt(String[] args) throws SQLException, IOException {
        String dataURL = args[0];

        double startTime = System.currentTimeMillis();    //��ȡ��ʼʱ��

        Rule rule = new Rule();
        //Domain domain = new Domain();
        String evidence_outFile = baseURL + "/HAI/evidence.db";

        //System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";//�����ϴ������ݼ�
        System.out.println("dataURL = " + dataURL);
        String splitString = ",";

        boolean ifHeader = true;
        //List<Tuple> rules = rule.loadRules(dataURL, rulesURL, splitString);
        rule.initData(dataURL, splitString, ifHeader);//����TupleList ��formatEvidence()ʹ��
        //ArrayList<Tuple> newTupleList = rule.tupleList;
        //dataSet���������ݵļ��ϣ���Ҫ�������ó�
        //ignoredIDs = rule.findIgnoredTuples(rules);
        //domain.header = rule.header;
        //header = rule.header;
        //domain.createMLN(rule.header, rulesURL);

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
        String mlnFileURL = baseURL + "/HAI/prog-new.mln";//prog.mln
        mlnFileURL = args.length > 0 ? args[1] : mlnFileURL;
        // ��������Ĳ�������
        list.add(mlnFileURL);
        String evidence_args = "-e";
        list.add(evidence_args);
        String evidenceFileURL = baseURL + "/HAI/evidence.db"; //samples/smoke/
        list.add(evidenceFileURL);
        String queryFile_args = "-queryFile";
        list.add(queryFile_args);
        String queryFileURL = baseURL + "/HAI/query.db";
        list.add(queryFileURL);
        String outFile_args = "-r";
        list.add(outFile_args);
        String weightFileURL = baseURL + "/HAI/out.txt";
        weightFileURL = args.length > 0 ? args[2] : weightFileURL;
        list.add(weightFileURL);
        String noDropDB = "-keepData";
        list.add(noDropDB);
        String maxIter_args = "-dMaxIter";
        list.add(maxIter_args);
        String maxIter = "400";
        list.add(maxIter);
        String mcsatSamples_args = "-mcsatSamples";
        //list.add(mcsatSamples_args);
        String mcsatSamples = "100";
        //list.add(mcsatSamples);
        String[] learnwt = list.toArray(new String[list.size()]);

        /*
        * ѵ���׶�
        * */

        int batch = 1; // �ɵ���
        int sampleSize = 1000; //�ε���

        for (int i = 0; i < batch; i++) {
            //rule.resample(newTupleList,sampleSize);
            rule.formatEvidence(evidence_outFile);

            //��ڣ�����ѧϰ weight learning����using 'Diagonal Newton discriminative learning'
            MLNmain.main(learnwt);

            //updateprogMLN("/home/zju/experiment/dataSet/HAI/out.txt" , dataURL);
        }
    }

    public static HashMap<Integer, String[]> main(String[] args) throws SQLException, IOException {

        String dataURL = baseURL + "/" + args[0] + "/" + args[1];
//        setLineID(dataURL, dataURL.replaceAll(".csv", "-hasID.csv"));//�����ݼ������Tuple ID
//        String tmp_dataURL = dataURL.replaceAll(".csv", "-hasID.csv");
        String tmp_dataURL = dataURL;
        Rule rule = new Rule();
        Domain domain = new Domain();

        //System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";  //�����ϴ������ݼ�
        System.out.println("dataURL = " + tmp_dataURL);

        String splitString = ",";
        boolean ifHeader = true;
        List<Tuple> rules = rule.loadRules(tmp_dataURL, rulesURL, splitString);
        rule.initData(tmp_dataURL, splitString, ifHeader);
        ignoredIDs = rule.findIgnoredTuples(rules);
        domain.header = rule.header;
        header = rule.header;


        /*
        * ��ȡѵ���׶ε�clausesȨ�ؽ��
        * */
        int batch = 1; // �ɵ���
        List<HashMap<String, Double>> attributesPROBList = new ArrayList<>();
        for (int i = 0; i < batch; i++) {
            //rule.formatEvidence(evidence_outFile);

            //��ȡ����ѧϰ�õ�����Ȩ�أ�����HashMap
            HashMap<String, Double> attributesPROB = Rule.loadRulesFromFile(baseURL + "/" + args[0] + "/out.txt");
            attributesPROBList.add(attributesPROB);
        }

        /*
        * ��ϴ�׶�
        * */
        //���򻮷� �γ�Domains
        domain.init(tmp_dataURL, splitString, ifHeader, rules);

        //domain.printDomainContent(domain.domains);

        //��ÿ��Domainִ��group by key����
        domain.groupByKey(domain.domains, rules);

        //����MLN�ĸ���������������
        domain.correctByMLN(domain.Domain_to_Groups, attributesPROBList, domain.header, domain.domains);

        //��ӡ�������Domain
//        domain.printDomainContent(domain.domains);

        System.out.println(">>> Combine Domains...");
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
        domain.findCandidate(domain.conflicts, domain.domains, attributesPROBList.get(0), ignoredIDs);


        //print dataset after cleaning
        //domain.printDataSet(domain.dataSet);

        writeToFile(cleanedFileURL, domain.dataSet, domain.header);


        return domain.dataSet;
    }

    public static void writeToFile( String[] header, ArrayList<String> list, String outFile){
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {// �ж��ļ��Ƿ����
                System.out.println("�ļ��Ѵ���: " + outFile);
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

            writer.write("ID,"+Arrays.toString(header)
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll(" ", ""));
            writer.newLine();//����

            for(String str: list){
                writer.write(str);
                writer.newLine();
            }
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void writeToFile(String cleanedFileURL, HashMap<Integer, String[]> dataSet, String[] header) {

        List<Map.Entry<Integer, String[]>> list = new ArrayList<>(dataSet.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, String[]>>() {
            @Override
            public int compare(Map.Entry<Integer, String[]> o1, Map.Entry<Integer, String[]> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

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


            writer.write("ID,"+Arrays.toString(header)
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll(" ", ""));
            writer.newLine();//����

            for (Map.Entry<Integer, String[]> map : list) {
                String line = Arrays.toString(map.getValue()).replaceAll("[\\[\\]]", "").replaceAll(" ", "");
                writer.write(map.getKey() + "," + line);
                writer.newLine();//����
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

