package data;

public class ProbAttribute {
	
	private String name = null;		//������
	private String value = null;	//����ȡֵ
	private double prob = 0.0;		//������ֵ�ĸ���
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public double getProb() {
		return prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
