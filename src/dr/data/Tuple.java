package dr.data;

public class Tuple {
	
	private String[] TupleContext = null;
	private int index = -1;//index from 1 to dataset.size;
	
	public Tuple(){}
	
	public void init(String tupleLine,String splitString,int index){//Init the tuple
		
		this.TupleContext = tupleLine.split(splitString);
		this.index = index;
		//System.out.println("Tuple.length = "+Tuple.length);
	}
	
	public String[] getContext(){
		if(null!=TupleContext){
			return TupleContext;
		}else
			System.out.println("Error: tuple context is empty.");
			return null;
	}
	
	public int getIndex(){
		return index;
	}
}
