package cn.edu.xmu.dm.d3c.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import cn.edu.xmu.dm.d3c.utils.InitClassifiers;
import cn.edu.xmu.dm.d3c.utils.InstanceUtil;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;

import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.LibD3C;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;

import weka.core.Instance;
import weka.core.Instances;

public class imDC1 {

	public Instances m_instances = null;
	public static Classifier[] bestClassifiers;// 选择bestClassifierNum个优秀的分类器
	public int lessLabelNum = 0;// 少数类的数目
	public static double lessLabel = 0;// 少数类的类别

	public void getBestClassifier(Instances data) throws Exception {
		m_instances = data;
		int numInstances = m_instances.numInstances();
		int numAttributes = m_instances.numAttributes();// 属性数目
		Instances trainData = new Instances(m_instances, numInstances);// 抽取少数类和同等数量的大类组成的数据集
		m_instances = data;
		// start 获得少数类类别，数量等信息
		System.out.println("获取最小类类别，实例数......");
		double[] labelArray = m_instances
				.attributeToDoubleArray(numAttributes - 1);
		Hashtable<Double, Integer> labelHashTable = new Hashtable();
		double key = 0;
		int value = 0;
		for (int i = 0; i <= labelArray.length - 1; i++) {
			key = labelArray[i];
			if (labelHashTable.containsKey(key)) {
				value = labelHashTable.get(key) + 1;
				labelHashTable.put(key, value);
			} else {
				labelHashTable.put(key, 1);
			}
		}
		Enumeration<Double> keys = labelHashTable.keys();
		key = keys.nextElement();
		lessLabelNum = labelHashTable.get(key);
		lessLabel = key;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			if (labelHashTable.get(key) < lessLabelNum) {
				lessLabelNum = labelHashTable.get(key);
				lessLabel = key;
			}
		}

		System.out.println("最小类类别为：" + lessLabel + ",数量为：" + lessLabelNum);
		// end
		// start 先在trainData加入生成最小类，抽取同等数量的最大类加入trainData
		System.out.println("准备测试最优分类器的抽取样本......");
		Instances instanceMore = new Instances(m_instances, numInstances);
		for (int i = numInstances - 1; i >= 0; i--) {
			if (m_instances.instance(i).classValue() == lessLabel) {
				trainData.add(m_instances.instance(i));
			} else {
				instanceMore.add(m_instances.instance(i));
			}
		}
		Random r = new Random();
		for (int i = 0; i < lessLabelNum; i++) {
			int randomNum = r.nextInt(instanceMore.numInstances() - 1);
			trainData.add(instanceMore.instance(randomNum));
		}
		Classifier[] classifiers;
		List<String> nameOfClassifiers = new ArrayList<String>();
		List<String> pathOfClassifiers = new ArrayList<String>();
		List<String> parameterOfCV = new ArrayList<String>();
		classifiers = InitClassifiers.init("classifiers.xml",
				nameOfClassifiers, pathOfClassifiers, parameterOfCV);
		bestClassifiers = classifiers;
	}

	public static double query(double[] preres) {
		double max = 0;
		int index = 0;

		for (int i = 0; i < preres.length; i++) {
			if (preres[i] > max) {
				max = preres[i];
				index = i;
			}
		}
		return index;
	}

	public static void main(String[] args) throws Exception {
		// TrainFilePath -l 1 -c cvNum or TrainFilePath -l 1 -p TestFilePath
		// resultFilePath

		// String
		// filePath="D:/Workspaces/MyEclipse 10/CCDM/data/task2_train.arff";
		// String lessLabelParameter = "1";
		InstanceUtil iu = new InstanceUtil();
		Instances input = iu.getInstances("D://train.arff");
		input.setClassIndex(input.numAttributes() - 1);

		imDC1 imdc = new imDC1();
		// imdc.getFileInstances("D://train.arff");
		// imdc.getBestClassifier("1");
		/*
		myClassifier myclassifier = new myClassifier(imdc.m_instances,
				imdc.bestClassifiers, imdc.lessLabelNum, imdc.lessLabel,
				imdc.bestClassifierNum);
		System.out.println("初始化集成分类算法（包括初始权重，迭代分类器设置）......");
		myclassifier.initmyclassifier();

		System.out.println("开始预测样本......");
		Evaluation eval = new Evaluation(imdc.m_instances);
		eval.crossValidateModel(myclassifier, input, 5, new Random());
		System.out.println(eval.toSummaryString());
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
		*/
	}

	// 使用测试集验证,当测试集有类别时验证算法性能
	/*
	 * myclassifier.buildClassifier(imdc.m_instances); FileReader frData = new
	 * FileReader(TestFilePath); Instances test_instances = new
	 * Instances(frData);
	 * test_instances.setClassIndex(test_instances.numAttributes() - 1); double
	 * result; int TP=0,FN=0,TN=0,FP=0; System.out.println("开始预测样本......");
	 * for(int i=0;i<test_instances.numInstances();i++){
	 * result=query(myclassifier
	 * .distributionForInstance(test_instances.instance(i)));
	 * if(test_instances.instance(i).classValue()==imdc.lessLabel){
	 * if(result==test_instances.instance(i).classValue()){//少数and正确,TP TP++;
	 * }else{//少数and错误,FN FN++; } }else{
	 * if(result==test_instances.instance(i).classValue()){//多数and正确,TN TN++;
	 * }else{//多数and错误,FP FP++; } } } System.out.println(TN+" "+FP);
	 * System.out.println(FN+" "+TP); System.out.println("sn:"+1.0*TP/(TP+FN));
	 * System.out.println("sp:"+1.0*TN/(FP+TN));
	 */

}
