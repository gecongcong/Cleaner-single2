package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.sun.org.apache.xpath.internal.operations.Bool;
import info.debatty.java.stringsimilarity.*;
import javafx.scene.input.DataFormat;
import jdk.nashorn.internal.ir.annotations.Ignore;
import spellchecker.SpellChecker;

public class Domain {

    public static double MIN_DOUBLE = 0.0001;
    public static double MIN_NEGNATIVE = -9;
    public static double MAX_NEGNATIVE = -99999999;
    public static double MAX_DOUBLE = 9999;

    public static double THRESHOLD = 0.01;

    public static int AVGNum = 2;

    public HashMap<Integer, String[]> dataSet = new HashMap<>();

    //	public List<String[]> dataSet = new ArrayList<String[]>();
    public List<HashMap<Integer, Tuple>> domains = null;
    //	public List<HashMap<Integer, Tuple>> groups = null;
    public List<List<HashMap<Integer, Tuple>>> Domain_to_Groups = null;    //List<groups> ���group by key��Domain�а�����group�ı��

//    public HashMap<Integer, Conflicts> conflicts = new HashMap<>();    //��¼��ͻ��Ԫ�飬����Domain���� <DomainID, ConflictTuple>

    public ArrayList<Integer> conflicts = new ArrayList<>();//��¼��ͻԪ�����ڵ���ID

    //�����У�������ݼ���û�и���������һ�������У�Attr1,Attr2,Attr3,...,AttrN
    public static String[] header = null;

    public Domain() {
    }

