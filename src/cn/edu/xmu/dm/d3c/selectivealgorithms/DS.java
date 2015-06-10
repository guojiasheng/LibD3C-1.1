package cn.edu.xmu.dm.d3c.selectivealgorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instances;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class DS {
	//原动态选择
		public static double doDS(Instances train,Classifier[] cfsArray,List<Integer> D,List<Double> correctRateArray,double initCorrectRate,List<Double> currentResult,List<Integer> ClassifierNo){
			double correctRate;
			int k;
			int threshold=2*cfsArray.length;
			//
			List<Integer> tempD=new ArrayList();
			tempD.addAll(D);
			//
			ClassifierNo.add(tempD.get(0));
			correctRate=correctRateArray.get(tempD.get(0));
			tempD.remove(0);
			//
			//
			while (tempD.size() != 0) {
				//
				if(ClassifierNo.size()>threshold){
					correctRate=currentResult.get(1);
					while(ClassifierNo.size()!=currentResult.get(2)){
						ClassifierNo.remove(ClassifierNo.size()-1);
					}
				}
				//
				if (correctRate >= initCorrectRate) {
					break;
				} else {
					
					ClassifierNo.add(tempD.get(0));
					
					Classifier[] newCfsArray = new Classifier[ClassifierNo.size()];
					
					for(k=0;k<ClassifierNo.size();k++){
						newCfsArray[k]=cfsArray[ClassifierNo.get(k)];
					}
					
					correctRate = D3CVoter.ensembleVote(train, /* test, */newCfsArray);
						
					tempD.remove(0);
					
					if((correctRate>currentResult.get(1)) && (ClassifierNo.size()<=threshold)){
						currentResult.clear();
						currentResult.add(Double.MAX_VALUE);
						currentResult.add(correctRate);
						currentResult.add((double)ClassifierNo.size());
					}
				}
			}
			//
			currentResult.clear();
			currentResult.add((double)0);
			currentResult.add(correctRate);
			currentResult.add((double)ClassifierNo.size());
			//
//			System.out.println();
//			DecimalFormat df=new DecimalFormat("0.00000");
			//System.out.println(df.format(correctRate));
			
			return correctRate;
			
		}
}
