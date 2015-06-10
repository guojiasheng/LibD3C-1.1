package cn.edu.xmu.dm.d3c.selectivealgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.core.Instances;
import cn.edu.xmu.dm.d3c.metrics.ClassifierDiversity;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class HCRR {
	/*
	 * 爬山策略（重复随机） train为训练集 cfsArray为全部候选分类器
	 * classifyResult为每个分类器对训练集单独训练得到的结果（0表示错误，1表示正确），用于计算差异度
	 * correctRateArray为每个分类器单独训练时候得到的正确率
	 * initCorrectRate为初始的目标正确率，在循环集成中随着每次循环不断递减（对循环集成起效）
	 * currentResult为当前一轮循环得到的结果
	 * (currentRersult.get[0]表示差异性度量值，currentResult.get[
	 * 1]表示正确率，currentResult.get[2]为得到的分类器的数量，对循环集成起效)
	 * ClassifierNo为当前一轮循环得到的分类器的编号序列
	 */
	public static double doHCRR(
			Instances train, 
			double initCorrectRate,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo, 
			List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances,
			List<Double> currentResult, 
			List<Integer> ClassifierNo) {

		double diversity;		// 差异性（差异性度量是κ度量，κ值越小，差异性越大）
		double tempDiversity;
		double correctRate;		// 正确率
		double voteCorrectRate;	// vote集成得到的正确率

		int numOfClassifiers = classifyRightOrWrong.size();
		int count = numOfClassifiers;	// 循环次数(循环次数为分类器的个数)
		int i = 0, j, k;
		int r;
		int maxNo;
		Random random = new Random();
		
		if (ClassifierNo.size() == 0) {	// 如果列表为空，说明是第一轮循环，那么就将正确率最高的分类器选为第一个分类器,
			diversity = currentResult.get(0);
			boolean bestBegin = false;
			if (bestBegin == false) {
				r = random.nextInt(numOfClassifiers);
				correctRate = correctRateArray.get(r);
				ClassifierNo.add(r);
			} else {
				correctRate = Collections.max(correctRateArray);
				maxNo = correctRateArray.indexOf(correctRate);
				ClassifierNo.add(maxNo);
			}
		} else {						//否则就只要将正确率替换成前一轮循环得到的正确率
			diversity = currentResult.get(0);
			correctRate = currentResult.get(1);
		}
		
		while (i < count) {
			if (correctRate >= initCorrectRate) {
				break;
			} else {
				// 随机产生一个分类器的编号，然后将得到该分类器和之前选择的分类器的分类结果一起计算差异性
				r = random.nextInt(numOfClassifiers);
				List<Integer>[] tempList = new List[ClassifierNo.size() + 1];
				for (k = 0; k < ClassifierNo.size(); k++) {
					tempList[k] = classifyRightOrWrong.get(ClassifierNo.get(k));
				}
				tempList[ClassifierNo.size()] = classifyRightOrWrong.get(r);
				tempDiversity = ClassifierDiversity.CalculateK(tempList);
				// κ度量的值越小，说明差异性越大
				if (tempDiversity <= diversity) {
					ClassifierNo.add(r);
					// 用来存放各种算法的正确率
					List<Double> newCorrectRateArray = new ArrayList<Double>();
					// 用来存放各种算法的概率分布
					List<List<double[]>> newClassifyDistributeForInstances = new ArrayList<List<double[]>>();
					for (j = 0; j < ClassifierNo.size(); j++) {
						newCorrectRateArray.add(correctRateArray
								.get(ClassifierNo.get(j)));
						
						newClassifyDistributeForInstances
						.add(classifyDistributeForInstances
								.get(ClassifierNo.get(j)));
					}
					voteCorrectRate = D3CVoter.probabilityVote(train,
							newCorrectRateArray,
							newClassifyDistributeForInstances);

					//System.out.println(voteCorrectRate);

					// 如果当前集成得到的正确率比之前的正确率高，那么就将差异性和正确率替换成该轮的值，否则将最后一个分类器编号删除(新加的分类器编号)
					if (voteCorrectRate > correctRate) {
						diversity = tempDiversity;
						correctRate = voteCorrectRate;
						// setBestMatrixString(tempMatrixString);
						// setBestClassDetailsString(tempClassDetailsString);
					} else {
						ClassifierNo.remove(ClassifierNo.size() - 1);
					}
				}
				i++;
			}
		}
		// 说明只有一个分类器，差异性为0
		if (diversity == Double.MAX_VALUE) {
			diversity = 0;
		}
		// 对于循环集成，每一次循环结束必须记录当前的差异性、正确率和所选分类器个数，对于所选择的分类编号训练由ClassifierNo保存
		currentResult.clear();
		currentResult.add(diversity);
		currentResult.add(correctRate);
		currentResult.add((double) ClassifierNo.size());
		return correctRate;
	}
}
