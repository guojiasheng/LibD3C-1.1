package cn.edu.xmu.dm.d3c.threadpool;

/**
 * desc:轮询各个分类器线程是否结束
 * <code>ThreadListener</code>
 * @author chenwq (chenwq@stu.xmu.edu.cn)
 * @version 1.0 2012/04/10
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EnsemblingThreadListener extends Thread {
	//
	public static boolean isOver = false;
	//
	public List<EnsemblingThread> array = new ArrayList<EnsemblingThread>();
	public List<Double> correctRate = new ArrayList<Double>();
	public List<String> strategies = new ArrayList<String>();
	public List<List<Integer>> ClassifierNos = new ArrayList<List<Integer>>();
	//
	long sleepTime = 1000;
	//
	public void run() {
		//
		//Logger logger = Logger.getLogger(ThreadListener.class);
		//
		//PropertyConfigurator.configure("log4j.properties");
		//
		boolean flag = false;
		//
		long startTime = System.currentTimeMillis();
		//
		while (!flag) {
			//
			boolean isRemoved = false;
			
			//System.err.println("当前线程个数: " + array.size());
			try {
				
				// 每隔一定时间监听一次各个文件统计线程
				Thread.sleep(sleepTime);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < array.size(); i++) {
				//
				if (array.get(i).isFinished()) {
					
					// 判断统计文件的线程是否已经完成
					//System.out.println(InitClassifiers.classifiersName[array.get(i).getI()] + " is finished!");
					Calendar c = Calendar.getInstance();
//					c.setTimeInMillis(array.get(i).getExecuteTime() - startTime);
					//System.out.println(InitClassifiers.classifiersName[array.get(i).getI()]+ " runs "+ c.get(Calendar.SECOND)+ "s");
					//
					//logger.info(InitClassifiers.classifiersName[array.get(i).getI()] + " is finished!");
					//logger.info(InitClassifiers.classifiersName[array.get(i).getI()] + " runs " + c.get(Calendar.SECOND) + "s\n");
					//
					this.correctRate.add(array.get(i).getCurrentCorrectRate());
					this.ClassifierNos.add(array.get(i).getClassifierNo());
					this.strategies.add(array.get(i).getStrategy());
					array.remove(i);// 将已经完成的线程对象从队列中移除
					//
					isRemoved = true;
				}
				//
				//
				if (!isRemoved) {
					long t2 = System.currentTimeMillis();
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(t2 - startTime);
					if (c.get(Calendar.SECOND) >= 10) {
						//
						array.get(i).stop();
						
						// 将已经完成的线程对象从队列中移除
						array.remove(i);

						//System.out.println(InitClassifiers.classifiersName[array.get(i).getI()] + " is removed!");
						//logger.info(InitClassifiers.classifiersName[array.get(i).getI()] + " is removed!");
					}
				}
			}
			//
			if (array.size() == 0) {
				
				// 如果统计线程都已经完成
				flag = true;
				EnsemblingThreadListener.isOver = true;
			}
		}
	}
}
