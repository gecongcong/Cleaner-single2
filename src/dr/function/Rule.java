package dr.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dr.data.Class;
import dr.data.ClassSet;
import dr.data.DataSet;
import dr.data.Graph;
import dr.data.Graph.Node;
import dr.util.Util;

public class Rule {

	String[] Q = null; // �������� conditional attribute
	String[] P = null; // �������� variable attribute
	String[] A = null; // [Q,P]->A
	ClassSet X = null; // edge(X,Y)�е�X
	ClassSet Y = null; // edge(X,Y)�е�Y, Y=(X��A)
	double �� = 0.05; // ��ֵ��
	String CL = new String(); // �������
	int l = 1; // ��������С����
	double N;// tuple����
	ArrayList<Integer> Qc;// ��ʶ�ý��������ܷ���Ϊ��������

	public Rule() {
	}

	public void removeSuperSet(String[] x, String[] y, int num, ArrayList<Node> nodes) {//ɾ������
		//
		for (int i = num; i < nodes.size(); i++) {
			// System.out.println("�ж�"+Arrays.toString(nodes.get(i).data.getX())+"�Ƿ����"+Arrays.toString(x));

			if (Util.ifSubsumed(nodes.get(i).data.getX(), y))// �ж��Ƿ����Y��ʹ��minimal
			{
				nodes.get(i).edges.removeAll(nodes.get(i).edges);
			} else if (Util.ifSubsumed(nodes.get(i).data.getX(), x))// �ж��Ƿ����X
			{
				String xx[] = Util.substractAttribute(nodes.get(i).data.getX(), x);// �������
				// System.out.println("����,�����"+Arrays.toString(xx));
				for (int j = 0; j < nodes.get(i).edges.size(); j++) {
					// System.out.println("�ж�"+Arrays.toString(nodes.get(nodes.get(i).edges.get(j)).data.getX())+"�Ƿ����"+Arrays.toString(y));
					if (Util.ifSubsumed(nodes.get(nodes.get(i).edges.get(j)).data.getX(), y))// �ж��Ƿ����Y
					{
						String yy[] = Util.substractAttribute(nodes.get(nodes.get(i).edges.get(j)).data.getX(), y);
						// System.out.println("����,�����"+Arrays.toString(yy));
						if (Arrays.equals(xx, yy)) {
							// System.out.println("ɾ��"+Arrays.toString(nodes.get(i).data.getX())+"��"+Arrays.toString(nodes.get(i).edgeList.get(j).getX()));
							//A = Util.substractAttribute(nodes.get(nodes.get(i).edges.get(j)).data.getX(),nodes.get(i).data.getX());
							//addFD(nodes.get(i).data.getX(), A[0]);
							nodes.get(i).edges.remove(nodes.get(i).edges.get(j));// ɾ����صı�
							break;
						}
					}

				}
			}

		}

	}

