package cn.edu.xmu.dm.d3c.threadpool;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.core.Instances;
import cn.edu.xmu.dm.d3c.core.CircleCombine;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EBSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EFSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCNRR;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCRR;

public class EnsemblingThread extends Thread
// 通过继承Thread类,并实现它的抽象方法run()
// 适当时候创建这一Thread子类的实例来实现多线程机制
// 一个线程启动后（也即进入就绪状态）一旦获得CPU将自动调用它的run()方法
{
	private boolean isFinished = false;
	
	private String selectiveAlgorithm;
	private String CCAlgorithm = "EBSS";
	private String strategy;
	private double currentCorrectRate = 0;
	private Instances input;
	private double newInitCorrectRate = 1;
	private double newInterval = 0.05;
	
	private List<List<Integer>> newClassifyRightOrWrong;
	private List<List<Integer>> newClassifyErrorNo;
	private List<Double> newCorrectRateArray;
	private List<List<double[]>> newClassifyDistributeForInstances;
	private List<Double> currentResult;
	private List<Integer> ClassifierNo;
	
	public EnsemblingThread() {
		
	}// 构造函数

	public void run() {
		
		Instances inputR = new Instances(input);
		Random random = new Random(1);
		inputR.randomize(random);
		
		if (selectiveAlgorithm.equals("HCNRR"))
			currentCorrectRate = HCNRR.doHCNRR(inputR, newInitCorrectRate,
					newClassifyRightOrWrong, newClassifyErrorNo,
					newCorrectRateArray, newClassifyDistributeForInstances,
					currentResult, ClassifierNo);
		else if (selectiveAlgorithm.equals("HCRR"))
			currentCorrectRate = HCRR.doHCRR(inputR, newInitCorrectRate,
					newClassifyRightOrWrong, newClassifyErrorNo,
					newCorrectRateArray, newClassifyDistributeForInstances,
					currentResult, ClassifierNo);
		else if (selectiveAlgorithm.equals("EBSS"))
			currentCorrectRate = EBSS.doEBSS(inputR, newInitCorrectRate,
					newClassifyRightOrWrong, newClassifyErrorNo,
					newCorrectRateArray, newClassifyDistributeForInstances,
					currentResult, ClassifierNo);
		else if (selectiveAlgorithm.equals("EFSS"))
			currentCorrectRate = EFSS.doEFSS(inputR, newInitCorrectRate,
					newClassifyRightOrWrong, newClassifyErrorNo,
					newCorrectRateArray, newClassifyDistributeForInstances,
					currentResult, ClassifierNo);
		else if (selectiveAlgorithm.equals("CC"))
			try {
				currentCorrectRate = CircleCombine.doCircleCombine(inputR,
						newInitCorrectRate, newInterval, newClassifyRightOrWrong,
						newClassifyErrorNo, newCorrectRateArray,
						newClassifyDistributeForInstances, currentResult,
						ClassifierNo, CCAlgorithm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if (selectiveAlgorithm.equals("DS"))
			try {
				currentCorrectRate = CircleCombine.doCircleCombine(inputR,
						newInitCorrectRate, newInterval, newClassifyRightOrWrong,
						newClassifyErrorNo, newCorrectRateArray,
						newClassifyDistributeForInstances, currentResult,
						ClassifierNo, CCAlgorithm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				throw new Exception("Could not find selective algorithm:"
						+ selectiveAlgorithm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		this.isFinished = true;
	}

	public String getSelectiveAlgorithm() {
		return selectiveAlgorithm;
	}

	public void setSelectiveAlgorithm(String selectiveAlgorithm) {
		this.selectiveAlgorithm = selectiveAlgorithm;
	}

	public String getCCAlgorithm() {
		return CCAlgorithm;
	}

	public void setCCAlgorithm(String cCAlgorithm) {
		CCAlgorithm = cCAlgorithm;
	}

	public double getCurrentCorrectRate() {
		return currentCorrectRate;
	}

	public void setCurrentCorrectRate(double currentCorrectRate) {
		this.currentCorrectRate = currentCorrectRate;
	}

	public Instances getInput() {
		return input;
	}

	public void setInput(Instances input) {
		this.input = input;
	}

	public double getNewInitCorrectRate() {
		return newInitCorrectRate;
	}

	public void setNewInitCorrectRate(double newInitCorrectRate) {
		this.newInitCorrectRate = newInitCorrectRate;
	}

	public double getNewInterval() {
		return newInterval;
	}

	public void setNewInterval(double newInterval) {
		this.newInterval = newInterval;
	}

	public List<List<Integer>> getNewClassifyRightOrWrong() {
		return newClassifyRightOrWrong;
	}

	public void setNewClassifyRightOrWrong(
			List<List<Integer>> newClassifyRightOrWrong) {
		this.newClassifyRightOrWrong = newClassifyRightOrWrong;
	}

	public List<List<Integer>> getNewClassifyErrorNo() {
		return newClassifyErrorNo;
	}

	public void setNewClassifyErrorNo(List<List<Integer>> newClassifyErrorNo) {
		this.newClassifyErrorNo = newClassifyErrorNo;
	}

	public List<Double> getNewCorrectRateArray() {
		return newCorrectRateArray;
	}

	public void setNewCorrectRateArray(List<Double> newCorrectRateArray) {
		this.newCorrectRateArray = newCorrectRateArray;
	}

	public List<List<double[]>> getNewClassifyDistributeForInstances() {
		return newClassifyDistributeForInstances;
	}

	public void setNewClassifyDistributeForInstances(
			List<List<double[]>> newClassifyDistributeForInstances) {
		this.newClassifyDistributeForInstances = newClassifyDistributeForInstances;
	}

	public List<Double> getCurrentResult() {
		return currentResult;
	}

	public void setCurrentResult(List<Double> currentResult) {
		this.currentResult = currentResult;
	}

	public List<Integer> getClassifierNo() {
		return ClassifierNo;
	}

	public void setClassifierNo(List<Integer> classifierNo) {
		ClassifierNo = classifierNo;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
}

