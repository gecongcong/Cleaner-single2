package dr.function;

import java.util.ArrayList;
import java.util.Arrays;

import dr.data.Class;
import dr.data.ClassSet;
import dr.data.DataSet;
import dr.data.Tuple;

public class Partition {

	private String[] pre_value = null; // attribute的值
	private String[] behind_value = null;
	private Boolean[] key = null; // key表示是否已归类，如果为false,则代表未归类，true代表已归类

	public Partition() {
	}

	public Partition(DataSet dataSet) {
		int size = dataSet.getTupleList().size();
		key = new Boolean[size];
		for (int i = 0; i < size; i++)
			key[i] = false; // 初始化为false
	}

	public ClassSet partition1(DataSet dataSet, String[] attributes, ArrayList<ClassSet> clsSetList) {
		// 划分,同值的为一组,attrIndex代表属性列的序号
		ArrayList<Tuple> tupleList = dataSet.getTupleList();
		String[] a = new String[attributes.length - 1];
		int index = -1;
		for (int i = 0; i < dataSet.getHeader().length; i++)
			if (dataSet.getHeader()[i].equals(attributes[attributes.length - 1])) {
				index = i;
				break;
			}
		for (int i = 0; i < attributes.length - 1; i++)
			a[i] = attributes[i];
		ClassSet classSet = new ClassSet();
		ClassSet ans = new ClassSet();
		for (int i = 0; i < clsSetList.size(); i++) {
			if (Arrays.equals(clsSetList.get(i).getX(), a)) {
				classSet = clsSetList.get(i);
				break;
			}
		}
		ans.setX(attributes); // attributes为划分的依据
		for (int i = 0; i < classSet.getClassList().size(); i++) {
			Class cls = classSet.getClassList().get(i);
			Boolean[] flags1 = new Boolean[cls.getSize()];
			for (int j = 0; j < flags1.length; j++)
				flags1[j] = false; // 初始化为false
			for (int j = 0; j < cls.getSize(); j++) {
				Class cls1 = new Class();
				ArrayList<Integer> tupleIndexList = new ArrayList<Integer>();
				String index1 = tupleList.get(cls.getContent().get(j)).getContext()[index];
				boolean f = false;// 是否存在新的分类
				for (int k = j; k < cls.getSize(); k++) {
					if (flags1[k])
						continue;
					if (tupleList.get(cls.getContent().get(k)).getContext()[index].equals(index1)) {
						flags1[k] = true;
						tupleIndexList.add(cls.getContent().get(k));
						f = true;

					}
				}
				if (f) {
					cls1.setClassContent(tupleIndexList);
					cls1.setMiniTID(tupleIndexList.get(0)); // class中最小的Tuple id
					cls1.setSize(tupleIndexList.size()); // class的长度
					ans.addClass(cls1); // 将该class加入class Set中
					//System.out.println(tupleIndexList.get(0)+" "+tupleIndexList.size());
				}
			}
		}
		//ans.print();
		return ans;
	}

	public ClassSet partition(DataSet dataSet, String[] attributes, int[] attrIndex) {
		// 划分,同值的为一组,attrIndex代表属性列的序号

		ClassSet classSet = new ClassSet();
		classSet.setX(attributes); // attributes为划分的依据

		ArrayList<Tuple> tupleList = dataSet.getTupleList();
		int size = tupleList.size(); // 数据集的行数，即有几条tuple

		for (int j = 0; j < size; j++) {
			Tuple pre_tuple = tupleList.get(j);

			// 获取要对比的values
			pre_value = findValues(pre_tuple, attrIndex);
			if (key[j])
				continue;
			// 与其他的做对比
			Class cls = new Class();
			ArrayList<Integer> tupleIndexList = new ArrayList<Integer>();
			tupleIndexList.add(j);
			for (int i = j + 1; i < size; i++) { // 该value未归类
				if (key[i])
					continue;
				Tuple behind_tuple = tupleList.get(i);
				behind_value = findValues(behind_tuple, attrIndex);

				if (checkSame(pre_value, behind_value)) { // 如果相同，则加入class中
					tupleIndexList.add(i); // 将该tuple的编号,i,加入到list中
					key[i] = true; // 表示已归类
				}
			}
			cls.setClassContent(tupleIndexList);
			cls.setMiniTID(tupleIndexList.get(0)); // class中最小的Tuple id
			cls.setSize(tupleIndexList.size()); // class的长度
			classSet.addClass(cls); // 将该class加入class Set中
		}
		//classSet.print();
		return classSet;
	}

	public String[] findValues(Tuple tuple, int[] attrIndex) {// 获取要对比的values
		String[] values = new String[attrIndex.length];
		for (int index = 0; index < attrIndex.length; index++) {
			values[index] = tuple.getContext()[attrIndex[index]];
		}
		return values;
	}

	public boolean checkSame(String[] a, String[] b) {
		if (Arrays.equals(a, b)) {
			// System.out.println("same");
			return true;
		} else {
			// System.out.println("different");
			return false;
		}
	}

}
