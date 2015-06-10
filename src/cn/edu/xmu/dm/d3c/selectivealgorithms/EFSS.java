package cn.edu.xmu.dm.d3c.selectivealgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.core.Instances;
import cn.edu.xmu.dm.d3c.metrics.ClassifierDiversity;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class EFSS {
	
	/* 集成前序选择
	 * 参数意义同爬山策略（重复随机）
	 * 集成前序选择是将正确率比较好的分类器选出，与之前获得的分类器一起计算差异性和正确率，然后判断分类器是否被选中
	 */
	public static double doEFSS(
			Instances train,
			double initCorrectRate,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo,
			List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances,
			List<Double> currentResult,
			List<Integer> ClassifierNo){
		
		double diversity;			//差异性
		double tempDiversity;
		double correctRate;			//正确率
		double voteCorrectRate=0;	//vote集成得到的正确率
		
		List<Integer> sortedNo=new ArrayList<Integer>();//按正确率排序之后的分类器编号
		int i=0,j,k;
		int tempNo,currentNo;
		int numOfClassifiers=classifyRightOrWrong.size();
		int threshold=2*numOfClassifiers;
		//对正确率进行降序，并且获得排序之后每个位置上正确率的对应分类器编号
		/****************************/
		List<Double> newCorrectRateArray=new ArrayList<Double>();
		List<Double> tempCorrectRateArray=new ArrayList<Double>();
		newCorrectRateArray.addAll(correctRateArray);
		tempCorrectRateArray.addAll(correctRateArray);
		
		List<Integer> temp=new ArrayList<Integer>();
		for(i=0;i<tempCorrectRateArray.size();i++){
			
			temp.add(i);
		}
		
		Collections.sort(newCorrectRateArray);
		for(i=newCorrectRateArray.size()-1;i>=0;i--){
			tempNo=tempCorrectRateArray.indexOf(newCorrectRateArray.get(i));
			sortedNo.add(temp.get(tempNo));
			tempCorrectRateArray.remove(tempNo);
			temp.remove(tempNo);
		}
		/****************************/
		if (ClassifierNo.size() == 0) {
			diversity=currentResult.get(0);
			ClassifierNo.add(sortedNo.get(0));
			correctRate = correctRateArray.get(sortedNo.get(0));
			sortedNo.remove(0);
		} else {
			diversity=currentResult.get(0);
			correctRate=currentResult.get(1);
		}
		while (sortedNo.size() != 0) {
			if (correctRate >= initCorrectRate) {
				break;
			} else {
				// 获得当前正确率最高的分类器的编号，将它与之前选中的分类器一起计算差异性
				currentNo = sortedNo.get(0);
				List<Integer>[] tempList = new List[ClassifierNo.size() + 1];
				for (k = 0; k < ClassifierNo.size(); k++) {
					tempList[k] = classifyRightOrWrong.get(ClassifierNo.get(k));
				}
				tempList[ClassifierNo.size()] = classifyRightOrWrong.get(currentNo);
				tempDiversity = ClassifierDiversity.CalculateK(tempList);
				if (tempDiversity <= diversity) {
					ClassifierNo.add(sortedNo.get(0));
					List<Double> new_correctRateArray = new ArrayList<Double>();
					List<List<double[]>> new_classifyDistributeForInstances=new ArrayList<List<double[]>>();
					for(j = 0; j < ClassifierNo.size(); j++){
						new_correctRateArray.add(correctRateArray.get(ClassifierNo.get(j)));
						new_classifyDistributeForInstances.add(classifyDistributeForInstances.get(ClassifierNo.get(j)));
					}
					voteCorrectRate=D3CVoter.probabilityVote(train,new_correctRateArray,new_classifyDistributeForInstances);
					if (voteCorrectRate > correctRate) {
						diversity = tempDiversity;
						correctRate = voteCorrectRate;
//						setBestMatrixString(tempMatrixString);
//						setBestClassDetailsString(tempClassDetailsString);
					} else {
						ClassifierNo.remove(ClassifierNo.size() - 1);
					}
				}
				sortedNo.remove(0);
			}
		}
		
		// 说明只有一个分类器，差异性为0
		if (diversity == Double.MAX_VALUE) {
			diversity = 0;
		}
		currentResult.clear();
		currentResult.add(diversity);
		currentResult.add(correctRate);
		currentResult.add((double)ClassifierNo.size());
		//
		//System.out.println(correctRate);
		//System.out.println(bestMatrixString);
		return correctRate;
	}
}
