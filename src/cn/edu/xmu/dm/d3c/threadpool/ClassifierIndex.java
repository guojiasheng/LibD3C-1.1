package cn.edu.xmu.dm.d3c.threadpool;

public class ClassifierIndex {
	
	//
	private int index;
	private double correctRate;
	//
	public ClassifierIndex(int index, double correctRate){
		this.index = index;
		this.correctRate = correctRate;
	}
	//
	public int getIndex() {
		return index;
	}
	//
	public void setIndex(int index) {
		this.index = index;
	}
	//
	public double getCorrectRate() {
		return correctRate;
	}
	//
	public void setCorrectRate(double correctRate) {
		this.correctRate = correctRate;
	}
}
