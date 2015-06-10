package cn.edu.xmu.dm.d3c.threadpool;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClassifiersTrainingExecutor extends ThreadPoolExecutor {

	public ClassifiersTrainingExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		startTimes = new ConcurrentHashMap<String, Date>();
	}

	@Override
	public void shutdown() {
		System.out.printf("MyExecutor: Going to shutdown.\n");
		System.out.printf("MyExecutor: Executed tasks:%d\n",
				getCompletedTaskCount());
		System.out.printf("MyExecutor: Running tasks:%d\n", getActiveCount());
		System.out.printf("MyExecutor: Pending tasks:%d\n", getQueue().size());
		super.shutdown();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		System.out.printf("MyExecutor: A task is beginning: %s :%s\n",
				t.getName(), r.hashCode());
		startTimes.put(String.valueOf(r.hashCode()), new Date());
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		Future<?> result = (Future<?>) r;
		try {
			System.out.printf("*********************************\n");
			System.out.printf("MyExecutor: A task is finishing.\n");
//			System.out.printf("MyExecutor: Result: %s\n", result.get());
			System.out.printf("MyExecutor: queueSize: %d\n", this.getQueue().size());
			Date startDate = startTimes.remove(String.valueOf(r.hashCode()));
			Date finishDate = new Date();
			long diff = finishDate.getTime() - startDate.getTime();
			System.out.printf("MyExecutor: Duration: %d\n", diff);
			System.out.printf("*********************************\n");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public ConcurrentHashMap<String, Date> startTimes;
	
//
//	public static void main(String[] args) {
//		
//		ClassifiersTrainingExecutor myExecutor = new ClassifiersTrainingExecutor(6, 16, 1000,
//				TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
//		
//		List<Future<String>> results = new ArrayList<Future<String>>();
//		
//		for (int i = 0; i < 10; i++) {
//			ClassifierTrainingTask task = new ClassifierTrainingTask();
//			Future<String> result = myExecutor.submit(task);
//			results.add(result);
//		}
//
//		System.out.println("submit finished...");
//		for (int i = 0; i < 7; i++) {
//			try {
//				String result = results.get(i).get();
//				System.out.printf("Main: Result for Task %d :%s\n", i, result);
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		System.out.println("first 5 tasks finished...");
//		System.out.println("myExecutor shutdown...");
//		myExecutor.shutdown();
//
//		for (int i = 7; i < 10; i++) {
//			try {
//				String result = results.get(i).get();
//				System.out.printf("Main: Result for Task %d :%s\n", i, result);
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		try {
//			myExecutor.awaitTermination(1, TimeUnit.DAYS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		System.out.printf("Main: End of the program.\n");
//
//	}

}
