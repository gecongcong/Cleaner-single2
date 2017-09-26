package dr.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {

	/** 
	 * 计算阶乘数，即n! = n * (n-1) * ... * 2 * 1 
	 * @param n 
	 * @return 
	 */  
/*	private static int factorial(int n) {  //计算阶乘数
	    return (n > 1) ? n * factorial(n - 1) : 1;  
	}*/
	
	/** 
	 * 计算组合数，即C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
/*	public static int combination(int n, int m) {  //计算组合数
	    return (n >= m) ? factorial(n) / factorial(n - m) / factorial(m) : 0;  
	} */
	public  static String[] substractAttribute(String []a,String []b)//a包含b，计算a与b的差值
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
	
	public static boolean ifSubsumed(String []a,String b[])//判断a是否包含b
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
				File file = new File(fileName); // 找到File类的实例
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
	public static int[][] YHTri(int n) {  //计算组合数利用杨辉三角
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
			ans[i] = ans[i - 1] + YH[num][i];// 获取每一个level的第一个下标
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
	
	//排除诸如{{1,2},{3，5}。。。}和{{1,2},{3，5}，{4}。。。}
	/*for(int j=0;j<cls.getClassList().size();j++)
		for(k=0;k<ΩX1.size();k++)
		{
			//System.out.println("ΩX1.get(k).getContent():"+ΩX1.get(k).getContent().toString());
			//System.out.println("cls.getClassList().get(j):"+cls.getClassList().get(j).getContent().toString());
			if(ΩX1.get(k).getContent().toString().equals(cls.getClassList().get(j).getContent().toString()))
			{
			float S1=support1(Y,cls.getClassList().get(j).getMiniTID());
			if(S1>=θ)
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
			ΩX1=substractClass(ΩX,dd);					
		}
		}*/
}
