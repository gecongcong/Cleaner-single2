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
		
		String splitString = ",";//	数据之间的分隔符，比如以逗号分隔 或 空格 分隔。
		String currentDIR = System.getProperty("user.dir");//获得当前工程路径
		
//		String fileURL = currentDIR + "\\dataSet\\"+ "synthetic-car\\ground_truth.csv";
//		String fileURL = currentDIR + "\\dataSet\\"+ "test-city.data";
//	    String fileURL = currentDIR + "\\dataSet\\"+ "car evaluation-new\\car.data";
		//String fileURL = "E:\\gcc\\ground_truth.txt";
		//String fileURL = "E:\\gcc\\DiscoverRules\\dataSet\\cars_com\\cars_com.txt";
		boolean ifHeader = true;	//If the dataSet has header or not 数据集是否有属性名这一列
		
		DataSet dataSet = new DataSet(); 
		dataSet.init(fileURL,splitString,ifHeader); //import the dataSet,and format 导入数据集并格式化
		ArrayList<String[]> searchLattice = dataSet.getSearchLattice();//形成查询网络
		
		System.out.println("---------Generate Attribute Search Lattice 构建查询网络---------\n");
		System.out.println("searchLattice.size = "+searchLattice.size()+"\n");
		
		ArrayList<ClassSet> clsSetList = new ArrayList<ClassSet>();
		
		String[] header = dataSet.getHeader();
		int i;
		for(i=0;i<header.length;i++){ //该步骤是为了获得 attributes 和 attrIndex 参数
			String[] attributes= searchLattice.get(i);
			
			//System.out.println(Arrays.toString(attributes));//测试：打印查询网络 Attribute Search Lattice
			
			Partition partition = new Partition(dataSet);
			int[] attrIndex = new int[attributes.length];
			
			for(int j=0;j<attributes.length;j++){	//给attrIndex赋值，即找到Attribute对应的序号
				for(int k=0;k<header.length;k++){
					if(attributes[j].equals(header[k])){
						attrIndex[j] = k;
						break;
					}
				}
			}
			
			//执行划分操作
			ClassSet clsSet = partition.partition(dataSet, attributes, attrIndex);
			clsSetList.add(clsSet);
		}
		
		
		for(;i<searchLattice.size();i++){ 
			String[] attributes= searchLattice.get(i);	
			//System.out.println(Arrays.toString(attributes));//测试：打印查询网络 Attribute Search Lattice
			Partition partition = new Partition(dataSet);
			//执行划分操作
			ClassSet clsSet = partition.partition1(dataSet, attributes,clsSetList);
			clsSet.sort();
			clsSetList.add(clsSet);
		}	
		
/*		ArrayList<ClassSet> clsSetList1 = new ArrayList<ClassSet>();
		for(i=0;i<searchLattice.size();i++){ //该步骤是为了获得 attributes 和 attrIndex 参数
			String[] attributes= searchLattice.get(i);
			
			//System.out.println(Arrays.toString(attributes));//测试：打印查询网络 Attribute Search Lattice
			
			Partition partition = new Partition(dataSet);
			int[] attrIndex = new int[attributes.length];
			
			for(int j=0;j<attributes.length;j++){	//给attrIndex赋值，即找到Attribute对应的序号
				for(int k=0;k<header.length;k++){
					if(attributes[j].equals(header[k])){
						attrIndex[j] = k;
						break;
					}
				}
			}
			
			//执行划分操作
			ClassSet clsSet = partition.partition(dataSet, attributes, attrIndex);
			clsSetList1.add(clsSet);
		}
		
		boolean ss=true;
		if(clsSetList1.size()==clsSetList.size())
		{
			System.out.println("个数相同！");
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
					System.out.println("结点"+ii+":"+jj+"buyiyang");
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
			System.out.println("一样！");*/
			
		
		//float l=1;
		System.out.println("划分结束");
		Graph graph = new Graph();
		graph.createGraph(clsSetList,header.length);
		Rule rule = new Rule();
		String outURL = currentDIR+"\\rules.txt";
		Util.write(outURL,rule.findRules(graph,dataSet,header.length));//保存文件
		System.out.println("\nThe End.");
		return outURL;
	}
	
}
