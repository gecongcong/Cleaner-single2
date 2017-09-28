package data;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

import tuffy.util.Config;
import org.postgresql.Driver;

public class Rule {

    String predicate = null;
    String value = null;
    public String[] header = null;
    public ArrayList<Tuple> tupleList = new ArrayList<Tuple>();


    public Rule() {}


    //
    public ArrayList<Integer> findIgnoredTuples(List<Tuple> rules) {
        ArrayList<Integer> ignoredIDs = new ArrayList<Integer>();
        HashMap<String, Integer> map = new HashMap<String, Integer>(header.length);

        for (Tuple rule : rules) {
            int i = 0;
            while (i < rule.reason.length) {
                map.put(rule.reason[i++], 1);
            }
            int j = 0;
            while (j < rule.result.length) {
                map.put(rule.result[j++], 1);
            }
        }

        for (int i = 0; i < header.length; i++) {
            String predicate = header[i];
            Integer result = map.get(predicate);
            if (null == result) {//find ignored tuple predicate
                ignoredIDs.add(i);
            }
        }

        // 打印所有被ignore的ID
        System.out.print("Ignored Tuple ID:");
        for (int i : ignoredIDs) {
            System.out.println(i + " ");
        }
        return ignoredIDs;
    }


    /**
     * 閺夆晜鏌ㄥú鏍础閺囨岸鍤嬮悘鐐靛仦閿熺獤鍐╁?抽柣銊ュ婢у秹宕烽妸銉ョ仚闁汇劌瀚槐顏堝矗閿燂拷
     *
     * @return Attribute Index
     */
    public static int findAttributeIndex(String name, String[] header) {
        int index = 0;
        for (; index < header.length; index++) {
            if (header[index].equals(name))
                break;
        }
        return index;
    }


    /**
     * 閺夆晜鏌ㄥú鏍ㄥ緞濮橆偊鍤嬮悘鐐靛仦閿熺獤鍐╁?抽柣銊ュ婢у秹宕烽妸銉ョ仚闁汇劌瀚槐顏堝矗閿燂拷
     *
     * @return Attribute Indexes
     */

    public static int[] findAttributeIndex(String[] name, String[] header) {
        boolean[] flag = new boolean[header.length];
        int[] attributeIDs = new int[name.length];
        for (int i = 0; i < flag.length; i++) {
            flag[i] = false;
        }
        for (int i = 0; i < name.length; i++) {
            for (int index = 0; index < header.length; index++) {
                if (flag[index] == false && header[index].equals(name[i])) {
                    attributeIDs[i] = index;
                    flag[index] = true;
                    break;
                }
            }
        }
        return attributeIDs;
    }


    /**
     * @param tuple  IDs
     * @param reason IDs
     */

