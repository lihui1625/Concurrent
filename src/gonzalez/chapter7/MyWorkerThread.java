package gonzalez.chapter7;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class MyWorkerThread extends ForkJoinWorkerThread {

	private static ThreadLocal<Integer> taskCounter = new ThreadLocal<Integer>();

	protected MyWorkerThread(ForkJoinPool pool) {
		super(pool);
	}

	@Override
	protected void onStart() {
		super.onStart();
		System.out.printf("MyWorkerThread %d: Initializing task counter.\n",
				getId());
		taskCounter.set(0);
	}

	@Override
	protected void onTermination(Throwable exception) {
		System.out
				.printf("MyWorkerThread %d: %d\n", getId(), taskCounter.get());
		super.onTermination(exception);
	}

	public void addTask() {
		int counter = taskCounter.get().intValue();
		counter++;
		taskCounter.set(counter);
	}
	
	public static void main(String[] args) throws Exception {
		MyWorkerThreadFactory factory=new MyWorkerThreadFactory();
		ForkJoinPool pool=new ForkJoinPool(4, factory, null, false);
		int array[]=new int[100000];
		for (int i=0; i<array.length; i++){
		array[i]=1;
		}
		MyRecursiveTask task=new MyRecursiveTask(array,0,array.length);
		pool.execute(task);
		task.join();
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.DAYS);
		System.out.printf("Main: Result: %d\n",task.get()); 
	}

}

class MyWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		return new MyWorkerThread(pool);
	}

}

class MyRecursiveTask extends RecursiveTask<Integer> {

	private int array[];
	private int start, end;

	public MyRecursiveTask(int array[], int start, int end) {
		this.array = array;
		this.start = start;
		this.end = end;
	}

	@Override
	protected Integer compute() {
		Integer ret = 0;
		MyWorkerThread thread = (MyWorkerThread) Thread.currentThread();
		thread.addTask();
		return ret;
	}

	private Integer addResults(Task task1, Task task2) {
		int value;
		try {
			value =   task1.get().intValue() + task2.get().intValue();
		} catch (Exception e) {
			e.printStackTrace();
			value = 0;
		}
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return value;
	} 
}
