package dr.data;

import java.util.ArrayList;

public class Class implements Comparable{//内容 例如：{1,2,3,5}
	
	private ArrayList<Integer> tupleIndexList;
	
	private int size = 0;
	
	private int minimal_tuple_id = -1;
	
	public int getSize(){
		return size;
	}
	
	public int getMiniTID(){
		return minimal_tuple_id;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setMiniTID(int TID){
		minimal_tuple_id = TID;
	}
	
	public Class(){}
	
	public ArrayList<Integer> getContent(){
		//获得class的内容，即tuple index list,例如：{1,2,3,5}
		return tupleIndexList;
	}
	
	public void setClassContent(ArrayList<Integer> tupleIndexList){
		this.tupleIndexList = tupleIndexList;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Class a=(Class)obj;
		ArrayList<Integer> aa=new ArrayList<Integer>();
		aa.addAll(a.tupleIndexList);
		aa.removeAll(tupleIndexList);
		if(aa.size()!=0)
			return false;
		
		return a.minimal_tuple_id==minimal_tuple_id;
		
		
	}

	@Override
	public int compareTo(Object o) {
		Class a=(Class)o;
		if(this.getMiniTID()>a.getMiniTID())
			return 1;
		else
			return -1;
		// TODO Auto-generated method stub
	}

}