    public static int[] findResultIDs(int[] tupleIDs, int[] reasonIDs) {
        int[] resultIDs = new int[tupleIDs.length - reasonIDs.length];
        int index = 0;

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < tupleIDs.length; ++i) {
            boolean bContained = false;
            for (int j = 0; j < reasonIDs.length; ++j) {
                if (tupleIDs[i] == reasonIDs[j]) {
                    bContained = true;
                    break;
                }
            }

            if (!bContained) {
                resultIDs[index++] = tupleIDs[i];
                //list.add(tupleIDs[i]);
            }

        }


//        int res[] = new int[list.size()];
//        for(int i = 0; i < list.size(); ++i)
//            res[i] = list.get(i);
//		
        return resultIDs;
    }

    public static HashMap<String,GroundRule> createMLN(String filename,ArrayList<String> rules){
        //HashMap<String,GroundRule> results = new HashMap<String,GroundRule>();
        HashMap<String, GroundRule> map = new HashMap<String, GroundRule>();
        try{
            File file = new File(filename);//读文件
            File writefile = new File("/home/gcc/experiment/dataSet/HAI/rules-new.txt");
            if (!writefile.exists()) {
                writefile.createNewFile();
            }
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(file));
            String tmp = null;
            String[] header = null;
            int i=0;
            // 一次读入一行，直到读入null为文件结束
            while ((tmp = br.readLine()) != null) {
                if(i==0){
                    i++;
                    header = tmp.replaceAll(" ","").split(",");
                    continue;
                }else{
                    String[] tuple = tmp.replaceAll(" ","").split(",");
                    for(int k=0;k<rules.size();k++){
                        String currentRule = rules.get(k);
                        for(int j=0;j<header.length;j++){
                            if(currentRule.indexOf(header[j])!=-1){
                                currentRule = currentRule.replaceAll("value"+header[j],"\""+tuple[j]+"\"");
                                //rules.set(k,currentRule);
                            }
                        }

                        //String weight = currentRule.substring(0,currentRule.indexOf(","));
                        //String rule_noWeight = currentRule.substring(currentRule.indexOf(",")+1).trim();

                        if(map.get(currentRule)==null){

                            GroundRule gr = new GroundRule("",1);
                            map.put(currentRule,gr);
                        }else{
                            map.get(currentRule).number += 1;
                            map.put(currentRule,map.get(currentRule));
                        }
                        System.out.println(currentRule);
                    }
                }
            }
            Iterator<Map.Entry<String,GroundRule>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,GroundRule> entry = (Map.Entry<String,GroundRule>) iter.next();
                String key = entry.getKey();
                GroundRule value = entry.getValue();
                double weight = (double)value.number*10/map.size();
                map.get(key).weight = String.format("%.2f", weight);
                String result = map.get(key).weight+"\t"+key+"\n";
                bw.write(result);
                //results.put(result, 1);
            }
            br.close();
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return map;
    }

    public void getHeader(String DBurl, String splitString) {
        try {
            FileReader reader;
            reader = new FileReader(DBurl);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            if ((line = br.readLine()) != null) {
                header = line.split(splitString);
            } else {
                System.err.println("Error: No header!");
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getting predicates
     *
     * @param fileURL
     * @param splitString
     * @return List<String[]>
     * @throws IOException
     */
    public List<Tuple> loadRules(String DBurl, String fileURL, String splitString) throws IOException {
        System.out.println(">>> Getting Predicates.......");
        FileReader reader;
        String[] reason_predicates = null;
        String[] result_predicates = null;
        List<Tuple> list = new ArrayList<Tuple>();
        getHeader(DBurl, splitString);

        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;

            //  String current = "";
            int index = 0;


            // 用来存储出现过的规则
            List<String> store = new ArrayList<String>();

            while ((line = br.readLine()) != null && line.length() != 0) {  //The data has header

                line = line.replaceAll("1\t", "").replaceAll(" ", "");

                String[] line_partiton = line.split("=>");

                String[] reason = line_partiton[0].split(splitString);
                String[] result = line_partiton[1].split(splitString);

                int reason_length = reason.length;
                int result_length = result.length;

                reason_predicates = new String[reason_length];
                result_predicates = new String[result_length];

                for (int i = 0; i < reason_length; i++) {
                    reason_predicates[i] = reason[i].replaceAll("\\(.*\\)", "");
                }
                for (int i = 0; i < result_length; i++) {
                    result_predicates[i] = result[i].replaceAll("\\(.*\\)", "");
                }

                Tuple t = new Tuple();
                t.reason = reason_predicates;
                t.result = result_predicates;

                //t.setReasonAttributeIndex(findAttributeIndex(reason_predicates, header));

                String[] combine = new String[reason_length + result_length]; //将 reason 和 result 合并

                System.arraycopy(reason_predicates, 0, combine, 0, reason_length);
                System.arraycopy(result_predicates, 0, combine, reason_length, result_length);
                t.setAttributeNames(combine);
                //t.index = index++;
                Arrays.sort(combine);
                if (store.contains(Arrays.toString(combine))) continue;
                store.add(Arrays.toString(combine));
                System.out.println(Arrays.toString(combine));
                list.add(t);
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">>> Completed!");
        return list;
    }

    public static ArrayList<String[]> readFile(String URL){
        ArrayList<String[]> tuples = new ArrayList<String[]>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            String current = "";
            int key = 0; //tuple index
            br.readLine();
            while((line = br.readLine()) != null && line.length()!=0){
                String[] tuple = line.split(",");
                tuples.add(tuple);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tuples;
    }

    /**
     * @param outFile
     * @throws IOException
     */
    public void formatEvidence(String outFile) throws IOException {
        String content = "";
        //Clean all the out content in 'outFile'
        FileWriter fw;
        System.out.println(">> Write Evidence to file (evidence.db) ...");

        try {
            fw = new FileWriter(outFile);
            fw.write("");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int index = 1;
        class IndexAndCount {
            int count;
            int index;
            int headerIndex;

            IndexAndCount(int count, int headerIndex) {
                this.count = count;
                this.index = 0;
                this.headerIndex = headerIndex;
            }

            public void increase() {
                count++;
            }

            public void setIndex(int index) {
                this.index = index;
            }

            public double getCount() {
                return (double) count;
            }

            public int getIndex() {
                return index;
            }

            public int getHeaderIndex() {
                return headerIndex;
            }
        }

        /*//记录一个属性下所有的值和对应的出现次数，和对应的 属性索引
        HashMap<String, IndexAndCount> map = new HashMap<>();
        ArrayList<String[]> tupleList = readFile(dataFile);
        for (int i = 0; i < header.length; i++) {
            for(int k = 0; k < tupleList.size(); k++){
                String item = tupleList.get(k)[i];
                if (!map.containsKey(item)) {
                    map.put(item, new IndexAndCount(1, i));
                } else {
                    map.get(item).increase();
                }
            }
        }*/

        //记录一个属性下所有的值和对应的出现次数，和对应的 属性索引
        HashMap<String, IndexAndCount> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            for (int k = 0; k < tupleList.size(); k++) {
                String item = tupleList.get(k).getContext()[i];
                if (!map.containsKey(item)) {
                    map.put(item, new IndexAndCount(1, i));
                } else {
                    map.get(item).increase();
                }
            }
        }


        for (Map.Entry<String, IndexAndCount> entry : map.entrySet()) {
            // System.out.println(entry);
            if (entry.getValue().getIndex() == 0) { //这个是标志entry的位置
                entry.getValue().setIndex(index);
                index++;
            }
//          System.out.println(entry.getValue().getIndex()+' '+entry.getKey());
            double pre = entry.getValue().getCount() / tupleList.size(); // 属性值占的百分比
            pre += 0.0001;
            if (pre > 1) pre = 1;

            DecimalFormat format = new DecimalFormat("#0.0000");
            content += format.format(pre) + " ";
            content += header[entry.getValue().getHeaderIndex()] + "(\"" + entry.getKey() + "\")" + "\n";
        }

        writeToFile(content, outFile);
        System.out.println(">> Writing Completed!");

        String url = Config.db_url;
        String username = Config.db_username;
        String password = Config.db_password;

        try {
            Class.forName("org.postgresql.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // 下面的代码主要是向数据库写入数据文件

            String sql = "DROP TABLE IF EXISTS temp CASCADE;";
            stmt.execute(sql);

            sql = "CREATE TABLE temp(";
            for (int i = 0; i < header.length; i++) {
                sql += i == header.length - 1 ? header[i] + " bigint);" : header[i] + " bigint,";
            }
            System.out.println(sql);
            stmt.execute(sql);

            // create index ?
            sql = "CREATE INDEX temp_idx ON temp (";
            for (int i = 0; i < header.length; i++) {
                sql += i == header.length - 1 ? header[i] + ");" : header[i] + ", ";
            }

            System.out.println(sql);
            stmt.execute(sql);

            for (Tuple t : tupleList) {
                sql = "INSERT INTO temp VALUES(";
                for (int i = 0; i < t.getContext().length; i++) {
                    String item = t.getContext()[i];
                    sql += i == t.getContext().length - 1 ? map.get(item).getIndex() : map.get(item).getIndex() + ",";
                }
                sql += ");";
                // System.out.println(sql);
                stmt.execute(sql);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param fileURL
     * @param outFile
     */
    public void formatRules(String fileURL, String outFile) {

        FileReader inFile;
        try {
            inFile = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(inFile);
            String str = null;
            String firstOrderLogic = "";
            System.out.println(">> Write first-order-logic rules to file (prog.mln) ...");

            //Clean all the out content in 'outFile'
            FileWriter fw = new FileWriter(outFile);
            fw.write("");
            fw.close();

            while ((str = br.readLine()) != null && str.length() != 0) {
                firstOrderLogic = "1\t";//add闁靛棴鎷穌efault weight 婵烇綀顕ф慨鐐搭渶濡鍚囬柡澶婂暣閸ｇeight=1
                String[] line = str.split("=>");//闁告帒妫旂拹鐔煎灳濡搫鏂ч柛銉у灅eason闁炽儲鐟ラ幏浼村灳濡偐娉㈤柡瀣esult闁炽儲鐟ょ悮杈ㄧ▔椤忓牆鍔ラ柛鎺炴嫹

                String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                //reason
                for (int index = 0; index < reason.length; index++) {
                    if (reason[index].contains("=")) {
                        String[] current = reason[index].split("=");
                        setPredicate(current[0]);
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        firstOrderLogic += "!" + predicate + "(" + value + ") v ";
                    } else {
                        int value_index = 0;
                        for (int i = index; i < reason.length; i++) {
                            setPredicate(reason[i]);
                            setValue("x" + (value_index++));
                            firstOrderLogic += "!" + predicate + "(" + value + ") v ";
                        }
                        break;
                    }
                }

                //result
                for (int index = 0; index < result.length; index++) {
                    if (result[index].contains("=")) {
                        String[] current = result[index].split("=");
                        setPredicate(current[0]);
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        firstOrderLogic += predicate + "(" + value + ")";
                    } else {
                        int value_index = 0;
                        for (int i = index; i < result.length; i++) {
                            setPredicate(result[i]);
                            setValue("y" + (value_index++));
                            firstOrderLogic += predicate + "(" + value + ")";
                        }
                        break;
                    }
                }
                //System.out.println(firstOrderLogic+"\n");

                writeToFile(firstOrderLogic, outFile);
            }
            System.out.println(">> Writing Completed!");
            br.close();
            inFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    void setValue(String value) {
        this.value = value;
    }

    public static void writeToFile(String content, String outFile) {    //闁硅泛銈祇ntent闁汇劌瀚崬瀵革拷鍦攰閹风兘宕濋悩鎻掓櫢闁稿繈鍎查弸鍐╃閿燂拷
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile, true)));
            out.write(content + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * load Rules from files in disk
     * */
    public static HashMap<String, Double> loadRulesFromFile(String fileURL){
        FileReader reader;
        ArrayList<MLNClause> clauses = new ArrayList<MLNClause>();
        HashMap<String, Double> attributes = new HashMap<String, Double>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null ) {
                MLNClause mlnClause = new MLNClause();
                String weight = str.substring(0,str.indexOf(","));
                mlnClause.weight = Double.parseDouble(weight);
                str = str.substring(str.indexOf(",")+1);
                String[] line = str.split(" v ");
                for(String value: line){
                    value = value.replaceAll(" ","")
                            .replaceAll("\"","")
                            .replaceAll(".*\\(","")
                            .replaceAll("\\)","");
                    mlnClause.values.add(value);
                }
                clauses.add(mlnClause);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(MLNClause c : clauses){
            String[] tmp_value = new String[c.values.size()];
            for(int i=0;i<tmp_value.length;i++){
                tmp_value[i] = c.values.get(i);
            }
            Arrays.sort(tmp_value);
            attributes.put(Arrays.toString(tmp_value),c.weight);
        }

        return attributes;
    }

    /**
     * Init Rules
     */
    public void initRules(String fileURL) {
        //default fileURL = "E:\\eclipse_workspace\\DiscoverRules-v2.0\\rules.txt";
        FileReader reader;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {

                String[] line = str.split("=>");
                String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                //reason
                System.out.print("reason: ");
                for (int index = 0; index < reason.length; index++) {
                    if (reason[index].contains("=")) {
                        String[] current = reason[index].split("=");
                        setPredicate(current[0]);
                        System.out.print("P = " + predicate + ", ");
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        System.out.print("V = " + value + ".  ");
                    } else {
                        for (int i = index; i < reason.length; i++) {
                            setPredicate(reason[i]);
                            System.out.print("P = " + predicate + ", ");
                            System.out.print("V = any value.  ");
                        }
                        break;
                    }
                }
                System.out.println();

                //result
                System.out.print("result: ");
                for (int index = 0; index < result.length; index++) {
                    if (result[index].contains("=")) {
                        String[] current = result[index].split("=");
                        setPredicate(current[0]);
                        System.out.print("P = " + predicate + ", ");
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        System.out.print("V = " + value + ".  ");
                    } else {
                        for (int i = index; i < result.length; i++) {
                            setPredicate(result[i]);
                            System.out.print("P = " + predicate + ", ");
                            System.out.print("V = any value.  ");
                        }
                        break;
                    }
                }
                System.out.println();
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Init Data
     */


    public void initData(String fileURL, String splitString, boolean ifHeader) {//check if the data has header
        // read file content from file
        FileReader reader;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int index = 0; //tuple index
            if (ifHeader && (str = br.readLine()) != null) {  //The data has header

                header = str.split(splitString);

                while ((str = br.readLine()) != null) {
                    Tuple t = new Tuple();
                    t.init(str, splitString, index);//init the tuple,split with ","
                    tupleList.add(t);
                    index++;
                }
            } else { //如果没有header
                while ((str = br.readLine()) != null) {
                    Tuple t = new Tuple();
                    t.init(str, splitString, index);//init the tuple,split with ","
                    tupleList.add(t);
                    index++;
                }
                int length = tupleList.get(0).getContext().length;
                header = new String[length];
                char c = 64;
                for (int i = 1; i <= length; i++) {
                    c += 1;
                    header[i - 1] = String.valueOf(c);
                }
            }
            br.close();
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void resample(ArrayList<Tuple> newTupleList,int sanpleSize){

        ArrayList<Tuple> newList =new ArrayList<Tuple>();
        int totalSize = tupleList.size();
        for(int i=0;i<sanpleSize;i++){
            int index = (int)(Math.random()*totalSize);
            newList.add(tupleList.get(index));
        }
       tupleList = newList;
    }
}