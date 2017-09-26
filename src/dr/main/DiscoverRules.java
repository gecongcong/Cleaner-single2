package dr.main;

import java.util.ArrayList;
import java.util.Arrays;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import dr.data.Class;
import dr.data.ClassSet;
import dr.data.DataSet;
import dr.data.Graph;
import dr.function.Partition;
import dr.function.Rule;
import dr.util.Util;

public class DiscoverRules {
	public DiscoverRules(){};
	
	public String start(String fileURL){
		
		String splitString = ",";//	����֮��ķָ����������Զ��ŷָ� �� �ո� �ָ���
		String currentDIR = System.getProperty("user.dir");//��õ�ǰ����·��
		
//		String fileURL = currentDIR + "\\dataSet\\"+ "synthetic-car\\ground_truth.csv";
//		String fileURL = currentDIR + "\\dataSet\\"+ "test-city.data";
//	    String fileURL = currentDIR + "\\dataSet\\"+ "car evaluation-new\\car.data";
		//String fileURL = "E:\\gcc\\ground_truth.txt";
		//String fileURL = "E:\\gcc\\DiscoverRules\\dataSet\\cars_com\\cars_com.txt";
		boolean ifHeader = true;	//If the dataSet has header or not ���ݼ��Ƿ�����������һ��
		
		DataSet dataSet = new DataSet(); 
		dataSet.init(fileURL,splitString,ifHeader); //import the dataSet,and format �������ݼ�����ʽ��
		ArrayList<String[]> searchLattice = dataSet.getSearchLattice();//�γɲ�ѯ����
		
		System.out.println("---------Generate Attribute Search Lattice ������ѯ����---------\n");
		System.out.println("searchLattice.size = "+searchLattice.size()+"\n");
		
		ArrayList<ClassSet> clsSetList = new ArrayList<ClassSet>();
		
		String[] header = dataSet.getHeader();
		int i;
		for(i=0;i<header.length;i++){ //�ò�����Ϊ�˻�� attributes �� attrIndex ����
			String[] attributes= searchLattice.get(i);
			
			//System.out.println(Arrays.toString(attributes));//���ԣ���ӡ��ѯ���� Attribute Search Lattice
			
			Partition partition = new Partition(dataSet);
			int[] attrIndex = new int[attributes.length];
			
			for(int j=0;j<attributes.length;j++){	//��attrIndex��ֵ�����ҵ�Attribute��Ӧ�����
				for(int k=0;k<header.length;k++){
					if(attributes[j].equals(header[k])){
						attrIndex[j] = k;
						break;
					}
				}
			}
			
			//ִ�л��ֲ���
			ClassSet clsSet = partition.partition(dataSet, attributes, attrIndex);
			clsSetList.add(clsSet);
		}
		
		
		for(;i<searchLattice.size();i++){ 
			String[] attributes= searchLattice.get(i);	
			//System.out.println(Arrays.toString(attributes));//���ԣ���ӡ��ѯ���� Attribute Search Lattice
			Partition partition = new Partition(dataSet);
			//ִ�л��ֲ���
			ClassSet clsSet = partition.partition1(dataSet, attributes,clsSetList);
			clsSet.sort();
			clsSetList.add(clsSet);
		}	
		
/*		ArrayList<ClassSet> clsSetList1 = new ArrayList<ClassSet>();
		for(i=0;i<searchLattice.size();i++){ //�ò�����Ϊ�˻�� attributes �� attrIndex ����
			String[] attributes= searchLattice.get(i);
			
			//System.out.println(Arrays.toString(attributes));//���ԣ���ӡ��ѯ���� Attribute Search Lattice
			
			Partition partition = new Partition(dataSet);
			int[] attrIndex = new int[attributes.length];
			
			for(int j=0;j<attributes.length;j++){	//��attrIndex��ֵ�����ҵ�Attribute��Ӧ�����
				for(int k=0;k<header.length;k++){
					if(attributes[j].equals(header[k])){
						attrIndex[j] = k;
						break;
					}
				}
			}
			
			//ִ�л��ֲ���
			ClassSet clsSet = partition.partition(dataSet, attributes, attrIndex);
			clsSetList1.add(clsSet);
		}
		
		boolean ss=true;
		if(clsSetList1.size()==clsSetList.size())
		{
			System.out.println("������ͬ��");
			ArrayList<Class> classes1 = new ArrayList<Class>();
			ArrayList<Class> classes2 = new ArrayList<Class>();
			for(int ii=0;ii<clsSetList.size();ii++)
			{
				//classes.clear();
				//classes.addAll(clsSetList.get(ii).getClassList());
				//classes.removeAll(clsSetList1.get(ii).getClassList());
				//int a=classes.size();
				//classes.clear();
				//classes.addAll(clsSetList1.get(ii).getClassList());
				//classes.removeAll(clsSetList.get(ii).getClassList());
				//System.out.println(classes.size());
				classes1=clsSetList.get(ii).getClassList();
				classes2=clsSetList1.get(ii).getClassList();
				for(int jj=0;jj<classes1.size();jj++)
				if(!classes1.get(jj).equals(classes2.get(jj)))
				{
					System.out.println("���"+ii+":"+jj+"buyiyang");
					System.out.println("clsSetList:"+classes1.get(jj).getContent().toString());
					System.out.println("clsSetList1:"+classes2.get(jj).getContent().toString());
				}
				
			}
		}
		else
		{
			ss=false;
		}
		if(ss)
			System.out.println("һ����");*/
			
		
		//float l=1;
		System.out.println("���ֽ���");
		Graph graph = new Graph();
		graph.createGraph(clsSetList,header.length);
		Rule rule = new Rule();
		String outURL = currentDIR+"\\rules.txt";
		Util.write(outURL,rule.findRules(graph,dataSet,header.length));//�����ļ�
		System.out.println("\nThe End.");
		return outURL;
	}
	
}
