package cn.edu.xmu.dm.d3c.selectivealgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.core.Instances;
import cn.edu.xmu.dm.d3c.metrics.ClassifierDiversity;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class HCNRR {
	/*
	 * 爬山策略（非重复随机）
	 * 参数意义和爬山策略（重复随机）相同，爬山策略（重复随机）和爬山策略（不重复随机）主要的不同在于抽取分类器的方式上，有点像不重复采样和重复采样
	 */
	public static double doHCNRR(Instances train, double initCorrectRate,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo, List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances,
			List<Double> currentResult, List<Integer> ClassifierNo) {
		
		double diversity;
		double tempDiversity;
		double correctRate;
		double voteCorrectRate;
		
		int i = 0, j, k;
		int r;
		int maxNo;
		int tempNo;
		int candidateNo;
		int numOfClassifiers = classifyRightOrWrong.size();
		
		List<Integer> candidateClassifierNo = new ArrayList<Integer>();// 候选分类器列表初始化基本包括全部的分类器编号，其中分类器编号会在比较之后删除
		
		Random random = new Random(1000);
		// 如果列表为空，说明是第一轮循环，那么就决定是分类效果最好的分类器还是最近产生的分类器为第一个分类器，并将除了第一个分类器以外的所有分类器当作候选分类器，
		// 否则就只要将正确率替换成前一轮循环得到的正确率，并将全部的分类器当作候选分类器
		if (ClassifierNo.size() == 0) {

			diversity = currentResult.get(0);
			boolean bestBegin = false;
			if (bestBegin == false) {
				//
				r = random.nextInt(numOfClassifiers);
				correctRate = correctRateArray.get(r);
				ClassifierNo.add(r);
				tempNo = r;
			} else {
				
				correctRate = Collections.max(correctRateArray);
				maxNo = correctRateArray.indexOf(correctRate);
				ClassifierNo.add(maxNo);
				tempNo = maxNo;
			}
			for (j = 0; j < numOfClassifiers; j++) {
				if (j != tempNo) {
					candidateClassifierNo.add(j);
				}
			}
		} else {
			
			diversity = currentResult.get(0);
			correctRate = currentResult.get(1);
			
			for (j = 0; j < numOfClassifiers; j++) {
				candidateClassifierNo.add(j);
			}
		}
		
		while (candidateClassifierNo.size() != 0) {
			if (correctRate >= initCorrectRate) {
				break;
			} else {
				// 以候选分类器的数量作为参数,随机产生一个与之前没有重复的分类器编号
				r = random.nextInt(candidateClassifierNo.size());
				candidateNo = candidateClassifierNo.get(r);
				// 将得到该分类器和之前选择的分类器的分类结果一起计算差异性
				List<Integer>[] tempList = new List[ClassifierNo.size() + 1];
				for (k = 0; k < ClassifierNo.size(); k++) {
					tempList[k] = classifyRightOrWrong.get(ClassifierNo.get(k));
				}
				tempList[ClassifierNo.size()] = classifyRightOrWrong
						.get(candidateNo);
				tempDiversity = ClassifierDiversity.CalculateK(tempList);
				//
				if (tempDiversity <= diversity) {
					//
					ClassifierNo.add(candidateNo);

					// 用来存放各种算法的正确率
					List<Double> newCorrectRateArray = new ArrayList<Double>();
					for (j = 0; j < ClassifierNo.size(); j++) {
						//
						newCorrectRateArray.add(correctRateArray
								.get(ClassifierNo.get(j)));
					}

					// 用来存放各种算法的概率分布
					List<List<double[]>> newClassifyDistributeForInstances = new ArrayList<List<double[]>>();
					for (j = 0; j < ClassifierNo.size(); j++) {
						//
						newClassifyDistributeForInstances
								.add(classifyDistributeForInstances
										.get(ClassifierNo.get(j)));
					}

					voteCorrectRate = D3CVoter.probabilityVote(train,
							newCorrectRateArray,
							newClassifyDistributeForInstances);

					//System.out.println(voteCorrectRate);

					if (voteCorrectRate > correctRate) {
						diversity = tempDiversity;
						correctRate = voteCorrectRate;
//						setBestMatrixString(tempMatrixString);
//						setBestClassDetailsString(tempClassDetailsString);
					} else {
						ClassifierNo.remove(ClassifierNo.size() - 1);
					}
				}
				candidateClassifierNo.remove(r);
			}
		}
		// 说明只有一个分类器，差异性为0
		if (diversity == Double.MAX_VALUE) {
			diversity = 0;
		}
		//
		currentResult.clear();
		currentResult.add(diversity);
		currentResult.add(correctRate);
		currentResult.add((double) ClassifierNo.size());
		//
		return correctRate;
	}
}
