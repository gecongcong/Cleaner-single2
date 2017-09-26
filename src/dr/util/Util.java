package dr.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {

	/** 
	 * ����׳�������n! = n * (n-1) * ... * 2 * 1 
	 * @param n 
	 * @return 
	 */  
/*	private static int factorial(int n) {  //����׳���
	    return (n > 1) ? n * factorial(n - 1) : 1;  
	}*/
	
	/** 
	 * �������������C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
/*	public static int combination(int n, int m) {  //���������
	    return (n >= m) ? factorial(n) / factorial(n - m) / factorial(m) : 0;  
	} */
	public  static String[] substractAttribute(String []a,String []b)//a����b������a��b�Ĳ�ֵ
	{
	
		Arrays.sort(a);
		Arrays.sort(b);
		String[]ans1=new String[a.length-b.length];
		int u=0,v=0,w=0;
		for(;u<a.length&&v<b.length;)
		{
			if(a[u].compareTo(b[v])<0)
			{
				ans1[w++]=a[u];
				u++;
			}
			else if(a[u].compareTo(b[v])==0)
			{
				u++;
				v++;
			}
			else if(a[u].compareTo(b[v])>0)
			{
				v++;
			}
		}
		for(int i=u;i<a.length;i++)
			ans1[w++]=a[i];
		return ans1;
		
	}
	
	public static boolean ifSubsumed(String []a,String b[])//�ж�a�Ƿ����b
	{
		//Arrays.sort(a);
		//Arrays.sort(b);
		if(a.length<b.length)
			return false;
		else
		{
			int i=0,j=0;
			boolean flag=true;
			for(;i<b.length;)
			{
				if(b[i].compareTo(a[j])<0)
				{
					flag=false;
					break;
				}
				else if(b[i].compareTo(a[j])==0)
				{
					i++;
					j++;
					if(i==b.length)
					{
						break;
					}
					else if(j==a.length)
					{
						flag=false;
						break;
					}
				}
				else if(b[i].compareTo(a[j])>0)
				{
					j++;
					if(j==a.length)
					{
						flag=false;
						break;
					}
				}
			}
			if(flag)
				return true;
			else
				return false;
		}
	}
	
	public static boolean ifContain(String[] pre_X, String[] current_X){
		boolean key = false;
		int num = 0;
		for(int i=0;i<pre_X.length;i++){
			for(int k=0;k<current_X.length;k++){
				if(pre_X[i].equals(current_X[k])){
					num++;
					break;
				}
			}
		}
		if(num==pre_X.length)key=true;
		return key;
	}
	
	//write CL in txt
	public static void write(String fileName,String CL)
	{
				File file = new File(fileName); // �ҵ�File���ʵ��
		        try {
		        	if(!file.exists()){  
			            file.createNewFile();
			        }
		            Writer out = null; 
		            out = new FileWriter(file,false); 
		            out.write(CL+"\r\n"); 
		            out.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
				

	}
	
	static int MAX=50;
	public static int[][] YHTri(int n) {  //��������������������
	    int ans[][]=new int[MAX][MAX];
	    ans[0][0]=1;
	    ans[1][0]=1;
	    ans[1][1]=1;
	    for(int i=2;i<=n;i++)
	    	for(int j=0;j<=i;j++)
	    	{
	    		if(j==0)
	    			ans[i][j]=1;
	    		else if(j==i)
	    			ans[i][j]=1;
	    		else
	    		ans[i][j]=ans[i-1][j]+ans[i-1][j-1];
	    	}	
		return ans;
	    
	} 
	
	public static int[] getLevelFirst(int YH[][],int num)
	{
		int [] ans=new int[num+1];
		ans[0] = 0;
		for (int i = 1; i < num; i++)
			ans[i] = ans[i - 1] + YH[num][i];// ��ȡÿһ��level�ĵ�һ���±�
		return ans;
	}
	
	public  static void main(String args[]){
		//System.out.println(combination(7,1)+combination(7,2)+combination(7,3)+combination(7,4)+combination(7,5)+combination(7,6)+combination(7,7));
		
/*		String ss[]={"CLS","ED","GEN","OCC"};
		String st[]={"CLS","OCC","REC"};
		System.out.println(Arrays.toString(ss));
		if(ifSubsumed(ss,st)){
			String sss[]=substractAttribute(ss,st);
			for(int i=0;i<sss.length;i++)
				System.out.println(sss[i]);
		}*/
		int num=4;
		int YH[][]=YHTri(num);
		int first[]=getLevelFirst(YH,num);
		for(int i=0;i<first.length;i++)
			System.out.print(first[i]+" ");
	}
	
	//�ų�����{{1,2},{3��5}������}��{{1,2},{3��5}��{4}������}
	/*for(int j=0;j<cls.getClassList().size();j++)
		for(k=0;k<��X1.size();k++)
		{
			//System.out.println("��X1.get(k).getContent():"+��X1.get(k).getContent().toString());
			//System.out.println("cls.getClassList().get(j):"+cls.getClassList().get(j).getContent().toString());
			if(��X1.get(k).getContent().toString().equals(cls.getClassList().get(j).getContent().toString()))
			{
			float S1=support1(Y,cls.getClassList().get(j).getMiniTID());
			if(S1>=��)
			{
			String ss[]=dataset.getHeader();
			for(int jj=0;jj<ss.length;jj++)
			{
				if(ss[jj].equals(Q[i]))
				{
					String ans=dataset.getTupleList().get(cls.getClassList().get(j).getMiniTID()).getContext()[jj];
					addRule(Q[i],P,A,ans);
					break;
				}
			}
			
			}
			ArrayList<Class> dd=new ArrayList<Class>();
			dd.add(cls.getClassList().get(j));
			��X1=substractClass(��X,dd);					
		}
		}*/
}
