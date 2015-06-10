package cn.edu.xmu.dm.d3c.metrics;

import java.util.List;

public class ClassifierDiversity {
	// 计算两个分类器之间的不一致度量
	public static double CalculateDis(List<Integer> first, List<Integer> second) {
		double Dis = 0;
		int i;
		int diffNum = 0;
		for (i = 0; i < first.size(); i++) {
			if (second.size() > i) {
				if (first.get(i) != second.get(i)) {
					diffNum = diffNum + 1;
				}
			} else {
				break;
			}
		}
		Dis = (double) diffNum / (double) first.size();
		return Dis;
	}

	// 计算多个分类器之间的κ度量
	public static double CalculateK(List<Integer>[] classifyResult) {

		int L = classifyResult.length;
		int N = 0;
		
		//TOFIX avoid the condition that classifyResult[0].size() is 0
		for (int ii = 0; ii < classifyResult.length; ii++) {
			if (classifyResult[ii].size() != 0) {
				N = classifyResult[ii].size();
			}
		}
		
		int i, j;
		int num = 0;
		double Dis = 0;
		double p;
		double k;
		// 计算DISav
		for (i = 0; i < L - 1; i++) {
			for (j = i + 1; j < L; j++) {
				Dis = Dis + CalculateDis(classifyResult[i], classifyResult[j]);
			}
		}
		Dis = (Dis * 2) / (double) (L * (L - 1));
		// 计算平均准确率
		for (i = 0; i < classifyResult.length; i++) {
			//
			for (j = 0; j < classifyResult[i].size(); j++) {
				//
				if (classifyResult[i].get(j) == 1) {
					num = num + 1;
				}
			}
		}
		p = (double) num / (double) (L * N);
		// 计算κ度量
		k = 1 - Dis / (2 * p * (1 - p));
		// System.out.println(k);
		return k;
	}
}