    // ��������������Ǵ��� mln �ļ���
//	public void createMLN(String[] header, String rulesURL){
//		File file = new File(rulesURL);
//		BufferedReader reader = null;
//		ArrayList<String> lines = new ArrayList<String>();
//		try {
//			reader = new BufferedReader(new FileReader(file));
//			String line = null;
//			while ((line = reader.readLine()) != null && line.length()!=0) {
//				lines.add(line);
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e1) {
//				}
//			}
//		}
//
//		String mlnURL = baseURL+"dataSet\\HAI\\prog.mln";//prog.mln;
//		String content = null;
//		try {
//			//��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ�
//			FileWriter writer = new FileWriter(mlnURL, false);
//			for(int i=0;i<header.length;i++){
//				content = header[i]+"(value"+header[i]+")\n";
//				writer.write(content);
//			}
//			writer.write("\n");
//			for(int i=0;i<lines.size();i++){
//				writer.write("1\t"+lines.get(i)+"\n");
//			}
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}


    /**
     * ��rules�����ݼ��������򻮷� Partition DataSet into Domains
     *
     * @param fileURL
     * @param splitString
     * @param ifHeader
     * @param rules
     */
    public HashMap<Integer, String[]> init(String fileURL, String splitString, boolean ifHeader, List<Tuple> rules) {
        // read file content from file ��ȡ�ļ�����
        FileReader reader;
        int rules_size = rules.size();
        domains = new ArrayList<>(rules_size);
        for (int i = 0; i < rules_size; i++) {
            domains.add(new HashMap<>());
        }
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int key; //tuple index

            if (ifHeader && (str = br.readLine()) != null) {  //The data has header
                //   	header=str.split(splitString);
                while ((str = br.readLine()) != null) {
                    //	System.out.println(str);
                    //dataSet.add(str.split(splitString));
//                    str = str.replaceAll(" ", "");
                    key = Integer.parseInt(str.substring(0, str.indexOf(",")));
                    String[] tuple = str.substring(str.indexOf(",") + 1).split(",");
                    dataSet.put(key, tuple);

                    //Ϊÿһ��rule�������ݼ�����Di
                    for (int i = 0; i < rules_size; i++) {
                        Tuple curr_rule = rules.get(i);
                        String[] attributeNames = curr_rule.getAttributeNames();
                        int[] IDs = Rule.findAttributeIndex(attributeNames, header);
                        String[] reason = curr_rule.reason;
                        String[] result = curr_rule.result;
                        int[] reasonIDs = Rule.findAttributeIndex(reason, header);
                        int[] resultIDs = Rule.findResultIDs(IDs, reasonIDs);
                        String[] reasonContent = new String[reasonIDs.length];
                        String[] resultContent = new String[resultIDs.length];
                        String[] tupleContext = new String[IDs.length];
                        int[] tupleContextID = new int[IDs.length];

                        int reason_index = 0;
                        int result_index = 0;

                        for (int j = 0; j < IDs.length; j++) {
                            tupleContext[j] = tuple[IDs[j]];//�������ڸ������tuple����
                            tupleContextID[j] = IDs[j];    //�����Ӧ��ID
                            if (ifReason(IDs[j], reasonIDs)) {    //�����reason,�����reasonContent
                                reasonContent[reason_index++] = tuple[IDs[j]];
                            } else {
                                resultContent[result_index++] = tuple[IDs[j]];
                            }
                        }

                        Tuple t = new Tuple();
                        t.tupleID = key;
                        t.setContext(tupleContext);
                        t.setAttributeIndex(tupleContextID);

                        t.setReason(reasonContent);
                        t.setReasonAttributeIndex(reasonIDs);

                        t.setResult(resultContent);
                        t.setResultAttributeIndex(resultIDs);

                        t.setAttributeNames(attributeNames);

                        //����Di�������,����hashMap
                        domains.get(i % rules_size).put(key, t); //<K,V>  K = tuple ID , from 0 to n
                    }
                    //key++;

                }
            } else {
                int length = 0;
//	        	boolean flag = false;
                while ((str = br.readLine()) != null) {
                    //dataSet.add(str.split(splitString));
                    key = Integer.parseInt(str.substring(0, str.indexOf(",")));
                    String[] tuple = str.substring(str.indexOf(",") + 1).split(",");
                    dataSet.put(key, tuple);

                    for (int i = 0; i < rules_size; i++) {    //Ϊÿһ��rule�������ݼ�����Di
                        Tuple curr_rule = rules.get(i);
                        String[] attributeNames = curr_rule.getAttributeNames();

                        int[] IDs = Rule.findAttributeIndex(attributeNames, header);

                        String[] reason = curr_rule.reason;
//                        String[] result = curr_rule.result;

                        int[] reasonIDs = Rule.findAttributeIndex(reason, header);
                        int[] resultIDs = Rule.findResultIDs(IDs, reasonIDs);
                        String[] reasonContent = new String[reasonIDs.length];
                        String[] resultContent = new String[resultIDs.length];
                        String[] tupleContext = new String[IDs.length];

                        int[] tupleContextID = new int[IDs.length];

                        int reason_index = 0;
                        int result_index = 0;
                        for (int j = 0; j < IDs.length; j++) {
                            tupleContext[j] = tuple[IDs[j]];//�������ڸ������tuple����
                            tupleContextID[j] = IDs[j];    //�����Ӧ��ID
                            if (ifReason(IDs[j], reasonIDs)) {    //�����reason,�����reasonContent
                                reasonContent[reason_index++] = tuple[IDs[j]];
                            } else {
                                resultContent[result_index++] = tuple[IDs[j]];
                            }
                        }

                        Tuple t = new Tuple();
                        t.setContext(tupleContext);
                        t.setAttributeIndex(tupleContextID);
                        t.setReason(reasonContent);

                        t.setReasonAttributeIndex(reasonIDs);

                        t.setResult(resultContent);
                        t.setResultAttributeIndex(resultIDs);
                        t.setAttributeNames(attributeNames);
                        //һ�� t �� һ�� ����
                        //����Di�������,����hashMap
                        domains.get(i % rules_size).put(key, t); //<K,V>  K = tuple ID , from 0 to n
                    }
                    key++;
                }
                System.out.println("header length = " + length);
            }

            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    public static boolean ifContains(int[] IDs, int[] bigIDs) {
        boolean result = false;
        int count = 0;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(bigIDs.length);
        for (int i = 0; i < bigIDs.length; i++) {
            map.put(bigIDs[i], 1);
        }
        int i = 0;
        for (; i < IDs.length && IDs[i] != -1; i++) {
            Integer value = map.get(IDs[i]);
            if (null != value) {
                count++;
            }
        }
        if (count == i) result = true;
        return result;
    }

    public static boolean ifReason(int[] IDs, int[] reasonIDs) {
        boolean result = false;
        int count = 0;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(reasonIDs.length);
        for (int i = 0; i < reasonIDs.length; i++) {
            map.put(reasonIDs[i], 1);
        }
        for (int i = 0; i < IDs.length; i++) {
            Integer value = map.get(IDs[i]);
            if (null != value) {
                count++;
            }
        }
        if (count == IDs.length) result = true;
        return result;
    }


    public static boolean ifReason(int index, int[] reasonIDs) {
        boolean result = false;
        int[] copy_reasonIDs = Arrays.copyOfRange(reasonIDs, 0, reasonIDs.length);
        Arrays.sort(copy_reasonIDs);
        for (int i = 0; i < copy_reasonIDs.length; i++) {
            if (index == copy_reasonIDs[i]) {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * ���λ��֣�����reason predicates ��������
     */
    public List<List<Tuple>> groupByKey(List<HashMap<Integer, Tuple>> domains, List<Tuple> rules) {

        HashMap<Integer, Tuple> domain = null;
        int size = domains.size();
        Domain_to_Groups = new ArrayList<>(size);
        List<List<Tuple>> domain_outlier = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            domain = domains.get(i); // һ��
            int domain_size = domain.size(); //Ԫ�����
            HashSet<Integer> flags = new HashSet<>();

            List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
            List<Tuple> outlierTuple = new ArrayList<>();
            Iterator<Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Tuple> entry = iter.next();
                int k = entry.getKey();
                Tuple tuple1 = entry.getValue(); //һ�� tuple
                HashMap<Integer, Tuple> group = new HashMap<>();
                group.put(k, tuple1);    //add Ti to group(i), now G(i)={Ti}
                String[] reason1 = tuple1.reason;
                String[] content1 = tuple1.getContext();
                //int index=0;

                if (!flags.contains(k)) {
                    Iterator<Entry<Integer, Tuple>> iter2 = domain.entrySet().iterator();
                    boolean flag = false;
                    while (iter2.hasNext()) {
                        Entry<Integer, Tuple> entry2 = iter2.next();
                        if (entry.equals(entry2)) flag = true;
                        else if (flag) {
                            int m = entry2.getKey();
                            if (flags.contains(m)) continue;
                            Tuple tuple2 = entry2.getValue();
                            String[] content2 = tuple2.getContext();
                            String[] reason2 = tuple2.reason;
                            /*String content1_str = Arrays.toString(content1)
                                    .replaceAll("\\[", "")
                                    .replaceAll("]", "")
                                    .replaceAll(" ", "");
                            String content2_str = Arrays.toString(content2)
                                    .replaceAll("\\[", "")
                                    .replaceAll("]", "")
                                    .replaceAll(" ", "");*/
                            JaroWinkler jw = new JaroWinkler();
                            int dis = 0;
                            Cosine cosine = new Cosine();
                            for (int j = 0; j < content1.length; j++) {
                                dis += SpellChecker.distance(content1[j], content2[j]);
                            }
                            if (Arrays.equals(reason1, reason2)) {// ||  ||
                                group.put(m, tuple2);
                                flags.add(m);
                            }
                        }
                    }
                    if (group.size() > AVGNum) {
                        groups.add(group);
                    } else {
                        Iterator<Entry<Integer, Tuple>> iterator = group.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry<Integer, Tuple> entry1 = iterator.next();
                            Tuple t = entry1.getValue();
                            outlierTuple.add(t);
                        }
                    }
                }
            }
            if (groups.size() > 0) {
                Domain_to_Groups.add(groups);
            }
            domain_outlier.add(outlierTuple);
        }

        int d_index = 0;
        for (List<HashMap<Integer, Tuple>> d : Domain_to_Groups) {
            System.out.println("\n*******Domain " + (++d_index) + "*******");
            printGroup(d);
        }

        return domain_outlier;
    }

    public void smoothOutlierToGroup(List<List<Tuple>> domain_outlier, List<List<HashMap<Integer, Tuple>>> Domain_to_Groups,
                                     HashMap<Integer, String[]> dataSet, List<HashMap<String, Double>> attributesPROBList) {

        //HashMap<String, Double> attributesPROB = attributesPROBList.get(0);
        int top_k = 1;

        //convert domain_outlier(List) to HashSet
        HashSet<Integer> hashSet = new HashSet<>();
        for (List<Tuple> tuples : domain_outlier) {
            for (Tuple t : tuples) {
                hashSet.add(t.tupleID);
            }
        }

        //compute tuple minDistance
        for (int i = 0; i < Domain_to_Groups.size(); i++) {
            List<HashMap<Integer, Tuple>> groupList = Domain_to_Groups.get(i);
            List<Tuple> outliers = domain_outlier.get(i);

            for (Tuple outlierT : outliers) {
                String[] s_out_content = dataSet.get(outlierT.tupleID);
                String out_content = "";
                for (int j = 0; j < s_out_content.length; j++) {
                    if (j != s_out_content.length - 1) {
                        out_content += s_out_content[j] + ",";
                    } else out_content += s_out_content[j];
                }
                /*String out_content = Arrays.toString(dataSet.get(outlierT.tupleID))
                        .replaceAll("\\[", "")
                        .replaceAll("]", "")
                        .replaceAll(" ", "");*/
                double minDis = MAX_DOUBLE;
                int minID = 0;
                int mingi = -1;

                class DistanceInfo {
                    int tupleID;
                    double distance;

                    DistanceInfo(int tupleID, double distance) {
                        this.tupleID = tupleID;
                        this.distance = distance;
                    }
                }
                ArrayList<DistanceInfo> disList = new ArrayList<>();

                Iterator<Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<Integer, String[]> entry = iter.next();
                    int key = entry.getKey();
                    if (hashSet.contains(key)) continue;
                    /*Double prob = null;
                    for (int j = 0; j < groupList.size(); j++) { //����curr_content��PROB
                        HashMap<Integer, Tuple> group = groupList.get(j);
                        if (group.containsKey(key)) {
                            Tuple tuple = group.get(key);
                            String[] content = tuple.getContext();
                            String[] tmp_context = new String[content.length];
                            System.arraycopy(tuple.getContext(), 0, tmp_context, 0, content.length);
                            Arrays.sort(tmp_context);
                            String values = Arrays.toString(tmp_context);
                            prob = attributesPROB.get(values);
                            if (null == prob) {
                                prob = MIN_DOUBLE;
                            }
                            break;
                        }
                    }*/
//                    String[] curr_content = entry.getValue();

                    String[] value = entry.getValue();
                    String curr_content = "";
                    for (int j = 0; j < value.length; j++) {
                        if (j != value.length - 1) {
                            curr_content += value[j] + ",";
                        } else curr_content += value[j];
                    }

                    /*String curr_content = Arrays.toString(entry.getValue())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "")
                            .replaceAll(" ", "");*/


                    Cosine cosine = new Cosine();
                    double t_dis = 0;
                    /*
                    for (int j = 0; j < out_content.length; j++) {
                        if (!out_content[j].equals(curr_content[j])) {
                            t_dis += cosine.distance(out_content[j], curr_content[j]);
                        }
                    }*/
                    t_dis = cosine.distance(out_content, curr_content);
                    if (t_dis > 0.1) continue;

                    disList.add(new DistanceInfo(key, t_dis));
                }

                disList.sort(new Comparator<DistanceInfo>() {
                    @Override
                    public int compare(DistanceInfo o1, DistanceInfo o2) {
                        if (o1.distance > o2.distance) {
                            return 1;
                        } else if (o1.distance == o2.distance) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                });
                int n = top_k;
                int size = disList.size();
                if (size < top_k) {
                    n = size;
                }

                List<HashMap<String, Integer>> reverse_group_list = new ArrayList<>(groupList.size());
                for (int gi = 0; gi < groupList.size(); gi++) {
                    HashMap<Integer, Tuple> group = groupList.get(gi);
                    HashMap<String, Integer> reverse_group = new HashMap<>();
                    Iterator<Entry<Integer, Tuple>> it = group.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<Integer, Tuple> entry = it.next();

                        String[] value = entry.getValue().getContext();
                        String content = "";
                        for (int j = 0; j < value.length; j++) {
                            if (j != value.length - 1) {
                                content += value[j] + ",";
                            } else content += value[j];
                        }
                        /*String content = Arrays.toString(entry.getValue().getContext())
                                .replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll(" ", "");*/
                        if (null == reverse_group.get(content)) {
                            reverse_group.put(content, 1);
                        } else {
                            reverse_group.put(content, reverse_group.get(content) + 1);
                        }
                    }
                    reverse_group_list.add(reverse_group);

                }
                double minCOST = MAX_DOUBLE;
                for (int k = 0; k < n; k++) {
                    DistanceInfo disInfo = disList.get(k);
                    double dis = disInfo.distance;
                    int tupleID = disInfo.tupleID;
                    int gi = 0;
                    double t_cost = 0;
                    for (; gi < groupList.size(); gi++) {
                        HashMap<Integer, Tuple> group = groupList.get(gi);
                        if (group.containsKey(tupleID)) {

                            String[] value = group.get(tupleID).getContext();
                            String content = "";
                            for (int j = 0; j < value.length; j++) {
                                if (j != value.length - 1) {
                                    content += value[j] + ",";
                                } else content += value[j];
                            }
                            /*String content = Arrays.toString(group.get(tupleID).getContext())
                                    .replaceAll("\\[", "")
                                    .replaceAll("]", "")
                                    .replaceAll(" ", "");*/
                            int num = reverse_group_list.get(gi).get(content);
                            t_cost = dis / num;
                            break;
                        }
                    }
                    if (t_cost < minCOST) {
                        minCOST = t_cost;
                        minID = tupleID;
                        mingi = gi;
                    }
                }

                if (mingi != -1)
                    groupList.get(mingi).put(outlierT.tupleID, outlierT);//��outlierT�ӵ������Ƶ�group��
            }
        }
    }


    class Tuple2 {
        String content;
        int tupleID;

        Tuple2(int tupleID, String content) {
            this.content = content;
            this.tupleID = tupleID;
        }
    }

    public int replaceNCost(String B, ArrayList<Tuple2> list) { //replace A in list -> B cost N
        //that is to compute how many tuples differ from B
        int N = 0;
        for (int i = 0; i < list.size(); i++) {
            String A = list.get(i).content;
            if (!B.equals(A)) {
                N++;
            } else continue;
        }
        return N;
    }

    public int distanceCost(String[] A, String[] B) {
        Cosine cosine = new Cosine();
        int distance = -1;
        if (A.length != B.length) {
            System.err.println("Error: A != B");
        } else {
            distance = 0;
            for (int i = 0; i < A.length; i++) {
                distance += SpellChecker.distance(A[i], B[i]);
            }
        }

        return distance;
    }

    public HashMap<String, Candidate> spellCheck(HashMap<Integer, Tuple> group) {//����ÿ��tuple���滻����Сcost
        //System.out.println("--------spellCheck-------");

        HashMap<String, Candidate> cMap = new HashMap<>();
        ArrayList<Tuple2> tupleList = new ArrayList<>();
        Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();

        //put tuples into ArrayList
        while (iter.hasNext()) {
            Entry<Integer, Tuple> current = iter.next();
            Tuple tuple = current.getValue();
            int tupleID = current.getKey();
            int length = tuple.getContext().length;
            String[] content = new String[length];
            System.arraycopy(tuple.getContext(), 0, content, 0, length);
            Arrays.sort(content);
            String t = Arrays.toString(content);
//			String t = Arrays.toString(content).replaceAll("\\[","").replaceAll("]","");
            tupleList.add(new Tuple2(tupleID, t));
        }

        for (int i = 0; i < tupleList.size(); i++) {
            Tuple2 t = tupleList.get(i);
            String tuple = t.content;
            double dis = 1000;
            String candidate = tuple;
            //�ҵ���Tuple����Сdistance\
            int tupleID = t.tupleID;

            Cosine cosine = new Cosine();

            for (int j = 0; j < tupleList.size(); j++) {
                Tuple2 tuple2 = tupleList.get(j);
                String tmp_candidate = tuple2.content;
                //tupleID = tuple2.tupleID;
                if (tuple.equals(tmp_candidate)) continue;

                /*MetricLCS lcs = new MetricLCS();
                NormalizedLevenshtein l = new NormalizedLevenshtein();
                JaroWinkler jw = new JaroWinkler();
                QGram qGram = new QGram();*/
                double distance = SpellChecker.distance(tuple, tmp_candidate);
                int N = replaceNCost(tmp_candidate, tupleList);
                double tmp_cost = distance * N;
                if (tmp_cost < dis) {
                    tupleID = tuple2.tupleID;
                    dis = tmp_cost;
                    candidate = tmp_candidate;
                    //tupleID = tupleList.get(j).tupleID;
                }
            }
//			if(tupleID == -1){
//				tupleID = tupleList.get(0).tupleID;
//				candidate = tupleList.get(0).content;
//				cost = 0;
//			}
            cMap.put(tuple, new Candidate(tupleID, candidate, dis));
            //System.out.println("tupleID = "+tupleID+"; "+tuple+" -> "+candidate);
        }
        return cMap;
    }


    /**
     * ����MLN�ĸ���������������
     */
    public void correctByMLN(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<String, Double>> attributesPROBList, String[] header, List<HashMap<Integer, Tuple>> domains) {
        int DGindex = 0;

        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
//			System.out.println("---------------"+(DGindex+1)+"--------------------");

            for (int i = 0; i < groups.size(); i++) {
                HashMap<Integer, Tuple> group = groups.get(i);
                HashMap<Integer, Integer> weight = new HashMap<>();

                HashMap<String, Candidate> cMap = spellCheck(group);

                for (int t = 0; t < attributesPROBList.size(); t++) {
                    HashMap<String, Double> attributesPROB = attributesPROBList.get(t);

                    Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                    double pre_cost = MAX_NEGNATIVE;
                    int tupleID = 0;

                    //����group
                    while (iter.hasNext()) {
                        Entry<Integer, Tuple> current = iter.next();
                        Tuple tuple = current.getValue();

                        if(tuple.tupleID==1728){
                            System.out.println();
                        }

                        int length = tuple.getContext().length;
                        String[] tmp_context = new String[length];
                        System.arraycopy(tuple.getContext(), 0, tmp_context, 0, length);
                        Arrays.sort(tmp_context);
                        String values = Arrays.toString(tmp_context);

                        Double prob = attributesPROB.get(values);
                        Candidate c = cMap.get(values);
                        String candidate = c.candidate;
                        if (prob == null) {
                            prob = MIN_DOUBLE;    //˵�������ֻ�����ݼ�����ֹ�һ�Σ�������Ϊ���Ų��ߴ����ԣ�����=MIN_DOUBLE
                        }
                        double cost;
                        if (c.cost == 0) {
                            cost = MIN_DOUBLE;
                        } else
                            cost = prob * c.cost;
                        if (cost > pre_cost) {
                            pre_cost = cost;
                            tupleID = tuple.tupleID;
//                            tupleID = c.tupleID;
                        }
                    }
                    if (weight.get(tupleID) == null)
                        weight.put(tupleID, 1);
                    else {
                        weight.put(tupleID, weight.get(tupleID) + 1);
                    }
                }
                // ����ҳ���Ϊ��׼��tupleID
                int resultNum = 0;
                int resultTupleID = 0;

                for (Map.Entry<Integer, Integer> entry : weight.entrySet()) {
                    int num = entry.getValue();
                    if (num > resultNum) {
                        resultNum = num;
                        resultTupleID = entry.getKey();
                    }
                }

                // tupleID �Ǳ�ѡ����Ϊ�ɾ����ݵģ�����Ҫ�õ�
                Tuple cleanTuple = group.get(resultTupleID);
                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                //���������ֵ��������ȷ��ȥ�滻��
                if (cleanTuple != null) {
                    while (iter.hasNext()) {
                        Entry<Integer, Tuple> current = iter.next();
                        Tuple copyT = cleanTuple;
//                        Tuple copyT = copyTuple(cleanTuple);
//                        copyT.tupleID = current.getKey();
                        group.put(current.getKey(), copyT);
                        domains.get(DGindex).put(current.getKey(), copyT);
                    }
                }
            }
            DGindex++;
        }
        //����������Group���
        System.out.println("\n=======After Correct Values By MLN Probability=======");
        int d_index = 0;
        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
            System.out.println("\n*******Domain " + (++d_index) + "*******");
            printGroup(groups);
        }
    }

    public static Tuple copyTuple(Tuple t1) {
        Tuple t2 = new Tuple();
        t2.setContext(t1.getContext());
        t2.AttributeIndex = t1.AttributeIndex;
        t2.AttributeNames = t1.getAttributeNames();
        t2.reason = t1.reason;
        t2.result = t1.result;
        t2.reasonAttributeIndex = t1.reasonAttributeIndex;
        t2.resultAttributeIndex = t1.resultAttributeIndex;
        return t2;
    }

    /**
     * ɾ���ظ�����
     */
    public void deleteDuplicate(List<List<Integer>> keyList_list, HashMap<Integer, String[]> dataSet) {
//		System.out.println("\tDuplicate keys: ");
        for (List<Integer> keyList : keyList_list) {
            if (keyList == null) continue;
            for (int i = 0; i < keyList.size() - 1; i++) {
                int key1 = keyList.get(i);
                String[] pre_tuple = dataSet.get(key1);
//				System.out.print("\tGROUP "+i+":"+key1+" ");
                for (int j = i + 1; j < keyList.size(); j++) {
                    int key2 = keyList.get(j);
                    String[] curr_tuple = dataSet.get(key2);
                    if (Arrays.toString(pre_tuple).equals(Arrays.toString(curr_tuple))) { //֮����Ҫ�Ƚ�����Ϊ��Щ����û�г��֣����ܲ�ͬ
//						System.out.print(key2+" ");
                        dataSet.remove(key2);
                    }
                }
//				System.out.println();
            }
        }
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static int[] concat(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    /**
     * �ϲ�����Domain�е�tuple���֣�����ԭ����ͬһ��Tuple
     */

    public Tuple combineTuple(Tuple t1, Tuple t2, int[] sameID) {

        Tuple t = new Tuple();
        // ���Ƿ������ͬ������
        if (sameID.length == 0 || (sameID.length > 0 && sameID[0] == -1)) { //��������ͬ������
            int[] attributeIndex = concat(t1.getAttributeIndex(), t2.getAttributeIndex());
            String[] TupleContext = concat(t1.getContext(), t2.getContext());
            t.setContext(TupleContext);
            t.setAttributeIndex(attributeIndex);
        } else { //������ͬ������

            int[] t1IDs = t1.getAttributeIndex();
            int[] t2IDs = t2.getAttributeIndex();

            // ��� sameIDlength �ĳ���
            int sameIDlength = 0;
            for (int j = 0; j < sameID.length; j++) {
                if (sameID[j] == -1) break;
                sameIDlength++;
            }


            // �µ� attributeindex
            int[] attributeIndex = Arrays.copyOf(t1IDs, t1IDs.length + t2IDs.length - sameIDlength);
            //�µ�tuplecontext
            String[] t1context = t1.getContext();
            String[] t2context = t2.getContext();
            String[] TupleContext = Arrays.copyOf(t1context, attributeIndex.length);

            int k = t1IDs.length;

            for (int i = 0; i < t2IDs.length; i++) {
                boolean flag = false;
                for (int j = 0; j < t1IDs.length; j++) {
                    if (t2IDs[i] == t1IDs[j]) {
                        flag = true;
                        break;
                    }
                }
                if (flag) continue; // ���ڵ�ǰ t2 Ԫ������ԣ�t1 ���к���һ��������
                attributeIndex[k] = t2IDs[i];
                TupleContext[k] = t2context[i];
                k++;
            }


//			for(int i=0; i<t2IDs.length; i++){
//				for(int ID: sameID){
//					if(ID==-1)break;//��ʼ��Ϊ-1�����ѱ�������ֵ�Ĳ���
//					if(t2IDs[i]==ID)continue;
//					attributeIndex[k] = t2IDs[i];
//					TupleContext[k++] = t2context[i];
//				}
//			}
            t.setContext(TupleContext);
            t.setAttributeIndex(attributeIndex);
        }
        return t;
    }


    /**
     * �ҵ���������Ľ���
     */
    public static int[] findSameID(int[] IDs1, int[] IDs2) {
        int[] sameID = null;
        //��ʼ��
        if (IDs1.length > IDs2.length)
            sameID = new int[IDs2.length];
        else
            sameID = new int[IDs1.length];
        for (int i = 0; i < sameID.length; i++)
            sameID[i] = -1;

        //findSameID
        int[] newIDs1 = Arrays.copyOfRange(IDs1, 0, IDs1.length);
        int[] newIDs2 = Arrays.copyOfRange(IDs2, 0, IDs2.length);
        Arrays.sort(newIDs1);
        Arrays.sort(newIDs2);
        int i = 0, j = 0, index = 0;
        while (i < newIDs1.length && j < newIDs2.length) {
            if (newIDs1[i] == newIDs2[j]) {
                sameID[index++] = newIDs1[i];
                ++i;
                ++j;
            } else if (newIDs1[i] < newIDs2[j])
                ++i;
            else
                ++j;
        }
        return sameID;
    }

    /**
     * �Ƚ�����Ԫ��Tuple1��Tuple2���Ƿ������ͬ���ԣ���ֵ��ͬ
     */


    public static boolean checkConflict(Tuple t1, Tuple t2, int[] sameID) {
        boolean result = false;


        String[] context1 = t1.getContext();
        String[] context2 = t2.getContext();

        int[] attributeID1 = t1.getAttributeIndex();
        int[] attributeID2 = t2.getAttributeIndex();


        HashMap<Integer, String> map1 = new HashMap<Integer, String>(context1.length);    // MAP<Attribute name,Attribute value>
        HashMap<Integer, String> map2 = new HashMap<Integer, String>(context2.length);


        // �Ѷ�Ӧ��ֵ�ŵ�map ��

        for (int i = 0; i < context1.length; i++)
            map1.put(attributeID1[i], context1[i]);


        for (int i = 0; i < context2.length; i++)
            map2.put(attributeID2[i], context2[i]);


        Iterator<Entry<Integer, String>> iter = null;

        if (context1.length < context2.length) {
            int i = 0;
            iter = map1.entrySet().iterator();

            while (iter.hasNext()) {
                Entry<Integer, String> entry = iter.next();
                int tupleID = entry.getKey(); // header ������

                String value1 = entry.getValue();
                String value2 = map2.get(tupleID);

                if (value2 == null) continue; // ˵�� tuple2 ��û�и�����
                sameID[i++] = tupleID; // ��¼��ͬ������

                if (!value1.equals(value2)) {//���ڳ�ͻ
                    result = true;
                    break;
                }

            }

        } else {
            int i = 0;
            iter = map2.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, String> entry = iter.next();
                int tupleID = entry.getKey();
                String value1 = entry.getValue();
                String value2 = map1.get(tupleID);
                if (value2 == null) continue;
                sameID[i++] = tupleID;
                if (!value1.equals(value2)) {//���ڳ�ͻ
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public HashMap<Integer, Tuple> combineGroup(List<Integer> keyList, HashMap<Integer, Tuple> group1, HashMap<Integer, Tuple> group2, int Domain1, int Domain2) {

        HashMap<Integer, Tuple> newGroup = new HashMap<>(group1.size()); // new group �Ĵ�С�϶����ᳬ����������һ��group �Ĵ�С

        for (int ki = 0; ki < keyList.size(); ) {

            int key = keyList.get(ki);
            Tuple t1 = group1.get(key);
            Tuple t2 = group2.get(key);

            ConflictTuple ct1 = new ConflictTuple(t1);
            ConflictTuple ct2 = new ConflictTuple(t2);

            int[] sameID = new int[t1.getAttributeIndex().length];

            for (int i = 0; i < sameID.length; i++) {
                sameID[i] = -1;
            }
            if (!checkConflict(t1, t2, sameID)) { //����ͻ
                newGroup.put(key, combineTuple(t1, t2, sameID));
                ki++;
            } else {
                /*System.out.println("Conflict:");
                System.out.println("\tcontext1 = "+Arrays.toString(group1.get(key).TupleContext));
				System.out.println("\tcontext2 = "+Arrays.toString(group2.get(key).TupleContext));*/

                // ��group1 �� group2 �е�Ԫ�鶼ȥ����
                group1.remove(key);
                group2.remove(key);
                //System.out.println("\tID = "+key);

//				for(int m = 0;m<keyList.size();m++){
//					System.out.println(keyList.get(m));
//				}

                // ��keylist �е�ki ��Ӧ��Ԫ�鶼ȥ��
                keyList.remove(ki);

                //����DomainID����ͻ��Tuple��¼����
                // Ԫ�齫��ͻ�����Լ�¼����
//                ct1.setConflictIDs(sameID);
//                ct2.setConflictIDs(sameID);
                // ��¼���ڵ�domain
//                ct1.domainID = Domain1;
//                ct2.domainID = Domain2;

                // ��ͻ��Ԫ����
//                addConflict(key, ct1);
//                addConflict(key, ct2);
                conflicts.add(key);


                // ÿ��Ԫ�鶼�ǳ�ͻ�ģ�return null
                if (keyList.isEmpty()) {
                    return null;
                }

            }
        }
        return newGroup;
    }


    /*public void addConflict(int tupleID, ConflictTuple t) {
        Conflicts oldct = conflicts.get(tupleID);
        if (null == oldct) {    //��Domain����δ��ӳ�ͻԪ��
            Conflicts newct = new Conflicts();
            newct.tuples.add(t);
            conflicts.put(tupleID, newct);
        } else {
            oldct.tuples.add(t);
        }
    }*/

    /**
     * ����keyֵ������group�Ľ���
     */
    public static List<Integer> interset(Integer[] a1, Integer[] a2) {
        int len1 = a1.length;
        int len2 = a2.length;
        int len = a1.length + a2.length;
        Map<Integer, Integer> m = new HashMap<Integer, Integer>(len);

        List<Integer> ret = new ArrayList<Integer>();

        for (int i = 0; i < len1; i++) {
            if (m.get(a1[i]) == null)
                m.put(a1[i], 1);
        }

        for (int i = 0; i < len2; i++) {
            if (m.get(a2[i]) != null)
                m.put(a2[i], 2);
        }

        for (java.util.Map.Entry<Integer, Integer> e : m.entrySet()) {
            if (e.getValue() == 2) {
                ret.add(e.getKey());
            }
        }

        return ret;
    }


    /**
     * ���ظ�group������keyֵ
     */
    public static Integer[] calculateKeys(HashMap<Integer, Tuple> group) {
        Integer[] keys = new Integer[group.size()];
        Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Entry<Integer, Tuple> entry = (Entry<Integer, Tuple>) iter.next();
            keys[i++] = entry.getKey();
        }
        return keys;
    }

    public boolean ifSameValue(int[] conflictIDs, Tuple t, Tuple ct) {
        boolean result = false;
        int count = 0;
        int i = 0;
        int ti = 0, cti = 0;
        for (; i < conflictIDs.length && conflictIDs[i] != -1; i++) {
            while (ti < t.AttributeIndex.length) {
                if (t.AttributeIndex[ti] == conflictIDs[i]) {
                    break;
                } else ti++;
            }
            while (cti < ct.AttributeIndex.length) {
                if (ct.AttributeIndex[cti] == conflictIDs[i]) {
                    break;
                } else cti++;
            }
            if (cti == ct.AttributeIndex.length || ti == t.AttributeIndex.length) return false;
            if (t.TupleContext[ti].equals(ct.TupleContext[cti])) count++;
        }
        if (count == i) result = true;
        return result;
    }

    public boolean ifSameValue(int[] conflictIDs, Tuple t, ConflictTuple ct) {
        boolean result = false;
        int count = 0;
        int i = 0;
        int ti = 0, cti = 0;
        for (; i < conflictIDs.length && conflictIDs[i] != -1; i++) {
            while (ti < t.AttributeIndex.length) {
                if (t.AttributeIndex[ti] == conflictIDs[i]) {
                    break;
                } else ti++;
            }
            while (cti < ct.AttributeIndex.length) {
                if (ct.AttributeIndex[cti] == conflictIDs[i]) {
                    break;
                } else cti++;
            }
            if (cti == ct.AttributeIndex.length || ti == t.AttributeIndex.length) return false;
            if (t.TupleContext[ti].equals(ct.TupleContext[cti])) count++;
        }
        if (count == i) result = true;
        return result;
    }

    /**
     * Ϊ��ͻԪ���ҵ���ѡ���滻����(new)
     */
    public void findCandidate(ArrayList<Integer> conflicts, List<HashMap<Integer, Tuple>> domains, HashMap<String, Double> attributesPROB, ArrayList<Integer> ignoredIDs) {

        for (Integer id : conflicts) {
            double prob = -1;
            Tuple fixTuple = new Tuple();
            boolean ischange = false;

            for (int k = 0; k < domains.size(); k++) {
                Tuple candidateTuple = new Tuple();
                ArrayList<String[]> tmp_list = new ArrayList<>();//��¼�ó�ͻԪ���޸������ж�ÿһ��domain��ѡ��
                HashMap<Integer, Tuple> first_domain = domains.get(k);
                Tuple ct = first_domain.get(id);
                int tupleID = ct.tupleID;

                tmp_list.add(ct.getContext());

                if (null != ct) {
                    Tuple combinedTuple = ct;
                    for (int i = 0; i < domains.size(); i++) {  //�����һ�����ƣ��ų�base Domain
                        if (i == k) {
                            continue;
                        }
                        HashMap<Integer, Tuple> domain = domains.get(i);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();

                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);//�����
                        if (sameID[0] != -1) {//˵���н���������
                            Iterator<Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
                            double maxProb = MIN_DOUBLE;
                            Tuple maxTuple = null;
                            while (iter.hasNext()) {
                                Entry<Integer, Tuple> en = iter.next();
                                Tuple t = en.getValue();
                                //˵������ֵҲ��ͬ
                                if (ifSameValue(sameID, t, combinedTuple)) {  //ifContains(sameID, t.AttributeIndex) &&

                                    String[] tmp_context = new String[t.getContext().length];
                                    System.arraycopy(t.getContext(), 0, tmp_context, 0, t.getContext().length);
                                    Arrays.sort(tmp_context);
                                    String value = Arrays.toString(tmp_context);
                                    Double curr_prob = attributesPROB.get(value);

                                    if (curr_prob == null) {
                                        curr_prob = MIN_DOUBLE;
                                    }
                                    if (curr_prob > maxProb) {
                                        maxProb = curr_prob;
                                        maxTuple = t;
                                    }
                                    /*System.err.println("3.current maxTuple = " + Arrays.toString(maxTuple.TupleContext));
                                    System.err.println("attribute id = " + Arrays.toString(maxTuple.AttributeIndex));*/
                                }
                            }
                            if (null != maxTuple) {
                                tmp_list.add(maxTuple.getContext());
                                combinedTuple = combineTuple(combinedTuple, maxTuple, sameID);

                                /*System.err.println("3.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                System.err.println("attribute id = " + Arrays.toString(combinedTuple.AttributeIndex));*/
                            } else {
//                                System.out.println("debug here");
                            }
                        } else {//û�н�����ֱ�Ӻϲ�
                            Tuple t = domain.get(tupleID);
                            combinedTuple = combineTuple(combinedTuple, t, sameID);
                            tmp_list.add(t.getContext());

                            /*System.err.println("4.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                            System.err.println("attribute id = " + Arrays.toString(combinedTuple.AttributeIndex));*/

                        }
                    }
                    candidateTuple = combinedTuple;
                } else {
                    System.out.println("Can't find candidate for conflict tuple");
                }

                double tmp_prob = 1;
                for (String[] value : tmp_list) {

                    String[] tmp_context = new String[value.length];
                    System.arraycopy(value, 0, tmp_context, 0, value.length);
                    Arrays.sort(tmp_context);
                    Double curr_prob = attributesPROB.get(Arrays.toString(tmp_context));
                    if (null == curr_prob) {
                        curr_prob = MIN_DOUBLE;
                    }
                    tmp_prob *= curr_prob;
                }
                String[] old_tuple_part = new String[candidateTuple.AttributeIndex.length];
                for (int i = 0; i < candidateTuple.AttributeIndex.length; i++) {
                    int index = candidateTuple.AttributeIndex[i];
                    old_tuple_part[i] = dataSet.get(id)[index];
                }
                double dis = distanceCost(candidateTuple.getContext(), old_tuple_part);
                if (dis == 0) {
                    dis = MIN_DOUBLE;
                }
                tmp_prob /= dis;
                if (tmp_prob > prob) {
                    ischange = true;
                    prob = tmp_prob;
                    fixTuple = candidateTuple;
//                    System.out.print("P = " + prob + " , " + Arrays.toString(fixTuple.getContext()) + "\n");
                }
            }

            //�޸�dataset����һ��������
            if (ischange) {
                String[] ignoredValues = null;
                if (ignoredIDs.size() > 0) {
                    ignoredValues = new String[ignoredIDs.size()];
                    for (int i = 0; i < ignoredIDs.size(); i++) {
                        ignoredValues[i] = dataSet.get(id)[ignoredIDs.get(i)];
                    }
                }

                String[] newTupleContext = new String[header.length];
                for (int i = 0; i < header.length; i++) {
                    if (ignoredIDs.size() > 0) {
                        for (int j = 0; j < ignoredIDs.size(); j++) {
                            if (i == ignoredIDs.get(j)) {
                                newTupleContext[i] = ignoredValues[j];
                            }
                        }
                    }
                    for (int k = 0; k < fixTuple.AttributeIndex.length; k++) {
                        if (i == fixTuple.AttributeIndex[k]) {
                            newTupleContext[i] = fixTuple.TupleContext[k];
                        }
                    }
                }
                String[] old_tuple = dataSet.get(id);
                for (int index = 0; index < newTupleContext.length; index++) {
                    if (null == newTupleContext[index]) {
                        newTupleContext[index] = old_tuple[index];
                    }
                }
                dataSet.put(id, newTupleContext);
//                System.out.println("ID = [" + id + "] fix Tuple = " + Arrays.toString(newTupleContext));
//                System.out.println("fix content = " + Arrays.toString(fixTuple.getContext()));
//                System.out.println("attribute id = " + Arrays.toString(fixTuple.AttributeIndex));
            }

        }
        System.out.println();
    }

    /**
     * Ϊ��ͻԪ���ҵ���ѡ���滻����(old)
     */
    public void findCandidate(HashMap<Integer, Conflicts> conflicts, List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<Integer, Tuple>> domains, List<HashMap<String, Double>> attributesPROBList, ArrayList<Integer> ignoredIDs) {

        Tuple candidateTuple = new Tuple();
        //���ݳ�ͻԪ�飬����ͬ������γɺ�ѡ����������

        Iterator<Entry<Integer, Conflicts>> conflict_iter = conflicts.entrySet().iterator();

        while (conflict_iter.hasNext()) {
            Boolean ischange = false;

            Entry<Integer, Conflicts> entry = conflict_iter.next();
            int tupleID = entry.getKey();
            Conflicts conf = entry.getValue();
            List<ConflictTuple> list = conf.tuples;

            HashMap<Tuple, Integer> weight = new HashMap<>();

            for (int tt = 0; tt < attributesPROBList.size(); tt++) {

                HashMap<String, Double> attributesPROB = attributesPROBList.get(tt);

                for (ConflictTuple ct : list) {

                    int[] conflictIDs = ct.conflictIDs;
                    int conflictDomainID = ct.domainID;
                    int length = Domain_to_Groups.size();
                    boolean[] flag = new boolean[length];
                    Tuple combinedTuple = ct;
                    //ȥƥ����һ�������Ԫ��ֵ
                    for (int i = 0; i < length; i++) {
                        if (flag[i] || i == conflictDomainID) continue;
                        HashMap<Integer, Tuple> domain = domains.get(i);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();
                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);
                        if (sameID[0] != -1) { //˵���н���������
                            List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
                            for (HashMap<Integer, Tuple> group : cur_groups) {
                                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                                while (iter.hasNext()) {
                                    Entry<Integer, Tuple> en = iter.next();
                                    Tuple t = en.getValue();
                                    if (ifContains(sameID, t.AttributeIndex) && ifSameValue(sameID, t, ct)) {
                                        flag[i] = true;
                                        combinedTuple = combineTuple(combinedTuple, t, sameID);

                                        //System.err.println("1.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                    /*for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                        if (combinedTuple.TupleContext[a] == null) {
											System.err.println("debug here 1");
										}
									}*/
                                        break;
                                    }
                                }
                                if (flag[i]) break;
                            }
                        } else {
                            combinedTuple = combineTuple(combinedTuple, domain.get(tupleID), sameID);
                            //System.err.println("2.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));

						/*for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                            if (combinedTuple.TupleContext[a] == null) {
								System.err.println("debug here 2");
							}
						}*/
                            flag[i] = true;
                        }
                    }

                    //��group��ƥ�䲻������ȥDomain��ƥ��
                    for (int fi = 0; fi < flag.length; fi++) {
                        //System.out.println(Arrays.toString(flag));
                        if (flag[fi] || fi == conflictDomainID) continue;
                        HashMap<Integer, Tuple> domain = domains.get(fi);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();

                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);
                        if (sameID[0] != -1) {
                            Iterator<Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
                            while (iter.hasNext()) {
                                Entry<Integer, Tuple> en = iter.next();
                                Tuple t = en.getValue();
                                if (ifContains(sameID, t.AttributeIndex) && ifSameValue(sameID, t, ct)) {
                                    flag[fi] = true;
                                    combinedTuple = combineTuple(combinedTuple, t, sameID);
                                    System.err.println("3.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                    for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                        if (combinedTuple.TupleContext[a] == null) {
                                            System.err.println("debug here 3");
                                        }
                                    }
                                    break;
                                }
                            }
                            if (flag[fi]) break;
                        } else {
                            combinedTuple = combineTuple(combinedTuple, domain.get(tupleID), sameID);
                            System.err.println("4.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                            for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                if (combinedTuple.TupleContext[a] == null) {
                                    System.err.println("debug here 4");
                                }
                            }
                            flag[fi] = true;
                        }

                    }
                    //System.out.println(Arrays.toString(flag));
                    int count = 0;
                    for (int i = 0; i < flag.length; i++)
                        if (flag[i] == false) count++;

                    if (count == 1) {
                        String[] content = combinedTuple.TupleContext;
                        int[] contentID = combinedTuple.AttributeIndex;

                        //�����tuple��probability
                        String[] tmp_context = new String[content.length];
                        System.arraycopy(content, 0, tmp_context, 0, content.length);
                        Arrays.sort(tmp_context);
                        String value = Arrays.toString(tmp_context);
                        Double prob = attributesPROB.get(value);
                        if (prob == null) prob = 0.0;
//					for (int j = 0; j < length; j++) {
//						String result = header[contentID[j]] + "(" + content[j] + ")";
//						Double tmpProb = attributesPROB.get(result);
//						if (tmpProb == null) continue;//������result�ĸ���Ϊ1
//						prob *= tmpProb;
//					}
                        if (prob > candidateTuple.probablity) {
                            ischange = true;
                            candidateTuple = combinedTuple;
                            candidateTuple.probablity = prob;
                        }

                    }
                }
                if (weight.get(candidateTuple) == null) weight.put(candidateTuple, 1);
                else weight.put(candidateTuple, weight.get(candidateTuple) + 1);

            }
            int resultNum = 0;
            Tuple resultTuple = null;
            for (Map.Entry<Tuple, Integer> entryy : weight.entrySet()) {
                int num = entryy.getValue();
                if (num > resultNum) {
                    resultNum = num;
                    resultTuple = entryy.getKey();
                }
            }
            candidateTuple = resultTuple;

            //�޸�dataset����һ��������
            if (ischange) {

                String[] ignoredValues = null;
                if (ignoredIDs.size() > 0) {
                    ignoredValues = new String[ignoredIDs.size()];
                    for (int i = 0; i < ignoredIDs.size(); i++) {
                        ignoredValues[i] = dataSet.get(tupleID)[ignoredIDs.get(i)];
                    }
                }

                String[] newTupleContext = new String[header.length];
                for (int i = 0; i < header.length; i++) {
                    if (ignoredIDs.size() > 0) {
                        for (int j = 0; j < ignoredIDs.size(); j++) {
                            if (i == ignoredIDs.get(j)) {
                                newTupleContext[i] = ignoredValues[j];
                            }
                        }
                    }
                    for (int k = 0; k < candidateTuple.AttributeIndex.length; k++) {
                        if (i == candidateTuple.AttributeIndex[k]) {
                            newTupleContext[i] = candidateTuple.TupleContext[k];
                        }
                    }
                }
                String[] old_tuple = dataSet.get(tupleID);
                for (int index = 0; index < newTupleContext.length; index++) {
                    if (null == newTupleContext[index]) {
                        newTupleContext[index] = old_tuple[index];
                    }
                }
                dataSet.put(tupleID, newTupleContext);
                System.out.println("\ntupleID = [" + tupleID + "] candidate Tuple = " + Arrays.toString(newTupleContext));
            }
        }
    }


    public List<List<Integer>> combineDomain(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups) {
        List<List<Integer>> keysList = null;
        //ֻ��һ��Domain������Ҫ�ϲ�
        if (Domain_to_Groups.size() > 1) {
            List<HashMap<Integer, Tuple>> pre_groups = Domain_to_Groups.get(0);
            keysList = new ArrayList<>(pre_groups.size());

            int preDomainID = 0;
            int curDomainID = 0;


            //��һ��Domain������group��keyList
            //	for(HashMap<Integer, Tuple> group: pre_groups){
            //		keysList.add(Arrays.asList(calculateKeys(group)));
            //	}

//		for(List<Integer> list :keysList){
//			for(Integer output :list)
//				System.out.print(output);
//				System.out.println();
//		}

            for (int i = 1; i < Domain_to_Groups.size(); i++) {

                curDomainID = i;
                List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
                int pre_groups_index = 0;
                int pre_groups_size = pre_groups.size();
                int cur_groups_index = 0;
                int cur_groups_size = cur_groups.size();
                List<HashMap<Integer, Tuple>> temp = new ArrayList<HashMap<Integer, Tuple>>();
                while (pre_groups_index < pre_groups_size && cur_groups_index < cur_groups_size) {
                    HashMap<Integer, Tuple> cur_group = cur_groups.get(cur_groups_index);
                    HashMap<Integer, Tuple> pre_group = pre_groups.get(pre_groups_index);
                    //������group�Ľ���
                    List<Integer> keyList = interset(calculateKeys(pre_group), calculateKeys(cur_group));
                    if (keyList.size() != 0) {
                        pre_group = combineGroup(keyList, pre_group, cur_group, preDomainID, curDomainID);
                        if (keyList.size() > 1) {
                            keysList.add(keyList);
                            temp.add(pre_group);
                        }
                    }
                    cur_groups_index++;// �����ǰgroup��ǰһ�� domain ��groupû�н�������ô������һ��group��ƥ��
                    //�����ǰdomain �Ѿ�û�� �������� ƥ��� group �Ļ� ����ô ��ƥ��ǰһ�� domain �е���һ��group
                    if (cur_groups_index == cur_groups_size) {
                        cur_groups_index = 0;
                        pre_groups_index++;
                    }
                }
                pre_groups = temp;
                preDomainID = i;
            }
        }


        //===========test==========
//		int d_index = 0;
//		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
//			System.out.println("\n*******Domain "+(++d_index)+"*******");
//			printGroup(groups);
//		}
        //===========test==========


        System.out.println(">>> Fix the error values...");
        int i = 0;
        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
            i++;
            if (i == 9) {
                System.out.println("debug here");
            }
            for (HashMap<Integer, Tuple> group : groups) {
                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                //���������ֵ��������ȷ��ȥ�滻��
                while (iter.hasNext()) {
                    Entry<Integer, Tuple> entry = iter.next();
                    int currentKey = entry.getKey();
                    Tuple cleanTuple = entry.getValue();
                    int[] AttrIDs = cleanTuple.getAttributeIndex();
                    String[] values = cleanTuple.getContext();
                    //   String[] tuple = null;
                    String[] tuple = dataSet.get(currentKey);
                    for (int k = 0; k < AttrIDs.length; k++) {
                        //tuple = dataSet.get(currentKey);
                        tuple[AttrIDs[k]] = values[k];
                    }
                    dataSet.put(currentKey, tuple);
                }
            }
        }
        System.out.println(">>> Completed!");
        return keysList;
    }


    /**
     * ����ظ�����
     * */
    /*
    public List<List<Integer>> checkDuplicate(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups){

		List<List<Integer>> keyList_list = new ArrayList<List<Integer>>();
		int round = 0;

		if(Domain_to_Groups.size()<2){	//�������ظ�����
			return keyList_list;
		}

		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			if(round == 0){	//��һ��ѭ��,��ǵ�һ��Domain��groups�е��ظ�����
				for(HashMap<Integer, Tuple> group: groups){
					List<Integer> keyList = new ArrayList<Integer>();
					for (int key: group.keySet()) {
						//save keys
						keyList.add(key);
					}
					keyList_list.add(keyList); 	//��һ��Domain�У��ж��ٸ�group ���ж��ٸ�keyList_list
				}
				round++;
			}else{

				for(int k=0;k<keyList_list.size();k++){
					List<Integer> keyList = keyList_list.get(k);
					List<Integer> newList = new ArrayList<Integer>();
					for(int i=0;i<keyList.size();i++){
						int key = keyList.get(i);

						for(int index = 0;index < groups.size();index++){
							HashMap<Integer, Tuple> group = groups.get(index);
							if(group.containsKey(key)){
								newList.add(key);
								break;
							}else continue;
						}
					}
					if(newList.size()==0){
						keyList_list.remove(keyList);
					}else{
						keyList_list.set(k, newList);
					}
				}
			}
		}
		//�ϲ�����������
		return keyList_list;
	}

	*/

    /**
     * ���������еı��ID�͵�ǰ��������Tuple����ö�Ӧ������ֵvalues
     *
     * @return values
     */

    public static String[] getValues(Tuple t, int[] attributeIDs) {
        int length = attributeIDs.length;
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = t.reason[attributeIDs[i]];
        }
        return values;
    }

    /**
     * ��ӡ���ֺ������domain
     *
     * @param domains
     */
    public void printDomainContent(List<HashMap<Integer, Tuple>> domains) {
        HashMap<Integer, Tuple> domain = null;
        for (int i = 0; i < domains.size(); i++) {
            domain = domains.get(i);
            Iterator iter = domain.entrySet().iterator();
            System.out.println("\n---------Domain " + (i + 1) + "---------");

            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
                System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }

        //System.out.println("\nDomain.size = "+domains.size()+"\n");
        System.out.println("\n>>> Completed!");
    }

    /**
     * ��ӡ���ֺ������Group
     */
    public static void printGroup(List<HashMap<Integer, Tuple>> groups) {
        HashMap<Integer, Tuple> group = null;
        for (int i = 0; i < groups.size(); i++) {
            group = groups.get(i);
            Iterator iter = group.entrySet().iterator();
            System.out.println("\n---------Group " + (i + 1) + "---------");

            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
//                if ((Integer) key == 0)
                System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }
//		System.out.println("\nGroup.size = "+groups.size()+"\n");
        System.out.println(">>> Completed!");
    }


    /**
     * ��ӡ�������ݼ�������
     */
    public void printDataSet(HashMap<Integer, String[]> dataSet) {
        System.out.println("\n==========DataSet Content==========");

        Iterator iter = dataSet.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            Object key = entry.getKey();
            String[] value = (String[]) entry.getValue();
            System.out.println("key = " + key + " value = " + Arrays.toString(value));
        }
        System.out.println();
    }

    public void printConflicts(ArrayList<Integer> conflicts) {
        conflicts.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1 < o2) {
                    return -1;
                } else return 1;
            }
        });
        System.out.print("Conflict ID: [");
        for (Integer id : conflicts) {
            System.out.print("" + id + " ");
        }
        System.out.print("]\n");
    }

    public void printConflicts(HashMap<Integer, Conflicts> conflicts) {
        System.out.println("Conflict tuples:");
        Iterator<Entry<Integer, Conflicts>> iter = conflicts.entrySet().iterator();
        if (conflicts.size() == 0) {
            System.out.println("No Conflict tuples.");
            return;
        }
        while (iter.hasNext()) {
            Entry<Integer, Conflicts> entry = (Entry<Integer, Conflicts>) iter.next();
            Object key = entry.getKey();
            Conflicts value = entry.getValue();
            List<ConflictTuple> tuples = value.tuples;
            System.out.print("Tuple ID = " + key + "\n" + "Content = ");
            for (Tuple t : tuples) {
                System.out.print(Arrays.toString(t.getContext()) + " ");
            }
            System.out.println();
        }
    }
}
