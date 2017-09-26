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
		//�ýڵ�����ݣ������[CLS]={[0, 1],[2, 4, 6, 10],[3, 9],[5],[7],[8]}
		public ClassSet data = new ClassSet();	
		//����ߵ�list
		//public ArrayList<ClassSet> edgeList = new ArrayList<ClassSet>();
		public ArrayList<Integer> edges=new ArrayList<Integer>();
	};
	
	public Graph(){}
	
	public void createGraph(ArrayList<ClassSet> clsSetList, int N){
		YH=Util.YHTri(N);
		for(int i=0;i<clsSetList.size();i++){
			Node node = new Node();
			ClassSet classSet = clsSetList.get(i);
			node.data = classSet;					//���data
			findEdges(node,classSet,clsSetList,N);	//��ӱ�
			graph.add(node);
		}
		//System.out.println(clsSetList.size());
		//print();
	}
	
	public void findEdges(Node node, ClassSet classSet, ArrayList<ClassSet> clsSetList, int N){ //n����header�ĸ�������Ҫ������ϵ�Ԫ�صĸ���
		int X_size = classSet.getX().length;	//�����classSet���������Ԫ�أ���level-i
		int length = 0;
		for(int k=0;k<X_size;k++){
			length += YH[N][k+1];
		}
		//Arrays.sort(classSet.getX()); //���ַ������ݽ������򣬷���Ƚ�
		String[] pre_X = classSet.getX();
		int END =  YH[N][X_size+1];
		int count = 0;
		//System.out.println("N-X_size = "+(N-X_size));
		for(int i=length;count<N-X_size && i<length+END;i++){
			//Arrays.sort(clsSetList.get(i).getX()); //���ַ������ݽ������򣬷���Ƚ�
			//System.out.println("count = "+count);  //test
			String[] current_X = clsSetList.get(i).getX();
			
			if(Util.ifContain(pre_X,current_X)){	//�Ƚ�level i�еĽڵ��Ƿ������level i+1�Ľڵ㣬������֮����ڱ�
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
		System.out.println("\n--------------------�ڵ����--------------------\n");
		for(int i=0;i<graph.size();i++){
			Node node = graph.get(i);
			System.out.println("Node"+i+": "+Arrays.toString(node.data.getX()));//�����ʽ�������[CLS, MR]
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
