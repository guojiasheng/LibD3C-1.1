package cn.edu.xmu.dm.d3c.selectivealgorithms;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;
import cn.edu.xmu.dm.d3c.metrics.ClassifierDiversity;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class EBSS {
	// 集成后序选择
	public static double doEBSS(Instances train, double initCorrectRate,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo, List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances,
			List<Double> currentResult, List<Integer> ClassifierNo) {

		double diversity;
		double tempDiversity = Double.MAX_VALUE;
		double correctRate;
		double voteCorrectRate;
		List<Double> tempResult = new ArrayList();
		
		int i = 0, j, k, r;
		int index;
		int numOfClassifiers = classifyRightOrWrong.size();
		int threshold = 2 * numOfClassifiers;
		double num;
		/*
		 * 第一阶段 如果已选的分类器的个数为0，说明是第一次进行后序选择，那么将全部的分类作为候选分类器加入列表中，并计算此时的差异性和正确率，
		 * 否则说明已经经过至少1次的循环，那么首先将差异性和正确率替换成前一次的结果，然后判断此时是否满足正确率阈值。
		 * 如果不满足，则将全部分类器一个一个的进入列表，同时记录目前为止最大的差异性和对应的分类器的数量（分类器数量是不超过阈值的）。
		 * 如果分类器数量超过阈值，那么列表中的分类器减少到之前差异性最大时候的分类器数量。
		 * 之前的操作完成之后，候选分类器已经确定，那么计算此时的正确率，进入第二阶段。
		 */
		if (ClassifierNo.size() == 0) {
			List<Integer>[] temp = new List[numOfClassifiers];
			for (j = 0; j < numOfClassifiers; j++) {
				temp[j] = classifyRightOrWrong.get(j);
			}
			diversity = ClassifierDiversity.CalculateK(temp);
			correctRate = D3CVoter.probabilityVote(train, correctRateArray,
					classifyDistributeForInstances);

			currentResult.clear();
			currentResult.add(diversity);
			currentResult.add(correctRate);
			currentResult.add((double) numOfClassifiers);
			for (j = 0; j < numOfClassifiers; j++) {
				ClassifierNo.add(j);
			}
		} else {
			diversity = currentResult.get(0);
			correctRate = currentResult.get(1);
			if (correctRate >= initCorrectRate) {
				return correctRate;
			}
			tempResult.add(diversity);
			tempResult.add(currentResult.get(2));
			for (j = 0; j < numOfClassifiers; j++) {
				List<Integer>[] tempList = new List[ClassifierNo.size() + 1];
				for (k = 0; k < ClassifierNo.size(); k++) {
					tempList[k] = classifyRightOrWrong.get(ClassifierNo.get(k));
				}
				tempList[ClassifierNo.size()] = classifyRightOrWrong.get(j);
				//
				tempDiversity = ClassifierDiversity.CalculateK(tempList);
				//
				ClassifierNo.add(j);
				// 差异度量κ越小，说明差异性越大，进行集成的效果可能更好
				if ((tempDiversity < tempResult.get(0))
						&& (ClassifierNo.size() <= threshold)) {
					//
					tempResult.clear();
					tempResult.add(tempDiversity);
					tempResult.add((double) ClassifierNo.size());
				}
			}
			diversity = tempDiversity;
			// 分类器数量超过阈值
			if (ClassifierNo.size() > threshold) {
				while (ClassifierNo.size() != tempResult.get(1)) {
					ClassifierNo.remove(ClassifierNo.size() - 1);
				}
				diversity = tempResult.get(0);
			}
			// 用来存放各种算法的正确率
			List<Double> newCorrectRateArray = new ArrayList<Double>();
			// 用来存放各种算法的概率分布
			List<List<double[]>> newClassifyDistributeForInstances = new ArrayList<List<double[]>>();
			for (k = 0; k < ClassifierNo.size(); k++) {
				newCorrectRateArray.add(correctRateArray.get(ClassifierNo
						.get(k)));
				newClassifyDistributeForInstances
						.add(classifyDistributeForInstances.get(ClassifierNo
								.get(k)));
			}

			correctRate = D3CVoter.probabilityVote(train, newCorrectRateArray,
					newClassifyDistributeForInstances);

			//System.out.println(correctRate);
		}
		// setBestMatrixString(tempMatrixString);
		// setBestClassDetailsString(tempClassDetailsString);
		/*
		 * 第二阶段 本程序的后序选择的删除操作，是根据分类器的类型（0~19）进行。
		 * 首先查找对应类型分类器（从0开始）在列表中的位置，如果返回值是-1，说明给类型的分类器在列表中不存在，那么就查找下一种分类器类型。
		 * 如果返回的不为-1，那么就将对应位置上的分类器屏蔽，然后计算正确率，如果此时的正确率高于之前的正确率，那么就将该位置上的分类器从
		 * 列表中删除，并将正确率替换成最新计算得到的正确率。
		 */
		r = 0;
		while (r != numOfClassifiers) {
			if (correctRate >= initCorrectRate) {
				break;
			} else {
				index = ClassifierNo.indexOf(r);
				if (index != -1) {
					j = 0;
					List<Integer>[] tempList = new List[ClassifierNo.size() - 1];
					for (k = 0; k < ClassifierNo.size(); k++) {
						if (k != index) {
							tempList[j] = classifyRightOrWrong.get(ClassifierNo
									.get(k));
							j++;
						}
					}
					
					System.out.println("---: " + tempList.length);
					tempDiversity = ClassifierDiversity.CalculateK(tempList);
					if (tempDiversity <= diversity) {

						List<Double> newCorrectRateArray = new ArrayList<Double>();
						List<List<double[]>> newClassifyDistributeForInstances = new ArrayList<List<double[]>>();
						j = 0;
						for (k = 0; k < ClassifierNo.size(); k++) {
							if (k != index) {
								newCorrectRateArray.add(j, correctRateArray
										.get(ClassifierNo.get(k)));
								newClassifyDistributeForInstances.add(j,
										classifyDistributeForInstances
												.get(ClassifierNo.get(k)));
								j++;
							}
						}
						voteCorrectRate = D3CVoter.probabilityVote(train,
								newCorrectRateArray,
								newClassifyDistributeForInstances);
						//System.out.println(voteCorrectRate);
						if (voteCorrectRate >= correctRate) {
							ClassifierNo.remove(index);
							diversity = tempDiversity;
							correctRate = voteCorrectRate;
							// setBestMatrixString(tempMatrixString);
							// setBestClassDetailsString(tempClassDetailsString);
						}
					}
				}
				r++;
			}
		}
		// 说明只有一个分类器，差异性为0
		if (diversity == Double.MAX_VALUE) {
			diversity = 0;
		}
		currentResult.clear();
		currentResult.add(diversity);
		currentResult.add(correctRate);
		currentResult.add((double) ClassifierNo.size());
		//System.out.println(correctRate);

		return correctRate;
	}
}
