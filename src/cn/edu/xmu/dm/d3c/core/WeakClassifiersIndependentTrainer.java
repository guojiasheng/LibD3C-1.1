package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.core.Instances;
import cn.edu.xmu.dm.d3c.utils.ClassifyResultArffLoader;

public class WeakClassifiersIndependentTrainer {
	
	public List<Classifier> IndependentlyTrainWeakClassifiers(Instances input, Classifier[] cfsArray, double validatePercent, int numSlots, int timeOut, List<String> pathOfClassifiers,List<String> parameterOfCV) throws Exception {
		int i, j;
		
		SelectiveEnsemble se = new SelectiveEnsemble();
		Random random = new Random(1);
		Instances inputR = new Instances(input);
		inputR.randomize(random);
		
		Parameterz parameterz = new Parameterz(cfsArray.length);
		//独立训练
		List<Classifier> bcfs = se.IndependentTrain(
				inputR, 
				cfsArray, 
				validatePercent, 
				numSlots,
				timeOut,
				parameterz.classifyRightOrWrong, 
				parameterz.classifyErrorNo, 
				parameterz.correctRateArray, 
				parameterz.classifyDistributeForInstances, 
				random, 
				parameterz.qqs,
				pathOfClassifiers,
				parameterOfCV);
		
		System.out.println("并发独立训练结果："+parameterz.qqs);
		
		j=0;
		for(i=0;i<cfsArray.length;i++){
			int temp=parameterz.qqs.indexOf(i);
			if(temp!=-1){
				
				parameterz.available_cfsArray[j]=cfsArray[temp];
				parameterz.available_classifyRightOrWrong.add(parameterz.classifyRightOrWrong.get(temp));
				parameterz.available_classifyErrorNo.add(parameterz.classifyErrorNo.get(temp));
				parameterz.available_correctRateArray.add(parameterz.correctRateArray.get(temp));
				parameterz.available_classifyDistributeForInstances.add(parameterz.classifyDistributeForInstances.get(temp));
				j++;
				parameterz.temp_qc_1.add(i);
		
			}
		}
		
		System.out.println("可用的分类器按序："+parameterz.temp_qc_1);
		
		ClassifyResultArffLoader.writeClassifyResult(
													input, 
													validatePercent,
													"", 
													parameterz.temp_qc_1,
													parameterz.available_classifyRightOrWrong, 
													parameterz.available_classifyErrorNo,
													parameterz.available_correctRateArray, 
													parameterz.available_classifyDistributeForInstances);
		return bcfs;
	}
}