	public ArrayList<Integer> initQ(ArrayList<Node> nodes) {//ɾ��������ֵ����������ȡֵ
		ArrayList<Integer> ans = new ArrayList<Integer>();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < nodes.get(i).data.getClassList().size(); j++)
			if (nodes.get(i).data.getClassList().get(j).getSize() / N >= ��) {
					// System.out.println(nodes.get(i).data.getClassList().get(j).getContent().toString());
					ans.add(i);
					break;
				}
		}	
		return ans;
	}

	public String findRules(Graph graph, DataSet dataSet, int num) { // num ������Ŀ
		ArrayList<Node> nodes = graph.getGraph();// ��ȡG
		int YH[][] = graph.getYH();// �����������
		int firstofLevel[] = Util.getLevelFirst(YH, num);
		N = dataSet.getTupleList().size();
		Qc = initQ(nodes);
		System.out.print("Qc:");
		for (int k1 = 0; k1 < Qc.size(); k1++) {
			System.out.print(Qc.get(k1) + " ");
		}
		System.out.println("");
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println("Ѱ�ҽ��" + i + "�Ĺ���");
			Node node = nodes.get(i);// ��ȡ��ǰ���
			X = node.data;
			// ArrayList<ClassSet> Edge = node.edgeList;// ��ȡ��
			ArrayList<Integer> edges = node.edges;
			for (int j = 0; j < edges.size(); j++) {
				// Y = Edge.get(j);
				Y = nodes.get(edges.get(j)).data;
				if (X.getClassList().size() == Y.getClassList().size())// �ж��Ƿ�����FD
				{
					 System.out.println(Arrays.toString(X.getX())+"��"+Arrays.toString(Y.getX())+"�Ǻ���������ϵ");
					int first = firstofLevel[Y.getX().length - 1];// ����������Ŀ���õ����ڲ��һ����node���,level[x]��Ӧlevel
					A = Util.substractAttribute(Y.getX(), X.getX());
					addFD(X.getX(), A[0]); // x-1
					removeSuperSet(X.getX(), Y.getX(), first, nodes);
				} else {
					ArrayList<Class> ��X = find��X(X, Y);// ��ȡ��X
					if (��X.size() != 0) {
						
						 /*System.out.print(Arrays.toString(X.getX()) + "->" + Arrays.toString(Y.getX()) + ":"); 
						 for (int k1 = 0; k1< ��X.size() - 1; k1++) 
						 {
						 System.out.print(��X.get(k1).getContent().toString() +" "); 
						 } 
						 System.out.println(��X.get(��X.size() - 1).getContent().toString());*/

						findCFD(��X, X, Y, nodes, dataSet, firstofLevel, YH);
					}
				}

			}
			// System.out.println("");
		}
		return CL;
	}

	public ArrayList<Class> find��X(ClassSet X, ClassSet Y)// ��X��Y�л�æ�X
	{

		ArrayList<Class> ans = new ArrayList<Class>();
		for (int i = 0; i < X.getClassList().size(); i++) {
			for (int j = 0; j < Y.getClassList().size(); j++) {
				if (X.getClassList().get(i).getContent().equals(Y.getClassList().get(j).getContent())&& X.getClassList().get(i).getSize() >= 2)
					ans.add(X.getClassList().get(i));
			}
		}
		return ans;

	}

	public ArrayList<ClassSet> findQ(ClassSet Q, ArrayList<Node> nodes)// num //
																		// level
	{
		ArrayList<ClassSet> ans = new ArrayList<ClassSet>();
		for (int i = 0; i < Qc.size(); i++) {
			if (Util.ifSubsumed(Q.getX(), nodes.get(Qc.get(i)).data.getX())) {
				ans.add(nodes.get(Qc.get(i)).data);
			}
		}
		/*
		 * System.out.print("����Q�Ŀ������:"); for (int k1 = 0; k1 < ans.size();
		 * k1++) { System.out.print(Arrays.toString(ans.get(k1).getX()) + " ");
		 * } System.out.println("");
		 */
		return ans;
	}

	public void findCFD(ArrayList<Class> ��X, ClassSet x, ClassSet y, ArrayList<Node> nodes, DataSet dataset,
			int[] firstofLevel, int YH[][]) {
		// System.out.println("����findCFD");
		A = Util.substractAttribute(y.getX(), x.getX());
		ArrayList<ClassSet> cls = findQ(x, nodes);//�ҵ����е�Q
		for (int j = 0; j < cls.size(); j++) {// every class
			Q = cls.get(j).getX();
			P = Util.substractAttribute(x.getX(), Q);
			 //System.out.println("Q" + j + ":Ϊ" + Arrays.toString(Q));
			ArrayList<Class> ��X1 = (ArrayList<Class>) ��X.clone();
			 //System.out.println("��X1��ʼ�Ĵ�С" + ��X1.size());
			for (int qi = 0; qi < cls.get(j).getClassList().size(); qi++) {
				if (cls.get(j).getClassList().get(qi).getSize() > l&&cls.get(j).getClassList().get(qi).getSize()/N>=��) {// �ų����ȵ���1����������������ֵ��qi
					 //System.out.print("q" + qi + "�Ļ��ּ���");
					 //System.out.println((cls.get(j).getClassList().get(qi).getContent().toString()));
					 //System.out.println("q" + qi + "�Ĵ�С" +(cls.get(j).getClassList().get(qi).getSize()));
					 //System.out.println("��X1�Ĵ�С" + ��X1.size());
					ArrayList<Integer> index = checkQ(��X1, cls.get(j).getClassList().get(qi));
					 //System.out.println("��ʱindex�Ĵ�С" + index.size());
					double S = support(y, index);// �жϱȽ�
					if (S >= ��) {
						String ss[] = dataset.getHeader();// �õ�����
						ArrayList<String> ansq = new ArrayList<String>();
						List<String> pp = new ArrayList<String>();
						String ansa = null;
						if (index.size() == 1) {// �ޱ�������
							for (int jj = 0; jj < Q.length; jj++) {
								for (int k = 0; k < ss.length; k++) {
									if (ss[k].equals(Q[jj])) {
										// System.out.println("x.getX()[0]"+x.getX()[0]);
										ansq.add(dataset.getTupleList().get(index.get(0)).getContext()[k]);
									} else if (ss[k].equals(A[0])) {
										// System.out.println("y.getX()[1]"+y.getX()[1]);
										ansa = dataset.getTupleList().get(index.get(0)).getContext()[k];
									}
								}
							}

						} else if (index.size() > 1) {// �б�������
							Collections.addAll(pp, P);
							for (int jj = 0; jj < Q.length; jj++) {
								for (int k = 0; k < ss.length; k++) {
									if (ss[k].equals(Q[jj])) {
										// System.out.println("x.getX()[0]" + x.getX()[0]);
										ansq.add(dataset.getTupleList().get(index.get(0)).getContext()[k]);
									}
								}
							}
						}
						addRule(Q, pp, A[0], ansq, ansa);
					}
					��X1.removeIf(c -> (index.contains(c.getMiniTID())));
					/* System.out.println("��X1�Ĵ�С��"+��X1.size()); */
					if (��X1.size() == 0)
						break;
				}
			}
		}

	}

	public void addRule(String q[], List<String> p, String a, ArrayList<String> ans, String ans2) {// q����������
		String rule = q[0] + "(" + ans.get(0) + ")";
		for (int i = 1; i < q.length; i++) {
			rule += " , " + q[i] + "(" + ans.get(i) + ")";
		}
		if (p.size() != 0)
			for (int i = 0; i < p.size(); i++) {
				rule += "," + p.get(i)+"(value"+p.get(i)+")";
			}
		rule += " => " + a;
		if (ans2 != null)
			rule += "(" + ans2 + ")";
		else rule += "(value"+a+")";
		rule += "\r\n";
		System.out.println("rule:" + rule);
		CL = CL + rule;
	}

	public void addFD(String q[], String a) {
		String rule = q[0] + "(value"+q[0]+")";
		for (int i = 1; i < q.length; i++) {
			rule += " , " + q[i] + "(value"+q[i]+")";
		}
		rule += " => " + a + "(value"+a+")";
		rule += "\r\n";
		System.out.println("rule:" + rule);
		CL = CL + rule;
	}

	public double support(ClassSet cls, ArrayList<Integer> index) { // interest
																	// measure
																	// for
		// CFDs ,
		// miniID���������������СԪ��id�ļ���
		double s = 0;
		double size = 0;
		ArrayList<Class> classList = cls.getClassList();
		// double N = 0; // the Number of tuples in R
		for (int i = 0; i < cls.getClassList().size(); i++) {
			// N += classList.get(i).getSize();
			for (int j = 0; j < index.size(); j++) {
				if (cls.getClassList().get(i).getMiniTID() == index.get(j))
					size += classList.get(i).getSize();
			}
		}
		s = size / N;
		// System.out.println("��ֵs:" + s);
		return s;
	}

	// check for a qi in ��Q that contains only tuple values from ��X. �˶�
	// ��Q�е�Ԫ���Ƿ�������ڦ�X
	public ArrayList<Integer> checkQ(ArrayList<Class> ��X, Class Q) {
		boolean key = false;
		ArrayList<Integer> index = new ArrayList<Integer>();
		ArrayList<Integer> pre_class = (ArrayList<Integer>) Q.getContent().clone();
		int pre_miniID = pre_class.get(0); // �ҵ���Q��class����С���Ǹ�tuple ID

		for (int i = 0; i < ��X.size(); i++) {
			ArrayList<Integer> curr_class = (ArrayList<Integer>) ��X.get(i).getContent().clone();
			int curr_miniID = curr_class.get(0); // �ҵ���X��class����С���Ǹ�tuple ID
			if (pre_miniID == curr_miniID) { // ������С���Ǹ�tuple ID���
				if (pre_class.containsAll(curr_class)) { // ��Xi�����ڡ�Q
					index.add(curr_miniID);
					pre_class.removeAll(curr_class); // �Ƴ�����������������ֱ��pre_class(����Q')Ϊ��
					if (pre_class.size() == 0) {
						key = true;
						break;
					}
					pre_miniID = pre_class.get(0);
				}
			}
		}
		if (key)
			return index;
		else {
			return new ArrayList<Integer>();
		}
	}

	/*
	 * public static void main(String[] args) throws Exception {
	 * 
	 * String ss[]={"qweq1","123","fwerw","3123","asd"}; String
	 * st[]={"123","asd","fwerw","qweq1"}; System.out.println(ss.length);
	 * String[] ans2=substractAttribute(ss,st); for(int i=0;i<ans2.length;i++)
	 * System.out.println(ans2[i]); }
	 */
}
