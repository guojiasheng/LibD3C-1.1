package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.core.Instances;
import cn.edu.xmu.dm.d3c.clustering.KMeanz;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EBSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EFSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCNRR;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCRR;
import cn.edu.xmu.dm.d3c.utils.ClassifyResultArffLoader;

public class ClusterBasedSelectiveStrategy {
	private double ssCorrectRate=1;	// 目标最优精度
	private double ssInterval=0.05;	// 每次衰减精度
	private int numClusters=10;		
	
	String selectiveAlgorithm="HCRR";
	String CCAlgorithm="EBSS";
	
	//基于聚类的选择策略
	public List<String> doClusterBasedSelectiveStrategy(Instances input, Classifier[] cfsArray, int numfolds, int numSlots, int timeOut, List<String> pathOfClassifiers,List<String> parameterOfCV)throws Exception{
		int i;
		
		Instances inputR = new Instances(input);
		Random random = new Random(1);
		inputR.randomize(random);
		
		WeakClassifiersIndependentTrainer wcit = new WeakClassifiersIndependentTrainer();
		wcit.IndependentlyTrainWeakClassifiers(input, cfsArray, numfolds,numSlots, timeOut, pathOfClassifiers, parameterOfCV);
		Instances classifyResult = ClassifyResultArffLoader.loadClassifyResultFromArff("");
		
		List<Integer> chooseClassifiers = new ArrayList<Integer>();
		
		KMeanz km = new KMeanz(numClusters);
	
		Parameterz parameterz = new Parameterz(cfsArray.length);
		
		km.buildClusterer(classifyResult, chooseClassifiers, parameterz.available_correctRateArray);
		
		System.out.println("聚类选择的结果："+chooseClassifiers);
		
		List<Integer> temp_qc_2=new ArrayList<Integer>();
		
		for(i=0;i<chooseClassifiers.size();i++){
			temp_qc_2.add(parameterz.temp_qc_1.get(chooseClassifiers.get(i)));
		}
		
		System.out.println("聚类选择的对应编号："+temp_qc_2);
		
		
		Classifier[] newCfsArray = new Classifier[chooseClassifiers.size()];

		List<List<Integer>> newClassifyRightOrWrong = new ArrayList<List<Integer>>();
		
		List<List<Integer>> newClassifyErrorNo = new ArrayList<List<Integer>>();
	
		List<Double> newCorrectRateArray = new ArrayList();
		
		List<List<double[]>> newClassifyDistributeForInstances=new ArrayList<List<double[]>>();
		
		// 保存分类信息的子集
		for (i = 0; i < chooseClassifiers.size(); i++) {
			//用来存放已经聚类选择的分类器
			newCfsArray[i] = parameterz.available_cfsArray[chooseClassifiers.get(i)];
			// 用来得到通过聚类后选中的分类器的分类结果
			// 取原来available_classifyRightOrWrong的子集，子集的序号为聚类筛选之后的分类器的编号
			newClassifyRightOrWrong.add(parameterz.available_classifyRightOrWrong.get(chooseClassifiers.get(i))); 
			// 用来得到通过聚类后选中的分类器的错分实例编号
			newClassifyErrorNo.add(parameterz.available_classifyErrorNo.get(chooseClassifiers.get(i)));
			// 用来存放各种算法的正确率
			newCorrectRateArray.add(parameterz.available_correctRateArray.get(chooseClassifiers.get(i)));
			// 用来存放各种算法的概率分布
			newClassifyDistributeForInstances.add(parameterz.available_classifyDistributeForInstances.get(chooseClassifiers.get(i)));
		}
		
		double newInitCorrectRate = ssCorrectRate;
		double newInterval = ssInterval;
		
		List<Integer> ClassifierNo = new ArrayList();
		
		List<Double> currentResult = new ArrayList();
		
		currentResult.add(Double.MAX_VALUE);
		currentResult.add((double) 0);
		currentResult.add((double) 0);
		
		double currentCorrectRate = 0;
		
		if(selectiveAlgorithm.equals("HCNRR"))
			currentCorrectRate=HCNRR.doHCNRR(
				inputR, 
				newInitCorrectRate, 
				newClassifyRightOrWrong, 
				newClassifyErrorNo,
				newCorrectRateArray, 
				newClassifyDistributeForInstances, 
				currentResult,
				ClassifierNo);
		else if(selectiveAlgorithm.equals("HCRR"))
			currentCorrectRate=HCRR.doHCRR(
				inputR, 
				newInitCorrectRate, 
				newClassifyRightOrWrong, 
				newClassifyErrorNo,
				newCorrectRateArray, 
				newClassifyDistributeForInstances, 
				currentResult,
				ClassifierNo);
		else if(selectiveAlgorithm.equals("EBSS"))
			currentCorrectRate=EBSS.doEBSS(
				inputR, 
				newInitCorrectRate, 
				newClassifyRightOrWrong,
				newClassifyErrorNo,
				newCorrectRateArray,
				newClassifyDistributeForInstances,
				currentResult,
				ClassifierNo);
		else if(selectiveAlgorithm.equals("EFSS"))
			currentCorrectRate=EFSS.doEFSS(
				inputR, 
				newInitCorrectRate,
				newClassifyRightOrWrong,
				newClassifyErrorNo,
				newCorrectRateArray,
				newClassifyDistributeForInstances, 
				currentResult,
				ClassifierNo);
		else if(selectiveAlgorithm.equals("CC"))
			currentCorrectRate = CircleCombine.doCircleCombine(
				inputR,  
				newInitCorrectRate,
				newInterval,
				newClassifyRightOrWrong,
				newClassifyErrorNo,
				newCorrectRateArray,
				newClassifyDistributeForInstances,
				currentResult, 
				ClassifierNo,
				CCAlgorithm);
		else if(selectiveAlgorithm.equals("DS"))
			currentCorrectRate = CircleCombine.doCircleCombine(
				inputR,  
				newInitCorrectRate,
				newInterval,
				newClassifyRightOrWrong,
				newClassifyErrorNo,
				newCorrectRateArray,
				newClassifyDistributeForInstances,
				currentResult, 
				ClassifierNo,
				CCAlgorithm);
		else throw new Exception ("Could not find selective algorithm:"+selectiveAlgorithm);

		System.out.println("选择性集成所得结果："+currentCorrectRate);
		System.out.println("选择性集成获得的分类器编号："+ClassifierNo);
		List<Integer> temp_qc_3=new ArrayList<Integer>();
		for(i=0;i<ClassifierNo.size();i++){
			temp_qc_3.add(temp_qc_2.get(ClassifierNo.get(i)));
		}
		System.out.println("选择性集成学习获得的分类器对应编号："+temp_qc_3);
		List<String> choosePathOfClassifiers=new ArrayList<String>();
		for(i=0;i<temp_qc_3.size();i++){
			choosePathOfClassifiers.add(pathOfClassifiers.get(temp_qc_3.get(i)));
		}
		return choosePathOfClassifiers;
	}
	
	public void setSelectiveAlgorithm(String sa){
		selectiveAlgorithm=sa;
	}
	
	public void setCircleCombineAlgorithm(String cca){
		CCAlgorithm=cca;
	}
	
	public void setInitCorrectRate(double correctrate){
		ssCorrectRate=correctrate;
	}
	
	public void setInitInterval(double interval){
		ssInterval=interval;
	}
	
	public void setNumClusters(int num){
		numClusters=num;
	}
}

