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

	String[] Q = null; // 条件属性 conditional attribute
	String[] P = null; // 变量属性 variable attribute
	String[] A = null; // [Q,P]->A
	ClassSet X = null; // edge(X,Y)中的X
	ClassSet Y = null; // edge(X,Y)中的Y, Y=(X∪A)
	double θ = 0.05; // 阈值θ
	String CL = new String(); // 结果规则集
	int l = 1; // 话分类最小长度
	double N;// tuple数量
	ArrayList<Integer> Qc;// 标识该结点的属性能否作为条件属性

	public Rule() {
	}

	public void removeSuperSet(String[] x, String[] y, int num, ArrayList<Node> nodes) {//删除超集
		//
		for (int i = num; i < nodes.size(); i++) {
			// System.out.println("判断"+Arrays.toString(nodes.get(i).data.getX())+"是否包含"+Arrays.toString(x));

			if (Util.ifSubsumed(nodes.get(i).data.getX(), y))// 判断是否包含Y，使得minimal
			{
				nodes.get(i).edges.removeAll(nodes.get(i).edges);
			} else if (Util.ifSubsumed(nodes.get(i).data.getX(), x))// 判断是否包含X
			{
				String xx[] = Util.substractAttribute(nodes.get(i).data.getX(), x);// 求出超集
				// System.out.println("包含,相减得"+Arrays.toString(xx));
				for (int j = 0; j < nodes.get(i).edges.size(); j++) {
					// System.out.println("判断"+Arrays.toString(nodes.get(nodes.get(i).edges.get(j)).data.getX())+"是否包含"+Arrays.toString(y));
					if (Util.ifSubsumed(nodes.get(nodes.get(i).edges.get(j)).data.getX(), y))// 判断是否包含Y
					{
						String yy[] = Util.substractAttribute(nodes.get(nodes.get(i).edges.get(j)).data.getX(), y);
						// System.out.println("包含,相减得"+Arrays.toString(yy));
						if (Arrays.equals(xx, yy)) {
							// System.out.println("删除"+Arrays.toString(nodes.get(i).data.getX())+"和"+Arrays.toString(nodes.get(i).edgeList.get(j).getX()));
							//A = Util.substractAttribute(nodes.get(nodes.get(i).edges.get(j)).data.getX(),nodes.get(i).data.getX());
							//addFD(nodes.get(i).data.getX(), A[0]);
							nodes.get(i).edges.remove(nodes.get(i).edges.get(j));// 删除相关的边
							break;
						}
					}

				}
			}

		}

	}

	public ArrayList<Integer> initQ(ArrayList<Node> nodes) {//删除低于阈值的条件属性取值
		ArrayList<Integer> ans = new ArrayList<Integer>();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < nodes.get(i).data.getClassList().size(); j++)
			if (nodes.get(i).data.getClassList().get(j).getSize() / N >= θ) {
					// System.out.println(nodes.get(i).data.getClassList().get(j).getContent().toString());
					ans.add(i);
					break;
				}
		}	
		return ans;
	}

	public String findRules(Graph graph, DataSet dataSet, int num) { // num 属性数目
		ArrayList<Node> nodes = graph.getGraph();// 获取G
		int YH[][] = graph.getYH();// 计算杨辉三角
		int firstofLevel[] = Util.getLevelFirst(YH, num);
		N = dataSet.getTupleList().size();
		Qc = initQ(nodes);
		System.out.print("Qc:");
		for (int k1 = 0; k1 < Qc.size(); k1++) {
			System.out.print(Qc.get(k1) + " ");
		}
		System.out.println("");
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println("寻找结点" + i + "的规则");
			Node node = nodes.get(i);// 获取当前结点
			X = node.data;
			// ArrayList<ClassSet> Edge = node.edgeList;// 获取边
			ArrayList<Integer> edges = node.edges;
			for (int j = 0; j < edges.size(); j++) {
				// Y = Edge.get(j);
				Y = nodes.get(edges.get(j)).data;
				if (X.getClassList().size() == Y.getClassList().size())// 判断是否属于FD
				{
					 System.out.println(Arrays.toString(X.getX())+"和"+Arrays.toString(Y.getX())+"是函数依赖关系");
					int first = firstofLevel[Y.getX().length - 1];// 计算属性数目，得到所在层的一个书node编号,level[x]对应level
					A = Util.substractAttribute(Y.getX(), X.getX());
					addFD(X.getX(), A[0]); // x-1
					removeSuperSet(X.getX(), Y.getX(), first, nodes);
				} else {
					ArrayList<Class> ΩX = findΩX(X, Y);// 获取ΩX
					if (ΩX.size() != 0) {
						
						 /*System.out.print(Arrays.toString(X.getX()) + "->" + Arrays.toString(Y.getX()) + ":"); 
						 for (int k1 = 0; k1< ΩX.size() - 1; k1++) 
						 {
						 System.out.print(ΩX.get(k1).getContent().toString() +" "); 
						 } 
						 System.out.println(ΩX.get(ΩX.size() - 1).getContent().toString());*/

						findCFD(ΩX, X, Y, nodes, dataSet, firstofLevel, YH);
					}
				}

			}
			// System.out.println("");
		}
		return CL;
	}

	public ArrayList<Class> findΩX(ClassSet X, ClassSet Y)// 从X和Y中获得ΩX
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
		 * System.out.print("条件Q的可能情况:"); for (int k1 = 0; k1 < ans.size();
		 * k1++) { System.out.print(Arrays.toString(ans.get(k1).getX()) + " ");
		 * } System.out.println("");
		 */
		return ans;
	}

	public void findCFD(ArrayList<Class> ΩX, ClassSet x, ClassSet y, ArrayList<Node> nodes, DataSet dataset,
			int[] firstofLevel, int YH[][]) {
		// System.out.println("进入findCFD");
		A = Util.substractAttribute(y.getX(), x.getX());
		ArrayList<ClassSet> cls = findQ(x, nodes);//找到所有的Q
		for (int j = 0; j < cls.size(); j++) {// every class
			Q = cls.get(j).getX();
			P = Util.substractAttribute(x.getX(), Q);
			 //System.out.println("Q" + j + ":为" + Arrays.toString(Q));
			ArrayList<Class> ΩX1 = (ArrayList<Class>) ΩX.clone();
			 //System.out.println("ΩX1初始的大小" + ΩX1.size());
			for (int qi = 0; qi < cls.get(j).getClassList().size(); qi++) {
				if (cls.get(j).getClassList().get(qi).getSize() > l&&cls.get(j).getClassList().get(qi).getSize()/N>=θ) {// 排除长度等于1和数量不够满足阈值的qi
					 //System.out.print("q" + qi + "的划分集合");
					 //System.out.println((cls.get(j).getClassList().get(qi).getContent().toString()));
					 //System.out.println("q" + qi + "的大小" +(cls.get(j).getClassList().get(qi).getSize()));
					 //System.out.println("ΩX1的大小" + ΩX1.size());
					ArrayList<Integer> index = checkQ(ΩX1, cls.get(j).getClassList().get(qi));
					 //System.out.println("此时index的大小" + index.size());
					double S = support(y, index);// 判断比较
					if (S >= θ) {
						String ss[] = dataset.getHeader();// 得到属性
						ArrayList<String> ansq = new ArrayList<String>();
						List<String> pp = new ArrayList<String>();
						String ansa = null;
						if (index.size() == 1) {// 无变量属性
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

						} else if (index.size() > 1) {// 有变量属性
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
					ΩX1.removeIf(c -> (index.contains(c.getMiniTID())));
					/* System.out.println("ΩX1的大小："+ΩX1.size()); */
					if (ΩX1.size() == 0)
						break;
				}
			}
		}

	}

	public void addRule(String q[], List<String> p, String a, ArrayList<String> ans, String ans2) {// q：条件属性
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
		// miniID代表符合条件的最小元组id的集合
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
		// System.out.println("阈值s:" + s);
		return s;
	}

	// check for a qi in ∏Q that contains only tuple values from ΩX. 核对
	// ∏Q中的元组是否仅来自于ΩX
	public ArrayList<Integer> checkQ(ArrayList<Class> ΩX, Class Q) {
		boolean key = false;
		ArrayList<Integer> index = new ArrayList<Integer>();
		ArrayList<Integer> pre_class = (ArrayList<Integer>) Q.getContent().clone();
		int pre_miniID = pre_class.get(0); // 找到∏Q的class里最小的那个tuple ID

		for (int i = 0; i < ΩX.size(); i++) {
			ArrayList<Integer> curr_class = (ArrayList<Integer>) ΩX.get(i).getContent().clone();
			int curr_miniID = curr_class.get(0); // 找到ΩX的class里最小的那个tuple ID
			if (pre_miniID == curr_miniID) { // 两个最小的那个tuple ID相等
				if (pre_class.containsAll(curr_class)) { // ΩXi包含于∏Q
					index.add(curr_miniID);
					pre_class.removeAll(curr_class); // 移除交集，继续搜索，直至pre_class(即∏Q')为空
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
