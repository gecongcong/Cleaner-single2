package dr.data;

import java.util.ArrayList;
import java.util.Arrays;

import dr.util.*;

public class Graph {
	
	ArrayList<Node> graph = new ArrayList<Node>();
	private int YH[][]=null;

	public ArrayList<Node> getGraph() {
		return graph;
	}

	public void setGraph(ArrayList<Node> graph) {
		this.graph = graph;
	}

	public class Node{
		//该节点的内容，比如∏[CLS]={[0, 1],[2, 4, 6, 10],[3, 9],[5],[7],[8]}
		public ClassSet data = new ClassSet();	
		//储存边的list
		//public ArrayList<ClassSet> edgeList = new ArrayList<ClassSet>();
		public ArrayList<Integer> edges=new ArrayList<Integer>();
	};
	
	public Graph(){}
	
	public void createGraph(ArrayList<ClassSet> clsSetList, int N){
		YH=Util.YHTri(N);
		for(int i=0;i<clsSetList.size();i++){
			Node node = new Node();
			ClassSet classSet = clsSetList.get(i);
			node.data = classSet;					//添加data
			findEdges(node,classSet,clsSetList,N);	//添加边
			graph.add(node);
		}
		//System.out.println(clsSetList.size());
		//print();
	}
	
	public void findEdges(Node node, ClassSet classSet, ArrayList<ClassSet> clsSetList, int N){ //n代表header的个数，即要排列组合的元素的个数
		int X_size = classSet.getX().length;	//看这个classSet里包含几个元素，即level-i
		int length = 0;
		for(int k=0;k<X_size;k++){
			length += YH[N][k+1];
		}
		//Arrays.sort(classSet.getX()); //对字符串数据进行排序，方便比较
		String[] pre_X = classSet.getX();
		int END =  YH[N][X_size+1];
		int count = 0;
		//System.out.println("N-X_size = "+(N-X_size));
		for(int i=length;count<N-X_size && i<length+END;i++){
			//Arrays.sort(clsSetList.get(i).getX()); //对字符串数据进行排序，方便比较
			//System.out.println("count = "+count);  //test
			String[] current_X = clsSetList.get(i).getX();
			
			if(Util.ifContain(pre_X,current_X)){	//比较level i中的节点是否包含于level i+1的节点，即两者之间存在边
				//node.edgeList.add(clsSetList.get(i));
				node.edges.add(i);
				count++;
			}
			
		}
	}
	
	public int[][] getYH() {
		return YH;
	}

	public void setYH(int[][] yH) {
		YH = yH;
	}
	
	public void print(){
		System.out.println("\n--------------------节点与边--------------------\n");
		for(int i=0;i<graph.size();i++){
			Node node = graph.get(i);
			System.out.println("Node"+i+": "+Arrays.toString(node.data.getX()));//输出格式，例如∏[CLS, MR]
			//ArrayList<ClassSet> list = node.edgeList;
			ArrayList<Integer> nums=node.edges;
			System.out.print("Edges: ");
/*			for(int j=0;j<list.size();j++){
				ClassSet clsSet = list.get(j);
				System.out.print(Arrays.toString(clsSet.getX())+" ");
			}*/
			System.out.println();
			
			for(int j=0;j<nums.size();j++){
				System.out.print(nums.get(j)+" ");
			}
			System.out.println();
		}
	}
	
//	public static void main(String[] args){
//		
//	}
	
}
