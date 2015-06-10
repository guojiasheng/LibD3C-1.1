package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class myClassifier
		implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private Instances m_instances = null;
	private Instances less_instances = null;
	private Instances more_instances = null;
	private int bestClassifierNum = imDC1.bestClassifiers.length;// 迭代分类器数量
	private Classifier[] bestClassifiers = new Classifier[bestClassifierNum];// 选择bestClassifierNum个优秀的分类器
	private int iterationNum = imDC1.bestClassifiers.length;// 迭代分类器的数目
	private Classifier[] iterationClassifiers = null;;// 迭代分类器
	public static double[] iterationWeight;// 迭代分类器权重
	private int lessLabelNum = 0;// 少数类的数目
	private double lessLabel = 0;// 少数类的类别
	private double[] weight;// 样本权重数组
	private int[] flag;// 样本标记数组
	protected List<Classifier> m_preBuiltClassifiers = new ArrayList();
	public myClassifier(Instances m_instances, Classifier[] bestClassifiers,
			int lessLabelNum, double lessLabel, int bestClassifierNum) {
		this.m_instances = m_instances;
		this.bestClassifiers = bestClassifiers;
		this.lessLabelNum = lessLabelNum;
		this.lessLabel = lessLabel;
		this.bestClassifierNum = bestClassifierNum;
	}

	public myClassifier() {
		super();
	}

	// 初始化，包括初始化权重
	public void initmyclassifier() throws Exception {
		less_instances = new Instances(m_instances, m_instances.numInstances());
		more_instances = new Instances(m_instances, m_instances.numInstances());
		int numInstances = m_instances.numInstances();
		double moreWeight = 1.0;
		weight = new double[numInstances - lessLabelNum];
		flag = new int[numInstances - lessLabelNum];
		int temp = 0;
		for (int i = numInstances - 1; i >= 0; i--) {
			if (m_instances.instance(i).classValue() == lessLabel) {
				less_instances.add(m_instances.instance(i));

			} else {
				more_instances.add(m_instances.instance(i));
				weight[temp] = moreWeight;
				temp++;
			}
		}

		iterationClassifiers = new Classifier[iterationNum];
		iterationWeight = new double[iterationNum];
		for (int i = 0; i < iterationNum; i++) {
			iterationClassifiers[i] = bestClassifiers[i];
		}
	}

	public String getRevision() {
		return ("");
	}

	public Classifier[] build(Instances data) throws Exception {
		System.out.println("buildClassifier......");
		int num_more = 0, num_more_wrong = 0;
		int num_less = 0, num_less_wrong = 0;
		double instanceResult;
		double instanceReal;
		for (int i = 0; i < iterationNum; i++) {
			// start 创建分类器并用第i个分类器对所有实例预测，获得标志，错误信息等
			buildClassifierWithWeights(i);
			for (int j = 0; j < more_instances.numInstances(); j++) {
				instanceResult = iterationClassifiers[i]
						.classifyInstance(more_instances.instance(j));
				instanceReal = more_instances.instance(j).classValue();
				if (instanceResult == instanceReal) {// 多数类and预测正确
					flag[j] = 1;
					num_more++;
				} else {// 多数类and预测错误
					flag[j] = 2;
					num_more++;
					num_more_wrong++;
				}
			}
			for (int j = 0; j < less_instances.numInstances(); j++) {
				instanceResult = iterationClassifiers[i]
						.classifyInstance(less_instances.instance(j));
				instanceReal = less_instances.instance(j).classValue();
				if (instanceResult == instanceReal) {// 少数类and预测正确
					num_less++;
				} else {// 少数类and预测错误
					num_less++;
					num_less_wrong++;
				}
			}
			// end
			// start 重新设置样本权重，设置迭代分类器权重
			setWeights(num_more, num_more_wrong);
			// iterationWeight[i] = 1.0 * (num_more - num_more_wrong + num_less
			// - num_less_wrong) / num_more + num_less;
			// 本来基分类器权重为两类分类精度的几何平均
			double g = (1.0 * (num_more - num_more_wrong) / num_more + 1.0
					* (num_less - num_less_wrong) / num_less) / 2;
			// double g = 1.0*(num_less - num_less_wrong) /num_less;
			iterationWeight[i] = g;
			
			// end
		}
		return iterationClassifiers;
		
	}
/*
	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] probs = this.m_Classifiers.length > 0 ? getClassifier(0)
				.distributionForInstance(instance)
				: ((Classifier) this.m_preBuiltClassifiers.get(0))
						.distributionForInstance(instance);

		for (int i = 1; i < this.m_Classifiers.length; i++) {
			double[] dist = getClassifier(i).distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] += dist[j];
			}
		}

		int index = this.m_Classifiers.length > 0 ? 0 : 1;
		for (int i = index; i < this.m_preBuiltClassifiers.size(); i++) {
			double[] dist = ((Classifier) this.m_preBuiltClassifiers.get(i))
					.distributionForInstance(instance);
			for (int j = 0; j < dist.length; j++) {
				probs[j] += dist[j];
			}
		}

		for (int j = 0; j < probs.length; j++) {
			probs[j] /= (this.m_Classifiers.length + this.m_preBuiltClassifiers
					.size());
		}
		return probs;
	}
*/
	protected void buildClassifierWithWeights(int iteration) throws Exception {
		Instances trainData = selectWeightQuantile();
		for (int i = 0; i < less_instances.numInstances(); i++) {
			trainData.add(less_instances.instance(i));
		}
		iterationClassifiers[iteration].buildClassifier(trainData);
	}

	protected Instances selectWeightQuantile() {
		Instances trainData = new Instances(more_instances,
				more_instances.numInstances());
		Random r = new Random();
		double sum = 0;
		int index = 0;
		int number = 0;
		HashMap map_Data = new HashMap();
		for (int j = 0; j < weight.length; j++) {
			map_Data.put(String.valueOf(j),
					String.valueOf((r.nextInt(100) / 100.0) * weight[j]));
		}
		List<Map.Entry<String, String>> list_Data = new ArrayList<Map.Entry<String, String>>(
				map_Data.entrySet());
		Collections.sort(list_Data,
				new Comparator<Map.Entry<String, String>>() {
					public int compare(Map.Entry<String, String> o1,
							Map.Entry<String, String> o2) {
						if (o2.getValue() != null && o1.getValue() != null
								&& o2.getValue().compareTo(o1.getValue()) > 0) {
							return 1;
						} else if (o2.getValue() != null
								&& o1.getValue() != null
								&& o2.getValue().compareTo(o1.getValue()) == 0) {
							return 0;
						} else {
							return -1;
						}

					}
				});
		Iterator<Entry<String, String>> iterator = list_Data.iterator();
		while (iterator.hasNext() && number <=2*lessLabelNum) {
			index = Integer.parseInt(iterator.next().getKey());
		    trainData.add(more_instances.instance(index));
		   
			number++;
		}
		return trainData;
	}

	protected void setWeights(int num_more, int num_more_wrong) {
		double w = 0;
		double weightSum = 0;
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 1) {
				w = 0;
			} else {
				w = (num_more_wrong + 1) / num_more;
			}
			weight[i] = weight[i] + w;
			weightSum += weight[i];
		}
		for (int i = 0; i < weight.length; i++) {
			weight[i] = weight[i] / weightSum;
		}
	}
}