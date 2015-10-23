package gonzalez.chapter5;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class MyForkJoinPool4 {

	public static void main(String[] args) {

		int array[] = new int[100];
		MyTask task = new MyTask(array, 0, 100);
		ForkJoinPool pool = new ForkJoinPool();

		pool.execute(task);

		pool.shutdown();

		try {
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (task.isCompletedAbnormally()) {
			System.out.printf("Main: An exception has ocurred\n");
			System.out.printf("Main: %s\n", task.getException());
		}
		System.out.printf("Main: Result: %d", task.join());

	}
}

class MyTask extends RecursiveTask<Integer> {

	private int array[];
	private int start, end;

	public MyTask(int array[], int start, int end) {
		this.array = array;
		this.start = start;
		this.end = end;
	}

	@Override
	protected Integer compute() {
		System.out.printf("Task: Start from %d to %d\n", start, end);
		if (end - start < 10) {
			if ((3 > start) && (3 < end)) {
				throw new RuntimeException("This task throws an"
						+ "Exception: Task from " + start + " to " + end);
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			int mid = (end + start) / 2;
			MyTask task1 = new MyTask(array, start, mid);
			MyTask task2 = new MyTask(array, mid, end);
			invokeAll(task1, task2);
		}
		System.out.printf("Task: End form %d to %d\n", start, end);
		return 0;

	}
}