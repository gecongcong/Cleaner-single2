package dr.test;

import java.util.ArrayList;
import java.util.Arrays;

import dr.data.Class;
import dr.util.Util;

public class Main {
	public  static void main(String args[]){
		
		//String[] num=new String[]{"A","B","C","D"};
       // String str="";
        //count(0,str,num,3);//求3个数的组合个数
        //count(0,str,num,3);
        //求1-n个数的组合个数
        //count1(0,str,num);
		/*ArrayList<String> searchLattice = new ArrayList<String>();
		
		searchLattice.add("123");
		searchLattice.add("234");
		searchLattice.add("345");
		searchLattice.add("456");
		System.out.println(searchLattice.size());
		searchLattice.remove("345");
		System.out.println(searchLattice.size());
		for(int i=0;i<searchLattice.size();i++)
			System.out.println(searchLattice.get(i));*/
		boolean []aa=new boolean[12];
		System.out.println(aa[0]);
	
	}
	
	private static void count1(int i, String str, int[] num) {
        if(i==num.length){
            System.out.println(str);
            return;
        }
        count1(i+1,str,num);
        count1(i+1,str+num[i]+",",num);
    }
    
//	private static void count(int i, String str, String[] num,int n) {
//        if(n==0){
//            System.out.println(str);
//            return;
//        }
//        if(i==num.length){
//            return;
//        }
//        count(i+1,str+num[i]+",",num,n-1);
//        count(i+1,str,num,n);
//    }
	
	private static void count(int i, String str,String[] attribute,int n){ //辅助函数：求n个数的组合情况
		if(n==0){
            System.out.println(str);
            String[] multiple = str.split(",");
            System.out.println(Arrays.toString(multiple));
            return;
        }
        if(i==attribute.length){
            return;
        }
        count(i+1,str+attribute[i]+",",attribute,n-1);
        count(i+1,str,attribute,n);
    }
	
}
