package cn.edu.xmu.dm.d3c.core;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.core.Instances;
import cn.edu.xmu.dm.d3c.metrics.ClassifierDiversity;
import cn.edu.xmu.dm.d3c.selectivealgorithms.DS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EBSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.EFSS;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCNRR;
import cn.edu.xmu.dm.d3c.selectivealgorithms.HCRR;
import cn.edu.xmu.dm.d3c.voters.D3CVoter;

public class CircleCombine {
	
	//第一个分类器是否为最优
	private boolean bestBegin=true;
	public String tempMatrixString="";
	public String bestMatrixString="";
	public String tempClassDetailsString="";
	public String bestClassDetailsString="";
	
	//循环集成框架
	public static double doCircleCombine(
			Instances train,
			double initCorrectRate, 
			double interval,
			List<List<Integer>> classifyRightOrWrong,
			List<List<Integer>> classifyErrorNo,
			List<Double> correctRateArray,
			List<List<double[]>> classifyDistributeForInstances,
			List<Double> currentResult,
			List<Integer> ClassifierNo,
			String CCAlgorithm)throws Exception{
		
		//Logger logger = Logger.getLogger(SeletiveAlgorithm.class);
		//PropertyConfigurator.configure("log4j.properties");
		
		List<Integer> OptimalNo=new ArrayList();
		List<Double> optimalResult=new ArrayList();//用来存放全部最优的结果,optimalResult.get[0]表示差异性,optimalResult.get[1]表示正确率

		optimalResult.add(Double.MAX_VALUE);
		optimalResult.add((double)0);
		
		int circle=0;
		int i;
		int position;
		double maxCorrectRate;
		
		DecimalFormat df = new DecimalFormat("0.00000");
	
		while(initCorrectRate>=0){
			if(CCAlgorithm.equals("HCNRR"))
				HCNRR.doHCNRR(
						train, 
						initCorrectRate, 
						classifyRightOrWrong, 
						classifyErrorNo,
						correctRateArray, 
						classifyDistributeForInstances, 
						currentResult,
						ClassifierNo);
			else if(CCAlgorithm.equals("HCRR"))
				HCRR.doHCRR(
						train, 
						initCorrectRate, 
						classifyRightOrWrong, 
						classifyErrorNo,
						correctRateArray, 
						classifyDistributeForInstances, 
						currentResult,
						ClassifierNo);
			else if(CCAlgorithm.equals("EBSS"))
				EBSS.doEBSS(
						train, 
						initCorrectRate, 
						classifyRightOrWrong, 
						classifyErrorNo,
						correctRateArray, 
						classifyDistributeForInstances, 
						currentResult,
						ClassifierNo);
			else if(CCAlgorithm.equals("EFSS"))
				EFSS.doEFSS(
						train, 
						initCorrectRate, 
						classifyRightOrWrong, 
						classifyErrorNo,
						correctRateArray, 
						classifyDistributeForInstances, 
						currentResult,
						ClassifierNo);
			else throw new Exception ("Could not find selective algorithm:"+CCAlgorithm);
			
			if(currentResult.get(1)>optimalResult.get(1)){
				optimalResult.clear();
				optimalResult.add(currentResult.get(0));
				optimalResult.add(currentResult.get(1));
				OptimalNo.clear();
				OptimalNo.addAll(ClassifierNo);
			}
			
			//String strClassifierNo = "[";
			if(optimalResult.get(1)>=initCorrectRate){
				break;
			}else{
				initCorrectRate=initCorrectRate-interval;	
			}
			circle++;
			//strClassifierNo += "]";
			//logger.info(strClassifierNo);
		}
		return optimalResult.get(1);
		
	} 
	
	//特别的循环集成框架
	public double CC(Instances train,Classifier[] cfsArray,List<Integer> D,List<Double> correctRateArray,double initCorrectRate, double interval,List<Double> currentResult,List<Integer> ClassifierNo){
		int circle=0;
		double correctRate=0;
		while(initCorrectRate>=0){
			System.out.println("Circle:"+circle);
			correctRate=DS.doDS(train,cfsArray,D,correctRateArray,initCorrectRate,currentResult,ClassifierNo);
			if(correctRate>=initCorrectRate){
				System.out.print(initCorrectRate+"	");
				System.out.print(correctRate+"	");
				System.out.println("ClassifierNo:"+ClassifierNo);
				break;
			}else{
				System.out.print(initCorrectRate+"	");
				System.out.print(correctRate+"	");
				System.out.println("ClassifierNo:"+ClassifierNo);
				initCorrectRate=initCorrectRate-interval;
			}
			circle++;
		}
		
		return correctRate;
	}	
	
	public void setBestBegin(boolean bb){
		bestBegin=bb;
	}
	public void setTempMatrixString(String str){
		tempMatrixString=str;
	}
	public void setBestMatrixString(String str){
		bestMatrixString=str;
	}
	public void setTempClassDetailsString(String str){
		tempClassDetailsString=str;
	}
	public void setBestClassDetailsString(String str){
		bestClassDetailsString=str;
	}
}



