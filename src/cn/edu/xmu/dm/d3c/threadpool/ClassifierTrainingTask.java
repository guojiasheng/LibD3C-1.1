package cn.edu.xmu.dm.d3c.threadpool;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import cn.edu.xmu.dm.d3c.core.SelectiveEnsemble;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassifierTrainingTask implements Callable<Classifier> {
	
	//
	private double value;
	
	private Instances input;

	private Instances train;
	
	private Instances validation;
	
	private Classifier classifier;
	
	private List<List<Integer>> classifyRightOrWrong;
	
	private List<List<Integer>> classifyErrorNo;
	
	private List<Double> correctRateArray;
	
	private List<List<double[]>> classifyDistributeForInstances;
	
	//private Random random;
	
	private int index;
	
	//private List<ClassifierIndex> qqs;
	private List<Integer> qqs;
	
	private List<String> pathOfClassifiers;
	
	private List<String> parameterOfCV;
	
	//private StringBuffer outBuff;
	
	private long executeTime;
	
	private boolean isFinished;
	
	// 构造函数
	public ClassifierTrainingTask(
			Instances input, 
			Instances train,
			Instances validation,
			Classifier classifier, 
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo,
			List<Double> correctRateArray, 
			List<List<double[]>> classifyDistributeForInstances,
			int index, 
			List<Integer> qqs,
			List<String> pathOfClassifiers,
			List<String> parameterOfCV) {
		//
		this.input = input;
		this.train= train;
		this.validation = validation;
		this.classifier = classifier;
		this.classifyRightOrWrong=classifyRightOrWrong;
		this.classifyErrorNo = classifyErrorNo;
		this.correctRateArray = correctRateArray;
		this.classifyDistributeForInstances=classifyDistributeForInstances;
		//this.random = random;
		this.index = index;
		this.qqs = qqs;
		this.pathOfClassifiers=pathOfClassifiers;
		this.parameterOfCV=parameterOfCV;
		//this.outBuff=outBuff;
	}
	
	public Classifier call() throws Exception {
		SelectiveEnsemble se = new SelectiveEnsemble();
		
		Classifier cf = se.CrossValidationModelForDistribute(
				input, 
				train,
				validation,
				classifier, 
				classifyRightOrWrong,
				classifyErrorNo,
				correctRateArray, 
				classifyDistributeForInstances,
				/*random,*/
				index, 
				qqs,
				pathOfClassifiers,
				parameterOfCV);
		this.isFinished = true;
		
		return cf;
	}
}
