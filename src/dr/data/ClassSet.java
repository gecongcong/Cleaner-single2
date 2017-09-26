package dr.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClassSet{//内容格式如：{{1,2,3,5},{4,6}}
	
	private ArrayList<Class> classList = new ArrayList<Class>();
	//格式如：{{1,2,3,5},{4,6}}
	
	private String[] X = null;//该分类的attribute依据，如 X=MR 或 X=(MR,REL)

	public ClassSet(){}
	
	public void init(ArrayList<Class> classList,String[] className){
		this.classList = classList;
		this.X = className;
	}
	
	public void setX(String[] attributes){
		X = attributes;
	}
	
	public String[] getX(){
		return this.X;
	}
	
	public void setClassList(ArrayList<Class> classList){
		this.classList = classList;
	}

	public void addClass(Class cls){
		classList.add(cls);
	}
	
	public ArrayList<Class> getClassList(){
		return this.classList;
	}
	
	public void sort()
	{
		Collections.sort(classList);
	}
	
	public void print(){//输出classSet的内容，如 ∏[ED]={[0, 1, 9],[2, 4, 8],[3, 6],[5],[7, 10]}
		
		int size = classList.size();

		System.out.print("∏"+Arrays.toString(this.X)+"={");
		for(int i=0;i<size-1;i++){
			System.out.print(classList.get(i).getContent().toString()+",");
		}
		System.out.print(classList.get(size-1).getContent().toString());
		System.out.println("}");
	}

}
