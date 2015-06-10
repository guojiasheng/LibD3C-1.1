package cn.edu.xmu.dm.d3c.clustering;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import weka.classifiers.rules.DecisionTableHashKey;
import weka.clusterers.NumberOfClustersRequestable;
import weka.clusterers.RandomizableClusterer;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WeightedInstancesHandler;


public class KMeanz extends RandomizableClusterer implements NumberOfClustersRequestable, WeightedInstancesHandler {

	private static final long serialVersionUID = 1L;
	
	private int m_NumClusters=0;
	private Instances m_ClusterCentroids;
	protected DistanceFunction m_DistanceFunction=new EuclideanDistance();	// 默认使用欧拉距离
	private boolean m_PreserveOrder=true;
	private int m_Iterations=0;
	private int m_MaxIterations=100;

	private int[] m_ClusterSizes;
	
	public KMeanz(int numClusters){
		super();
		m_SeedDefault=10;
		this.m_NumClusters = numClusters;
		setSeed(m_SeedDefault);
	}
	public void buildClusterer(Instances data){	
		
	}
	
	/**
	 * @param data 训练数据
	 * @param chooseClassifiers	保存筛选之后的分类器
	 * @param correctRateArray	保存筛选后的分类器的分类正确率
	 * @throws Exception
	 */
	public void buildClusterer(Instances data,List<Integer> chooseClassifiers,List<Double> correctRateArray)throws Exception{
		
		m_Iterations=0;
		Instances instances=new Instances(data);
		m_ClusterCentroids=new Instances(instances,m_NumClusters);

		int[] clusterAssignments=new int[instances.numInstances()];
		m_DistanceFunction.setInstances(instances);
		Random RandomO=new Random(getSeed());
		int instIndex;
		HashMap initC=new HashMap();
		DecisionTableHashKey hk=null;
		Instances initInstances=null;
		if(m_PreserveOrder)
			initInstances=new Instances(instances);
		else
			initInstances=instances;
		
		//随机获得起始的聚类中心
		for(int j=initInstances.numInstances()-1;j>=0;j--){
			instIndex=RandomO.nextInt(j+1);
			hk=new DecisionTableHashKey(initInstances.instance(instIndex),initInstances.numAttributes(),true);
			if(!initC.containsKey(hk)){
				m_ClusterCentroids.add(initInstances.instance(instIndex));
				initC.put(hk, null);
			}
			initInstances.swap(j, instIndex);
			
			if(m_ClusterCentroids.numInstances()==m_NumClusters){
				break;
			}
		}
		
		//TOFIX reset the m_NumClusters
		
		m_NumClusters = m_ClusterCentroids.numInstances();
		
		initInstances=null;
		int i;
		boolean converged=false;
		int emptyClusterCount;
		
		Instances[] tempI=new Instances[m_NumClusters];
		
		while(!converged){
			emptyClusterCount=0;
			m_Iterations++;
			converged=true;
			
			// 分别对每个样本计算到M个聚类中心的聚类，选择距离最近的聚类中心作为该样本的聚类类别
			for(i=0;i<instances.numInstances();i++){
				Instance toCluster=instances.instance(i);
				int newC=clusterProcessedInstance(toCluster);
				if(newC!=clusterAssignments[i]){
					converged=false;
				}
				clusterAssignments[i]=newC;
			}
			//改变聚类中心
			m_ClusterCentroids=new Instances(instances,m_NumClusters);
			
			for(i=0;i<m_NumClusters;i++){
				tempI[i]=new Instances(instances,0);
			}
			for(i=0;i<instances.numInstances();i++){
				tempI[clusterAssignments[i]].add(instances.instance(i));
			}
			for(i=0;i<m_NumClusters;i++){
				if(tempI[i].numInstances()==0){
					emptyClusterCount++;
				}else{
					moveCentroid(i,tempI[i]);
				}
			}
			
			if(emptyClusterCount>0){
				m_NumClusters-=emptyClusterCount;
				if(converged){
					Instances[] t=new Instances[m_NumClusters];
					int index=0;
					for(int k=0;k<tempI.length;k++){
						if(tempI[k].numInstances()>0){
							t[index++]=tempI[k];
						}
					}
					tempI=t;
				}else{
					tempI=new Instances[m_NumClusters];
				}
			}
			if(m_Iterations==m_MaxIterations)
				converged=true;
		}
		m_ClusterSizes=new int[m_NumClusters];
		for(i=0;i<m_NumClusters;i++){
			m_ClusterSizes[i]=tempI[i].numInstances();
		}
		selectClassifier(clusterAssignments,chooseClassifiers,correctRateArray);
	}
	
	// 从每个聚类中心中挑选分类正确率最高的分类器参与下一次循环集成选择
	public  void selectClassifier(int[] clusterAssignments,List<Integer> chooseClassifiers,List<Double> correctRateArray){
		int i,j;
		double correctRate;
		int chooseID=0;
		
		for(i=0;i<m_NumClusters;i++){
			correctRate=0;
			for(j=0;j<clusterAssignments.length;j++){
				if(clusterAssignments[j]==i){
					if(correctRate<correctRateArray.get(j)){
						correctRate=correctRateArray.get(j);
						chooseID=j;
					}
				}
			}
			chooseClassifiers.add(chooseID);
		}
	}
	
	//移动聚类中心
	protected double[] moveCentroid(int centroidIndex,Instances members/*,boolean updateClusterInfo*/){
		double[] vals=new double[members.numAttributes()];
		
		for(int j=0;j<members.numAttributes();j++){
			if(m_DistanceFunction instanceof EuclideanDistance || members.attribute(j).isNominal()){
				vals[j]=members.meanOrMode(j);
			}
		}
		m_ClusterCentroids.add(decideCentroid(vals,members));
		return vals;
	} 
	
	public Instance decideCentroid(double[] vals,Instances members){
		Instance inst=new DenseInstance(vals.length);
		int q;
		for(q=0;q<vals.length;q++){
			inst.setValue(q, vals[q]);
		}
		
		double minDistance=Double.MAX_VALUE;
		double tempDistance;
		int instanceID=0;
		int i;
		for(i=0;i<members.numInstances();i++){
			tempDistance=myDistance(inst,members.instance(i));
			if(tempDistance<minDistance ){
				minDistance=tempDistance;
				instanceID=i;
			}
		}
		return members.instance(instanceID);
	}
	
	//确定实例属于哪一个聚类
	private int clusterProcessedInstance(Instance instance){
		double minDist=Integer.MAX_VALUE;
		int bestCluster=0;
		
		for(int i=0;i<m_NumClusters;i++){
			double dist = 0;
			try {
				dist=myDistance(instance,m_ClusterCentroids.instance(i));
			}catch (IndexOutOfBoundsException ie) {
				ie.printStackTrace();
			}
			if(dist<minDist){
				minDist=dist;
				bestCluster=i;
			}
		}
		return bestCluster;
	}
	
	//根据新距离公式得到每个实例之间的距离
	protected  double myDistance(Instance first,Instance second){
		int i;
		int errorIntersect=0;
		for(i=0;i<first.numAttributes();i++){
			if((first.value(i)==second.value(i))&&(first.value(i)==0)){
				++errorIntersect;
			}
		}
		
//		// 以下两种方式计算结果一致
		return 1/(double)errorIntersect;
	}
	
	//获得聚类个数
	public int numberOfClusters()throws Exception{
		return m_NumClusters;
	}
	
	//设置聚类数
	public void setNumClusters(int n)throws Exception{
		if(n<=0){
			throw new Exception("Number of clusters must be > 0");
		}
		m_NumClusters=n;
	}
}
