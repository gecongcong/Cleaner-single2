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
import jdk.nashorn.internal.ir.annotations.Ignore;

public class Domain {

	public static String baseURL = "E:\\experiment\\";


	public HashMap<Integer,String[]> dataSet = new HashMap<Integer,String[]>();


	//	public List<String[]> dataSet = new ArrayList<String[]>();
	public List<HashMap<Integer, Tuple>> domains = null;
	//	public List<HashMap<Integer, Tuple>> groups = null;
	public List<List<HashMap<Integer, Tuple>>> Domain_to_Groups = null;	//List<groups> 存放group by key后，Domain中包含的group的编号

	public HashMap<Integer,Conflicts> conflicts = new HashMap<Integer,Conflicts>();	//记录冲突的元组，并按Domain分类 <DomainID, ConflictTuple>

	//属性列，如果数据集中没有给出，则构造一个属性列：Attr1,Attr2,Attr3,...,AttrN
	public String[] header = null;

	public Domain(){}

	// 这个函数的作用是创造 mln 文件。
	public void createMLN(String[] header, String rulesURL){
		File file = new File(rulesURL);
		BufferedReader reader = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null && line.length()!=0) {
				lines.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

		String mlnURL = baseURL+"dataSet\\HAI\\prog.mln";//prog.mln;
		String content = null;
		try {
			//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(mlnURL, false);
			for(int i=0;i<header.length;i++){
				content = header[i]+"(value"+header[i]+")\n";
				writer.write(content);
			}
			writer.write("\n");
			for(int i=0;i<lines.size();i++){
				writer.write("1\t"+lines.get(i)+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}





	/**
	 * 按rules对数据集进行纵向划分 Partition DataSet into Domains
	 * @param fileURL
	 * @param splitString
	 * @param ifHeader
	 * @param rules
	 * */


	public void init(String fileURL,String splitString,boolean ifHeader, List<Tuple> rules){
		// read file content from file 读取文件内容
		FileReader reader;
		int rules_size = rules.size();
		domains = new ArrayList<HashMap<Integer, Tuple>>(rules_size);
		for(int i=0;i<rules_size;i++){
			domains.add(new HashMap<Integer, Tuple>());
		}
		try{
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			int key = 0; //tuple index

			if(ifHeader && (str = br.readLine()) != null){  //The data has header
				//   	header=str.split(splitString);
				while((str = br.readLine()) != null) {
				//	System.out.println(str);
					//dataSet.add(str.split(splitString));

					String[] tuple = str.split(splitString);
					dataSet.put(key, tuple);

					//为每一条rule划分数据集区域Di
					for(int i=0;i<rules_size;i++){
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

						for(int j=0; j<IDs.length; j++){
							tupleContext[j] = tuple[IDs[j]];//放入属于该区域的tuple内容
							tupleContextID[j] = IDs[j]; 	//放入对应的ID
							if(ifReason(IDs[j],reasonIDs)){	//如果是reason,则放入reasonContent
								reasonContent[reason_index++] = tuple[IDs[j]];
							}else{
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

						//区域Di划分完毕,放入hashMap
						domains.get(i%rules_size).put(key,t); //<K,V>  K = tuple ID , from 0 to n
					}
					key++;

				}
			}else{
				int length = 0;
//	        	boolean flag = false;
				while((str = br.readLine()) != null) {
					//dataSet.add(str.split(splitString));
					String[] tuple = str.split(splitString);

					dataSet.put(key, tuple);

					for(int i=0;i<rules_size;i++){	//为每一条rule划分数据集区域Di
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
						for(int j=0; j<IDs.length; j++){
							tupleContext[j] = tuple[IDs[j]];//放入属于该区域的tuple内容
							tupleContextID[j] = IDs[j]; 	//放入对应的ID
							if(ifReason(IDs[j],reasonIDs)){	//如果是reason,则放入reasonContent
								reasonContent[reason_index++] = tuple[IDs[j]];
							}else{
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
						//一个 t 是 一个 区域
						//区域Di划分完毕,放入hashMap
						domains.get(i%rules_size).put(key,t); //<K,V>  K = tuple ID , from 0 to n
					}
					key++;
				}
				System.out.println("header length = "+length);
			}

			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean ifContains(int[] IDs, int[] bigIDs){
		boolean result = false;
		int count = 0;
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>(bigIDs.length);
		for(int i=0;i<bigIDs.length;i++){
			map.put(bigIDs[i], 1);
		}
		int i = 0;
		for(;i<IDs.length && IDs[i]!=-1;i++){
			Integer value = map.get(IDs[i]);
			if(null!=value){
				count++;
			}
		}
		if(count==i)result = true;
		return result;
	}

	public static boolean ifReason(int[] IDs, int[] reasonIDs){
		boolean result = false;
		int count = 0;
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>(reasonIDs.length);
		for(int i=0;i<reasonIDs.length;i++){
			map.put(reasonIDs[i], 1);
		}
		for(int i=0;i<IDs.length;i++){
			Integer value = map.get(IDs[i]);
			if(null!=value){
				count++;
			}
		}
		if(count==IDs.length)result = true;
		return result;
	}


	public static boolean ifReason(int index, int[] reasonIDs){
		boolean result = false;
		int[] copy_reasonIDs = Arrays.copyOfRange(reasonIDs, 0, reasonIDs.length);
		Arrays.sort(copy_reasonIDs);
		for(int i=0;i<copy_reasonIDs.length;i++){
			if(index == copy_reasonIDs[i]){
				result = true;
				break;
			}
		}
		return result;
	}


	/**
	 * 二次划分，根据reason predicates 建立索引
	 * @param List<HashMap<Integer,List>> domains
	 * @param List<Tuple> rules
	 * */


	public void groupByKey(List<HashMap<Integer,Tuple>> domains, List<Tuple> rules){

		HashMap<Integer, Tuple> domain = null;
		int size = domains.size();
		Domain_to_Groups = new ArrayList< List<HashMap<Integer, Tuple>> >(size);

		for(int i=0;i<size;i++){

			domain = domains.get(i); // 一个
			int domain_size = domain.size(); //元组对数
			boolean[] flags = new boolean[domain_size];

			List<HashMap<Integer, Tuple>> groups = new ArrayList<HashMap<Integer, Tuple>>();

			for(int k=0;k<domain_size;k++){ //对区域中不同的元组根据 Key 重新 group
				Tuple tuple1 = domain.get(k); //一个 tuple
				HashMap<Integer, Tuple> group = new HashMap<Integer, Tuple>();
				group.put(k, tuple1);	//add Ti to group(i), now G(i)={Ti}
				String[] reason1 = tuple1.reason;
				//int index=0;

				if(!flags[k]){
					for(int m=k+1;m<domain_size;m++){
						if(flags[m])continue;
						Tuple tuple2 = domain.get(m);
						String[] reason2 = tuple2.reason;

						if(Arrays.equals(reason1,reason2)){
							group.put(m, tuple2);
							flags[m] = true;
						}

						//index++;
					}

					if(group.size()>1){
						groups.add(group);
					}
				}

			}

			if(groups.size()>0){
				Domain_to_Groups.add(groups);
			}


		}

		int d_index=0;
		for(List<HashMap<Integer, Tuple>> d: Domain_to_Groups){
			System.out.println("\n*******Domain "+(++d_index)+"*******");
			printGroup(d);
		}


	}





	/**
	 * 根据MLN的概率修正错误数据
	 * */


	public void correctByMLN(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<String, Double>> attributesPROBList, String[] header, List<HashMap<Integer, Tuple>> domains){
		int DGindex = 0;

		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups) {
			System.out.println("---------------"+(DGindex+1)+"--------------------");

			for (int i = 0; i < groups.size(); i++) {
				HashMap<Integer, Tuple> group = groups.get(i);

				HashMap<Integer,Integer> weight=new HashMap<Integer,Integer>();

				for (int t = 0; t < attributesPROBList.size(); t++) {
					HashMap<String, Double> attributesPROB = attributesPROBList.get(t);

					Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
					double pre_prob = 0.0;
					int tupleID = 0;
					//遍历group
					while (iter.hasNext()) {
						Entry<Integer, Tuple> current = iter.next();
						Tuple tuple = current.getValue();

						int length = tuple.getContext().length;
						String[] tmp_context = new String[length];
						System.arraycopy(tuple.getContext(), 0, tmp_context, 0, length);
						Arrays.sort(tmp_context);
						String values = Arrays.toString(tmp_context);
						//System.out.println(resultValues);
						if(values.indexOf("10085")!=-1){
							System.out.println("在这停顿！！！");
						}

						Double prob = attributesPROB.get(values);
						if (prob == null) {
							prob = 0.0;	//说明这个团只在数据集里出现过一次，姑且认为该团不具代表性，概率置0
						}
						if (pre_prob < prob) {
							pre_prob = prob;
							tupleID = current.getKey();
						}
					}
					if(weight.get(tupleID)==null)
						weight.put(tupleID,1);
					else{
						weight.put(tupleID,weight.get(tupleID)+1);
					}
				}
				// 如何找出作为基准的tupleID
                int resultNum=0;
				int resultTupleID=0;

				for(Map.Entry<Integer,Integer> entry: weight.entrySet()){
					int num = entry.getValue();
					if(num>resultNum){
						resultNum=num;
						resultTupleID= entry.getKey();
					}
				 }

				// tupleID 是被选择作为干净数据的，这里要用到
				Tuple cleanTuple = group.get(resultTupleID);
				Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
				//修正错误的值，即用正确的去替换它
				if (cleanTuple != null)
					while (iter.hasNext()) {
						Entry<Integer, Tuple> current = iter.next();
						group.put(current.getKey(), cleanTuple);
						domains.get(DGindex).put(current.getKey(), cleanTuple);
					}
			}
				DGindex++;
			}
/*
		//输出修正后的Group结果
		System.out.println("\n=======After Correct Values By MLN Probability=======");
		int d_index = 0;
		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			System.out.println("\n*******Domain "+(++d_index)+"*******");
			printGroup(groups);
		}
*/
	}
	/**
	 * 删除重复数据
	 * @param List<List<Integer>> keyList_list
	 * @param HashMap<Integer,String[]> dataSet
	 * */
	public void deleteDuplicate(List<List<Integer>> keyList_list, HashMap<Integer,String[]> dataSet){
//		System.out.println("\tDuplicate keys: ");
		for(List<Integer> keyList: keyList_list){
			if(keyList==null) continue;
			for(int i=0;i<keyList.size()-1;i++){
				int key1 = keyList.get(i);
				String[] pre_tuple = dataSet.get(key1);
//				System.out.print("\tGROUP "+i+":"+key1+" ");
				for(int j=i+1;j<keyList.size();j++){
					int key2 = keyList.get(j);
					String[] curr_tuple = dataSet.get(key2);
					if(Arrays.toString(pre_tuple).equals(Arrays.toString(curr_tuple))){ //之所以要比较是因为有些属性没有出现，可能不同
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
	 * 合并两个Domain中的tuple部分，它们原属于同一个Tuple
	 * @param Tuple t1 , Tuple t2
	 * */

	public Tuple combineTuple (Tuple t1 , Tuple t2, int[] sameID){

		Tuple t = new Tuple();
		// 分是否存在相同的属性
		if(sameID.length==0 || (sameID.length>0 && sameID[0]== -1)){ //不存在相同的属性
			int[] attributeIndex = concat(t1.getAttributeIndex(), t2.getAttributeIndex());
			String[] TupleContext = concat(t1.getContext(), t2.getContext());
			t.setContext(TupleContext);
			t.setAttributeIndex(attributeIndex);
		}else{ //存在相同的属性

			int[] t1IDs = t1.getAttributeIndex();
			int[] t2IDs = t2.getAttributeIndex();

			// 求得 sameIDlength 的长度
			int sameIDlength = 0;
			for(int j=0;j<sameID.length;j++){
				if(sameID[j]==-1)break;
				sameIDlength++;
			}


			// 新的 attributeindex
			int[] attributeIndex = Arrays.copyOf(t1IDs, t1IDs.length + t2IDs.length - sameIDlength);
			//新的tuplecontext
			String[] t1context = t1.getContext();
			String[] t2context = t2.getContext();
			String[] TupleContext = Arrays.copyOf(t1context, attributeIndex.length);

			int k = t1IDs.length;

			for(int i=0; i<t2IDs.length; i++){
				boolean flag = false;
				for(int j=0;j<t1IDs.length;j++){
					if(t2IDs[i]==t1IDs[j]){
						flag = true;
						break;
					}
				}
				if(flag)continue; // 对于当前 t2 元组的属性，t1 中有和他一样的属性
				attributeIndex[k] = t2IDs[i];
				TupleContext[k] = t2context[i];
				k++;
			}




//			for(int i=0; i<t2IDs.length; i++){
//				for(int ID: sameID){
//					if(ID==-1)break;//初始化为-1，即已遍历完有值的部分
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
	 * 找到两个数组的交集
	 * */
	public static int[] findSameID(int[] IDs1, int[] IDs2){
		int[] sameID = null;
		//初始化
		if(IDs1.length > IDs2.length)
			sameID = new int[IDs2.length];
		else
			sameID = new int[IDs1.length];
		for(int i=0;i<sameID.length;i++)
			sameID[i] = -1;

		//findSameID
		int[] newIDs1 = Arrays.copyOfRange(IDs1, 0, IDs1.length);
		int[] newIDs2 = Arrays.copyOfRange(IDs2, 0, IDs2.length);
		Arrays.sort(newIDs1);
		Arrays.sort(newIDs2);
		int i = 0, j = 0, index = 0;
		while(i<newIDs1.length && j<newIDs2.length){
			if(newIDs1[i] == newIDs2[j]){
				sameID[index++] = newIDs1[i];
				++i;
				++j;
			}else if(newIDs1[i] < newIDs2[j])
				++i;
			else
				++j;
		}
		return sameID;
	}

	/**
	 * 比较两个元组Tuple1和Tuple2中是否存在相同属性，但值不同
	 * */


	public static boolean checkConflict(Tuple t1, Tuple t2, int[] sameID){
		boolean result = false;


		String[] context1 = t1.getContext();
		String[] context2 = t2.getContext();

		int[] attributeID1 = t1.getAttributeIndex();
		int[] attributeID2 = t2.getAttributeIndex();


		HashMap<Integer, String> map1 = new HashMap<Integer, String>(context1.length);	// MAP<Attribute name,Attribute value>
		HashMap<Integer, String> map2 = new HashMap<Integer, String>(context2.length);



		// 把对应的值放到map 里

		for(int i=0;i<context1.length;i++)
			map1.put(attributeID1[i], context1[i]);


		for(int i=0;i<context2.length;i++)
			map2.put(attributeID2[i], context2[i]);



		Iterator<Entry<Integer, String>> iter = null;

		if(context1.length < context2.length){
			int i=0;
			iter = map1.entrySet().iterator();

			while(iter.hasNext()){
				Entry<Integer, String> entry = iter.next();
				int tupleID = entry.getKey(); // header 的索引

				String value1 = entry.getValue();
				String value2 = map2.get(tupleID);

				if(value2==null)continue; // 说明 tuple2 中没有该属性
				sameID[i++] = tupleID; // 记录相同的属性

				if(!value1.equals(value2)){//存在冲突
					result = true;
					break;
				}

			}

		}else{
			int i=0;
			iter = map2.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Integer, String> entry = iter.next();
				int tupleID = entry.getKey();
				String value1 = entry.getValue();
				String value2 = map1.get(tupleID);
				if(value2==null)continue;
				sameID[i++] = tupleID;
				if(!value1.equals(value2)){//存在冲突
					result = true;
					break;
				}
			}
		}
		return result;
	}




	public HashMap<Integer, Tuple> combineGroup(List<Integer> keyList, HashMap<Integer, Tuple> group1, HashMap<Integer, Tuple> group2, int Domain1, int Domain2){


		HashMap<Integer, Tuple> newGroup = new HashMap<Integer, Tuple>(group1.size()); // new group 的大小肯定不会超过其中任意一个group 的大小

		for(int ki=0;ki<keyList.size();){

			int key = keyList.get(ki);
			Tuple t1 = group1.get(key);
			Tuple t2 = group2.get(key);

			ConflictTuple ct1 = new ConflictTuple(t1);
			ConflictTuple ct2 = new ConflictTuple(t2);

			int[] sameID = new int[t1.getAttributeIndex().length];

			for(int i=0; i<sameID.length; i++){
				sameID[i] = -1;
			}
			if(!checkConflict(t1, t2, sameID)){ //不冲突
				newGroup.put(key, combineTuple(t1, t2, sameID));
				ki++;
			}else{

				System.out.println("Conflict:");
				System.out.println("\tcontext1 = "+Arrays.toString(group1.get(key).TupleContext));
				System.out.println("\tcontext2 = "+Arrays.toString(group2.get(key).TupleContext));
				// 把group1 和 group2 中的元组都去掉？
				group1.remove(key);
				group2.remove(key);
				System.out.println("\tKEY = "+key+" ki = "+ki);
//				for(int m = 0;m<keyList.size();m++){
//					System.out.println(keyList.get(m));
//				}

				// 把keylist 中的ki 对应的元组都去掉
				keyList.remove(ki);

				//根据DomainID将冲突的Tuple记录下来
				// 元组将冲突的属性记录下来
				ct1.setConflictIDs(sameID);
				ct2.setConflictIDs(sameID);
				// 记录所在的domain
				ct1.domainID = Domain1;
				ct2.domainID = Domain2;


				// 冲突的元组数

				addConflict(key, ct1);
				addConflict(key, ct2);

				// 每个元组都是冲突的，return null
				if(keyList.isEmpty()){
					return null;
				}

			}
		}
		return newGroup;
	}


	public void addConflict(int tupleID, ConflictTuple t){
		Conflicts oldct = conflicts.get(tupleID);
		if(null == oldct){ 	//该Domain中尚未添加冲突元组
			Conflicts newct = new Conflicts();
			newct.tuples.add(t);
			conflicts.put(tupleID, newct);
		}else{
			oldct.tuples.add(t);
		}
	}

	/**
	 * 根据key值求两个group的交集
	 * */
	public static List<Integer> interset(Integer[] a1,Integer[] a2){
		int len1 = a1.length;
		int len2 = a2.length;
		int len = a1.length+a2.length;
		Map<Integer,Integer> m = new HashMap<Integer,Integer>(len);

		List<Integer> ret = new ArrayList<Integer>();

		for(int i=0;i<len1;i++){
			if(m.get(a1[i])==null)
				m.put(a1[i], 1);
		}

		for(int i=0;i<len2;i++){
			if(m.get(a2[i])!=null)
				m.put(a2[i],2);
		}

		for(java.util.Map.Entry<Integer, Integer> e:m.entrySet()){
			if(e.getValue()==2){
				ret.add(e.getKey());
			}
		}

		return ret;
	}


	/**
	 * 返回该group的所有key值
	 * */
	public static Integer[] calculateKeys(HashMap<Integer, Tuple> group){
		Integer[] keys = new Integer[group.size()];
		Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
		int i = 0;
		while(iter.hasNext()){
			Entry<Integer, Tuple> entry = (Entry<Integer, Tuple>) iter.next();
			keys[i++] = entry.getKey();
		}
		return keys;
	}


	public boolean ifSameValue(int[] conflictIDs, Tuple t, ConflictTuple ct){
		boolean result = false;
		int count = 0;
		int i = 0;
		int ti = 0, cti = 0;
		for(; conflictIDs[i]!=-1; i++){
			while(ti<t.AttributeIndex.length){
				if(t.AttributeIndex[ti]==conflictIDs[i]){
					break;
				}else ti++;
			}
			while(cti<ct.AttributeIndex.length){
				if(ct.AttributeIndex[cti]==conflictIDs[i]){
					break;
				}else cti++;
			}
			if(cti==ct.AttributeIndex.length || ti==t.AttributeIndex.length)return false;
			if(t.TupleContext[ti].equals(ct.TupleContext[cti]))count++;
		}
		if(count == i)result = true;
		return result;
	}


	
	/**
	 * 为冲突元组找到候选的替换方案
	 * */

	public void findCandidate(HashMap<Integer,Conflicts> conflicts , List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<Integer, Tuple>> domains, List<HashMap<String, Double>> attributesPROBList, ArrayList<Integer> ignoredIDs){

		Tuple candidateTuple = new Tuple();
		//根据冲突元组，按不同的组合形成候选的修正方案


		Iterator<Entry<Integer,Conflicts>> conflict_iter = conflicts.entrySet().iterator();

		while(conflict_iter.hasNext()) {
			Boolean ischange =false;


			Entry<Integer, Conflicts> entry = conflict_iter.next();
			int tupleID = entry.getKey();
			Conflicts conf = entry.getValue();
			List<ConflictTuple> list = conf.tuples;

        HashMap<Tuple,Integer> weight = new HashMap<Tuple,Integer>();

        for(int tt =0;tt<attributesPROBList.size();tt++) {

        	HashMap<String, Double>  attributesPROB = attributesPROBList.get(tt);

			for (ConflictTuple ct : list) {

				int[] conflictIDs = ct.conflictIDs;
				int conflictDomainID = ct.domainID;
				int length = Domain_to_Groups.size();
				boolean[] flag = new boolean[length];
				Tuple combinedTuple = ct;
				//去匹配下一个区域的元组值
				for (int i = 0; i < length; i++) {
					if (flag[i] || i == conflictDomainID) continue;
					HashMap<Integer, Tuple> domain = domains.get(i);
					Tuple firstTuple = domain.entrySet().iterator().next().getValue();
					int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);
					if (sameID[0] != -1) { // 说明有交集的属性
						List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
						for (HashMap<Integer, Tuple> group : cur_groups) {
							Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
							while (iter.hasNext()) {
								Entry<Integer, Tuple> en = iter.next();
								Tuple t = en.getValue();
								if (ifContains(sameID, t.AttributeIndex) && ifSameValue(sameID, t, ct)) {
									flag[i] = true;
									combinedTuple = combineTuple(combinedTuple, t, sameID);

									System.err.println("1.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
									for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
										if (combinedTuple.TupleContext[a] == null) {
											System.err.println("debug here 1");
										}
									}
									break;
								}
							}
							if (flag[i]) break;
						}
					} else {
						combinedTuple = combineTuple(combinedTuple, domain.get(tupleID), sameID);
						System.err.println("2.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));

						for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
							if (combinedTuple.TupleContext[a] == null) {
								System.err.println("debug here 2");
							}
						}

						flag[i] = true;
					}
				}


				//若group中匹配不到，则去Domain中匹配
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

					//计算该tuple的probability
					String[] tmp_context = new String[content.length];
					System.arraycopy(content, 0, tmp_context, 0, content.length);
					Arrays.sort(tmp_context);
					String value = Arrays.toString(tmp_context);
					Double prob = attributesPROB.get(value);
					if(prob == null) prob = 0.0;
//					for (int j = 0; j < length; j++) {
//						String result = header[contentID[j]] + "(" + content[j] + ")";
//						Double tmpProb = attributesPROB.get(result);
//						if (tmpProb == null) continue;//表明该result的概率为1
//						prob *= tmpProb;
//					}
					if (prob > candidateTuple.probablity) {
						ischange = true;
						candidateTuple = combinedTuple;
						candidateTuple.probablity = prob;
					}

				}
			}
			if(weight.get(candidateTuple)==null)weight.put(candidateTuple,1);
			else weight.put(candidateTuple,weight.get(candidateTuple)+1);

		}
			int resultNum=0;
			Tuple resultTuple=null;
			for(Map.Entry<Tuple,Integer> entryy: weight.entrySet()){
				int num = entryy.getValue();
				if(num>resultNum){
					resultNum=num;
					resultTuple= entryy.getKey();
				}
			}
			candidateTuple = resultTuple;

			//修改dataset中这一条的数据
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


	public List<List<Integer>> combineDomain(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups){

		//只有一个Domain，不需要合并
		if(Domain_to_Groups.size() == 1)return null;
		List<HashMap<Integer, Tuple>> pre_groups = Domain_to_Groups.get(0);
		List<List<Integer>> keysList = new ArrayList<List<Integer>>(pre_groups.size());


		int preDomainID = 0;
		int curDomainID = 0;


		//第一个Domain中所有group的keyList
		//	for(HashMap<Integer, Tuple> group: pre_groups){
		//		keysList.add(Arrays.asList(calculateKeys(group)));
		//	}


//		for(List<Integer> list :keysList){
//			for(Integer output :list)
//				System.out.print(output);
//				System.out.println();
//		}



		for(int i=1;i<Domain_to_Groups.size();i++){

			curDomainID = i;
			List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
			int pre_groups_index = 0;
			int pre_groups_size = pre_groups.size();
			int cur_groups_index = 0;
			int cur_groups_size = cur_groups.size();
			List<HashMap<Integer, Tuple>> temp= new ArrayList<HashMap<Integer, Tuple>>();
			while(pre_groups_index < pre_groups_size && cur_groups_index < cur_groups_size){
				HashMap<Integer, Tuple> cur_group = cur_groups.get(cur_groups_index);
				HashMap<Integer, Tuple> pre_group = pre_groups.get(pre_groups_index);
				//求两个group的交集
				List<Integer> keyList = interset(calculateKeys(pre_group) , calculateKeys(cur_group));
				if(keyList.size()!=0){
					pre_group = combineGroup(keyList, pre_group, cur_group, preDomainID, curDomainID);
					if(keyList.size()>1) {
						keysList.add(keyList);
						temp.add(pre_group);
					}
				}
				cur_groups_index++;// 如果当前group与前一个 domain 的group没有交集，那么进行下一个group的匹配
				//如果当前domain 已经没有 可以用来 匹配的 group 的话 ，那么 就匹配前一个 domain 中的下一个group
				if(cur_groups_index == cur_groups_size){
					cur_groups_index=0;
					pre_groups_index++;
				}
			}
			pre_groups=temp;
			preDomainID = i;
		}

		//===========test==========
//		int d_index = 0;
//		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
//			System.out.println("\n*******Domain "+(++d_index)+"*******");
//			printGroup(groups);
//		}
		//===========test==========




		System.out.println(">>> Fix the error values...");
		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			for(HashMap<Integer, Tuple> group: groups){
				Iterator<Entry<Integer, Tuple>>iter = group.entrySet().iterator();
				//修正错误的值，即用正确的去替换它
				while(iter.hasNext()){
					Entry<Integer,Tuple> entry = iter.next();
					int currentKey = entry.getKey();
					Tuple cleanTuple = entry.getValue();

					int[] AttrIDs = cleanTuple.getAttributeIndex();
					String[] values = cleanTuple.getContext();
					//   String[] tuple = null;
					String[] tuple = dataSet.get(currentKey);
					for(int k=0;k<AttrIDs.length;k++){
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
	 * 标记重复数据
	 * */
	/*
	public List<List<Integer>> checkDuplicate(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups){

		List<List<Integer>> keyList_list = new ArrayList<List<Integer>>();
		int round = 0;

		if(Domain_to_Groups.size()<2){	//不存在重复数据
			return keyList_list;
		}

		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			if(round == 0){	//第一次循环,标记第一个Domain的groups中的重复数据
				for(HashMap<Integer, Tuple> group: groups){
					List<Integer> keyList = new ArrayList<Integer>();
					for (int key: group.keySet()) {
						//save keys
						keyList.add(key);
					}
					keyList_list.add(keyList); 	//第一个Domain中，有多少个group 就有多少个keyList_list
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
		//合并完所有区域
		return keyList_list;
	}

	*/

	/**
	 * 根据属性列的编号ID和当前遍历到的Tuple，获得对应的属性值values
	 * @param Tuple tuple
	 * @param String[] attributeIDs
	 * @return values
	 * */

	public static String[] getValues(Tuple t, int[] attributeIDs){
		int length = attributeIDs.length;
		String[] values = new String[length];
		for(int i=0;i<length;i++){
			values[i] = t.reason[attributeIDs[i]];
		}
		return values;
	}

	/**
	 * 打印划分后的所有domain
	 * @param domains
	 * */
	public void printDomainContent(List<HashMap<Integer, Tuple>> domains){
		HashMap<Integer, Tuple> domain = null;
		for(int i=0;i<domains.size();i++){
			domain = domains.get(i);
			Iterator iter = domain.entrySet().iterator();
			System.out.println("\n---------Domain "+(i+1)+"---------");

			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				Object key = entry.getKey();
				Tuple value = (Tuple)entry.getValue();
				System.out.println("key = "+key+" value = "+Arrays.toString(value.getContext()));
			}
		}

		//System.out.println("\nDomain.size = "+domains.size()+"\n");
		System.out.println("\n>>> Completed!");
	}

	/**
	 * 打印划分后的所有Group
	 * @param List<HashMap<Integer, List>> groups
	 * */
	public void printGroup(List<HashMap<Integer, Tuple>> groups){
		HashMap<Integer, Tuple> group = null;
		for(int i=0;i<groups.size();i++){
			group = groups.get(i);
			Iterator iter = group.entrySet().iterator();
			System.out.println("\n---------Group "+(i+1)+"---------");

			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				Object key = entry.getKey();
				Tuple value = (Tuple)entry.getValue();
				System.out.println("key = "+key+" value = "+Arrays.toString(value.getContext()));
			}
		}
//		System.out.println("\nGroup.size = "+groups.size()+"\n");
		System.out.println(">>> Completed!");
	}


	/**
	 * 打印整个数据集的内容
	 * @param HashMap<Integer, String[]> dataSet
	 * */
	public void printDataSet(HashMap<Integer, String[]> dataSet){
		System.out.println("\n==========DataSet Content==========");

		Iterator iter = dataSet.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			Object key = entry.getKey();
			String[] value = (String[])entry.getValue();
			System.out.println("key = "+key+" value = "+Arrays.toString(value));
		}
		System.out.println();
	}

	public void printConflicts(HashMap<Integer,Conflicts> conflicts){
		System.out.println("Conflict tuples:");
		Iterator<Entry<Integer,Conflicts>> iter = conflicts.entrySet().iterator();
		if(conflicts.size()==0){
			System.out.println("No Conflict tuples.");
			return;
		}
		while(iter.hasNext()){
			Entry<Integer,Conflicts> entry = (Entry<Integer,Conflicts>) iter.next();
			Object key = entry.getKey();
			Conflicts value = entry.getValue();
			List<ConflictTuple> tuples = value.tuples;
			System.out.print("Tuple ID = "+key+"\n"+"Content = ");
			for(Tuple t: tuples){
				System.out.print(Arrays.toString(t.getContext())+" ");
			}

		}
	}
}
