package cn.edu.xmu.dm.d3c.voters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Vote;
import weka.core.Instances;
import weka.core.SelectedTag;
import cn.edu.xmu.dm.d3c.utils.InstanceUtil;

public class D3CVoter {
	
	public static double ensembleVote(Instances train, Classifier[] newCfsArray) {

		double correctRate =0;
		
		try {
			int i;
			
			Vote ensemble = new Vote();
			SelectedTag tag = new SelectedTag(Vote.MAJORITY_VOTING_RULE,
					Vote.TAGS_RULES);
			ensemble.setCombinationRule(tag);
			ensemble.setClassifiers(newCfsArray);
			ensemble.setSeed(2);
			ensemble.buildClassifier(train);
			Evaluation eval = new Evaluation(train);
			Random random = new Random(1000);
			eval.crossValidateModel(ensemble, train, 5, random);
			
			correctRate = 1 - eval.errorRate();
//			setTempMatrixString(eval.toMatrixString());
//			setTempClassDetailsString(eval.toClassDetailsString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return correctRate;
	}
	//概率投票
	public static double probabilityVote(Instances train, List<Double> correctRateArray, List<List<double[]>> distributeForInstances){
		int i,j,k;
		double correctRate=0;
		List<double[]> sumOfProbability=new ArrayList<double[]>();
		for(i=0;i<distributeForInstances.get(0).size();i++){
			double[] temp=new double[distributeForInstances.get(0).get(0).length];
			for(j=0;j<temp.length;j++){
				temp[j]=0;
			}
			for(j=0;j<distributeForInstances.size();j++){
				for(k=0;k<distributeForInstances.get(j).get(i).length;k++){
					temp[k]=temp[k]+correctRateArray.get(j)*distributeForInstances.get(j).get(i)[k];
				}
			}
			sumOfProbability.add(temp);
		}
		InstanceUtil myutil=new InstanceUtil();
		List<Double> predict_class=new ArrayList<Double>();
		for(i=0;i<sumOfProbability.size();i++){
			predict_class.add(myutil.findTheMaxNo(sumOfProbability.get(i)));
		}
		double numOfError=0;
		System.out.println("size:"+predict_class.size());
		System.out.println("instances:"+train.numInstances());
		for(i=0;i<predict_class.size();i++){
			if(train.instance(i).classValue()!=predict_class.get(i)){
				numOfError++;
			}
		}
		correctRate=((double)train.numInstances()-numOfError)/(double)train.numInstances();
		return correctRate; 
	}
}
