package cn.edu.xmu.dm.d3c.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import cn.edu.xmu.dm.d3c.utils.InstanceUtil;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

public class imDC {

	Instances[] data;
	int lessLabelNum = 0;// ���������Ŀ
	double lessLabel = 0;// ����������
	Instances train;
	
	
	Instances wrong;
	public void splitData(Instances m_instances) {
		 wrong=new Instances(m_instances,0);
		int numInstances = m_instances.numInstances();
		int numAttributes = m_instances.numAttributes();// ������Ŀ
		Instances trainData = new Instances(m_instances, numInstances);// ��ȡ�������ͬ�������Ĵ�����ɵ����ݼ�

		// start ��������������������Ϣ
		System.out.println("��ȡ��С�����ʵ����......");
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
		ArrayList<Double> lableClass = new ArrayList<Double>();
		Enumeration<Double> keys = labelHashTable.keys();
		key = keys.nextElement();
		lessLabelNum = labelHashTable.get(key);
		lableClass.add(key);
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			lableClass.add(key);
			if (labelHashTable.get(key) < lessLabelNum) {
				lessLabelNum = labelHashTable.get(key);
				lessLabel = key;
			}
		}
		System.out.println(lableClass);
		System.out.println("��С�����Ϊ��" + lessLabel + ",����Ϊ��" + lessLabelNum);
		// end
		// start ����trainData����������С�࣬��ȡͬ����������������trainData
		System.out.println("׼���������ŷ������ĳ�ȡ����......");

		data = new Instances[labelHashTable.size()];
		for (int k = 0; k < data.length; k++) {
			data[k] = new Instances(m_instances, numInstances);

		}

		int index;
		double x;
		for (int i = numInstances - 1; i >= 0; i--) {
			for (int j = 0; j < lableClass.size(); j++) {
				if (m_instances.instance(i).classValue() == lableClass.get(j)) {
					x = lableClass.get(j);
					index = (int) x;
					data[index].add(m_instances.instance(i));
					break;
				}
			}
		}
		m_instances = null;
	}

	protected void setWeight() {
		for (int i = 0; i < data.length; i++) {
			for (Instance x : data[i]) {
				x.setWeight(1.0);
			}
		}
	}

	protected void selectWeightQuantile() {
		train = new Instances(data[0], lessLabelNum*data.length);
		for (int i = 0; i < data.length; i++) {
			if (i == (int)lessLabel)
				continue;
			Random r = new Random();
			double sum = 0;
			int index = 0;
			int number = 0;
			data[i].randomize(r);
			while (number<data[i].size()&& number < lessLabelNum) {
				train.add(data[i].instance(number));
				// System.out.println(m_instances.instance(index));
				number++;
			}
			
			for(int j=0;j<data[(int)lessLabel].size();j++)
			{
				train.add(data[(int)lessLabel].instance(j));
			}
		//	System.out.println(wrong.size());
			
			for(int j=0;j<wrong.size();j++)
			{
				train.add(wrong.instance(j));
			}
		}
		// return trainData;
	}

	public void train(Classifier classfier,Instances data) throws Exception
	{
		wrong=new Instances(train,0);
		System.out.println("buildClassifier......");
		int num_more = 0, num_more_wrong = 0;
		int num_less = 0, num_less_wrong = 0;
		double instanceResult;
		double instanceReal;
			// start �������������õ�i��������������ʵ��Ԥ�⣬��ñ�־��������Ϣ��
		classfier.buildClassifier(data);
		for (int j = 0; j < data.numInstances(); j++) {
			instanceResult = classfier.classifyInstance(data.instance(j));
			instanceReal = data.instance(j).classValue();
			if (instanceResult != instanceReal) {// ������andԤ����ȷ
				wrong.add(data.instance(j));
			}
		}

	}
	
	public void build(Classifier []classifiers,Instances data) throws Exception
	{
		splitData(data);
		setWeight();
		for(int i=0;i<classifiers.length;i++)
		{
			selectWeightQuantile();
			train(classifiers[i],train);
			
		}
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		InstanceUtil iu = new InstanceUtil();
		Instances input = iu.getInstances("D://bupa.arff");
		input.setClassIndex(input.numAttributes() - 1);
		imDC im = new imDC();
		ArrayList<Classifier> m_Classifiers = new ArrayList<Classifier>();
	 	//m_Classifiers[0]=new Logistic();
    	//m_Classifiers[1]=new SMO();
    //	m_Classifiers[2]=new J48();
	//	im.build(m_Classifiers,input);
		
		
	}

}
