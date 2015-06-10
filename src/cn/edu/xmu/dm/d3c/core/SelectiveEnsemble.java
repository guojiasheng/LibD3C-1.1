package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import weka.classifiers.Classifier;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.Instances;
import weka.core.SerializedObject;
import weka.core.Utils;
import cn.edu.xmu.dm.d3c.threadpool.ClassifierTrainingTask;
import cn.edu.xmu.dm.d3c.threadpool.ClassifiersTrainingExecutor;

public class SelectiveEnsemble {

	public List<Classifier> IndependentTrain(Instances input,
			Classifier[] cfsArray, double validatePercent, int numSlots, int timeOut,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo, List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances, Random random,
			List<Integer> qqs, List<String> pathOfClassifiers,
			List<String> parameterOfCV) throws Exception {

		int i, j, k;

		input.randomize(random);

		Instances validation = new Instances(input, 0, (int)(Math.ceil(validatePercent * input.numInstances())));

		Instances train = input;

		int max = input.numInstances();
		int min = 0;

		Random randomIns = new Random();

		List<Integer> ls = new ArrayList<Integer>();
		
		for (i = 0; i < (int) ((validatePercent) * input.numInstances()); i++) {
			int s = random.nextInt(max) % (max - min + 1) + min;
			ls.add(s);
		}
		
		for (i = 0; i < input.numInstances(); i++) {
			if (ls.contains(i)) {
				train.remove(i);
			}
		}

		int processorNum = Runtime.getRuntime().availableProcessors();
		
		ClassifiersTrainingExecutor myExecutor = new ClassifiersTrainingExecutor(
				numSlots, processorNum, 200, TimeUnit.SECONDS,
				new LinkedBlockingDeque<Runnable>());

		List<Future<Classifier>> results = new ArrayList<Future<Classifier>>();

		List<Classifier> lcfs = Arrays.asList(cfsArray);
		// Collections.shuffle(lcfs);

		for (i = 0; i < lcfs.size(); i++) {
			ClassifierTrainingTask task = new ClassifierTrainingTask(input,
					train, validation, lcfs.get(i), classifyRightOrWrong,
					classifyErrorNo, correctRateArray,
					classifyDistributeForInstances, i, qqs, pathOfClassifiers,
					parameterOfCV);
			Future<Classifier> result = null;
			result = myExecutor.submit(task);

			results.add(result);
		}

		List<Classifier> bcfs = new ArrayList<Classifier>();
	
		for (Future<Classifier> f : results) {
			try {
				f.get(timeOut, TimeUnit.MINUTES);
			} catch (Exception ex) {
//				ex.printStackTrace();
				f.cancel(true);
			}
		}
		
		
		myExecutor.shutdownNow();
		
//		myExecutor.shutdown();
//		 while (!myExecutor.awaitTermination(50, TimeUnit.SECONDS)) {
//		 myExecutor.shutdownNow();
//		 System.out.println("shutdown classifiers training...");
//		 break;
//		 }

		for (Future<Classifier> f : results) {
			if (f.isCancelled())
				continue;
			bcfs.add(f.get());
		}

		System.out.println("基分类器单独训练完成!");

		return bcfs;
	}

	// 交叉验证模型用于输出单独训练的概率分布信息
	public Classifier CrossValidationModelForDistribute(Instances input,
			Instances train, Instances validation, Classifier classifier,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo, List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances, int index,
			List<Integer> qqs, List<String> pathOfClassifiers,
			List<String> parameterOfCV) {

		Classifier copiedClassifier = null;
		try {
			//
			int i, j, k;
			//
			input = new Instances(input);
			//
			List<Integer> single_classifyRightOrWrong = new ArrayList<Integer>();
			List<Integer> single_classifyErrorNo = new ArrayList<Integer>();
			double correctRate;
			List<double[]> single_classifyDistributeForInstances = new ArrayList<double[]>();

			//
			CVParameterSelection cvps = new CVParameterSelection();
			//
			// if (!parameterOfCV.get(index).equals("")) {
			// //
			// copiedClassifier = (Classifier) new SerializedObject(
			// classifier).getObject();
			// //
			// Random random = new Random(1000);
			// //
			// String[] options = new String[2];
			// options[0] = "-P";
			// options[1] = parameterOfCV.get(index);
			// //
			// cvps.setOptions(options);
			// cvps.setNumFolds(5);
			// cvps.setSeed(1);
			// cvps.setClassifier(copiedClassifier);
			// cvps.buildClassifier(input);
			// eval1.crossValidateModel(cvps, input, 5, random);
			// }
			//
			double numOfError = 0;
			//
			if (!parameterOfCV.get(index).equals("")) {
				//
				copiedClassifier = ((Classifier) Utils.forName(
						Classifier.class, pathOfClassifiers.get(index),
						cvps.getBestClassifierOptions()));

			} else {
				//
				copiedClassifier = (Classifier) new SerializedObject(classifier)
						.getObject();
			}

			copiedClassifier.buildClassifier(train);
			//
			for (j = 0; j < validation.numInstances(); j++) {
				// 样本的实际类别
				double real_class = validation.instance(j).classValue();
				// 样本的预测类别
				double predict_class = copiedClassifier
						.classifyInstance(validation.instance(j));
				// 添加当前分类器对于一条样本预测的概率分布
				double[] distribute = copiedClassifier
						.distributionForInstance(validation.instance(j));
				single_classifyDistributeForInstances.add(distribute);
				//
				if (real_class != predict_class) {
					//
					numOfError++;
					//
					single_classifyRightOrWrong.add(0);
				} else if (real_class == predict_class) {
					//
					single_classifyRightOrWrong.add(1);
				}
			}

			// 获得当前分类器的样本正确错误的情况
			classifyRightOrWrong.add(single_classifyRightOrWrong);

			// 获得当前分类器的错分样本号
			for (i = 0; i < single_classifyRightOrWrong.size(); i++) {
				//
				if (single_classifyRightOrWrong.get(i) == 0) {
					//
					single_classifyErrorNo.add(i);
				}
			}
			classifyErrorNo.add(single_classifyErrorNo);

			// 获得当前分类器的正确率
			correctRate = ((double) input.numInstances() - numOfError)
					/ (double) input.numInstances();

			//
			correctRateArray.add(correctRate);

			// 获得当前分类器对于每一条样本的概率分布
			classifyDistributeForInstances
					.add(single_classifyDistributeForInstances);

			//
			// qqs.add(new ClassifierIndex(index, correctRate));
			qqs.add(index);

			//
//			System.out.println(pathOfClassifiers.get(index) + "		"
//					+ correctRate);
			// outBuff.append(index + "	" + (1 - eval1.errorRate())+'\n');
		} catch (Exception e) {
			//
//			System.out.println(pathOfClassifiers.get(index) + "		" + "error");
			//
			// e.printStackTrace();
			// outBuff.append(index + "	" + "error"+'\n');
		}
		return copiedClassifier;
	}
}
