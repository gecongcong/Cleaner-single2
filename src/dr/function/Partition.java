package dr.function;

import java.util.ArrayList;
import java.util.Arrays;

import dr.data.Class;
import dr.data.ClassSet;
import dr.data.DataSet;
import dr.data.Tuple;

public class Partition {

	private String[] pre_value = null; // attribute��ֵ
	private String[] behind_value = null;
	private Boolean[] key = null; // key��ʾ�Ƿ��ѹ��࣬���Ϊfalse,�����δ���࣬true�����ѹ���

	public Partition() {
	}

	public Partition(DataSet dataSet) {
		int size = dataSet.getTupleList().size();
		key = new Boolean[size];
		for (int i = 0; i < size; i++)
			key[i] = false; // ��ʼ��Ϊfalse
	}

	public ClassSet partition1(DataSet dataSet, String[] attributes, ArrayList<ClassSet> clsSetList) {
		// ����,ֵͬ��Ϊһ��,attrIndex���������е����
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
		ans.setX(attributes); // attributesΪ���ֵ�����
		for (int i = 0; i < classSet.getClassList().size(); i++) {
			Class cls = classSet.getClassList().get(i);
			Boolean[] flags1 = new Boolean[cls.getSize()];
			for (int j = 0; j < flags1.length; j++)
				flags1[j] = false; // ��ʼ��Ϊfalse
			for (int j = 0; j < cls.getSize(); j++) {
				Class cls1 = new Class();
				ArrayList<Integer> tupleIndexList = new ArrayList<Integer>();
				String index1 = tupleList.get(cls.getContent().get(j)).getContext()[index];
				boolean f = false;// �Ƿ�����µķ���
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
					cls1.setMiniTID(tupleIndexList.get(0)); // class����С��Tuple id
					cls1.setSize(tupleIndexList.size()); // class�ĳ���
					ans.addClass(cls1); // ����class����class Set��
					//System.out.println(tupleIndexList.get(0)+" "+tupleIndexList.size());
				}
			}
		}
		//ans.print();
		return ans;
	}

	public ClassSet partition(DataSet dataSet, String[] attributes, int[] attrIndex) {
		// ����,ֵͬ��Ϊһ��,attrIndex���������е����

		ClassSet classSet = new ClassSet();
		classSet.setX(attributes); // attributesΪ���ֵ�����

		ArrayList<Tuple> tupleList = dataSet.getTupleList();
		int size = tupleList.size(); // ���ݼ������������м���tuple

		for (int j = 0; j < size; j++) {
			Tuple pre_tuple = tupleList.get(j);

			// ��ȡҪ�Աȵ�values
			pre_value = findValues(pre_tuple, attrIndex);
			if (key[j])
				continue;
			// �����������Ա�
			Class cls = new Class();
			ArrayList<Integer> tupleIndexList = new ArrayList<Integer>();
			tupleIndexList.add(j);
			for (int i = j + 1; i < size; i++) { // ��valueδ����
				if (key[i])
					continue;
				Tuple behind_tuple = tupleList.get(i);
				behind_value = findValues(behind_tuple, attrIndex);

				if (checkSame(pre_value, behind_value)) { // �����ͬ�������class��
					tupleIndexList.add(i); // ����tuple�ı��,i,���뵽list��
					key[i] = true; // ��ʾ�ѹ���
				}
			}
			cls.setClassContent(tupleIndexList);
			cls.setMiniTID(tupleIndexList.get(0)); // class����С��Tuple id
			cls.setSize(tupleIndexList.size()); // class�ĳ���
			classSet.addClass(cls); // ����class����class Set��
		}
		//classSet.print();
		return classSet;
	}

	public String[] findValues(Tuple tuple, int[] attrIndex) {// ��ȡҪ�Աȵ�values
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
