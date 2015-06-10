package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;

public class Parameterz {
	
	public List<List<Integer>> classifyRightOrWrong;
	public List<List<Integer>> classifyErrorNo;
	public List<Double> correctRateArray;
	public List<List<double[]>> classifyDistributeForInstances;
	public List<Integer> qqs;
	
	// 重新保存分类器信息
	public Classifier[] available_cfsArray;
	public List<List<Integer>> available_classifyRightOrWrong;
	public List<List<Integer>> available_classifyErrorNo;
	public List<Double> available_correctRateArray;
	public List<List<double[]>> available_classifyDistributeForInstances; 
	public List<Integer> temp_qc_1;
	
	public Parameterz(int cfNum) {
		classifyRightOrWrong = new ArrayList<List<Integer>>();	// 用于记录每种分类器单独训练时分类情况，1表示分类正确，0表示分类错误
		classifyErrorNo = new ArrayList<List<Integer>>();		// 用于得到每种分类器单独训练时候错分实例的编号
		correctRateArray = new ArrayList<Double>();					// 用来存放各种算法的正确率
		classifyDistributeForInstances=new ArrayList<List<double[]>>();// 用来存放概率分布得到的数据
		qqs=new ArrayList<Integer>();
		
		available_cfsArray=new  Classifier[cfNum];
		available_classifyRightOrWrong=new ArrayList<List<Integer>>();
		available_classifyErrorNo = new ArrayList<List<Integer>>();
		available_correctRateArray = new ArrayList<Double>();
		available_classifyDistributeForInstances=new ArrayList<List<double[]>>(); 
		temp_qc_1=new ArrayList<Integer>();
	}
}
