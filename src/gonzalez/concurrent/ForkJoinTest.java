package gonzalez.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest extends RecursiveTask<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7271380058701683949L;

	private static final int THRESHOLD = 2;// 阈值

	private int start;

	private int end;

	public ForkJoinTest(int start, int end) {

		this.start = start;

		this.end = end;

	}

	@Override
	protected Integer compute() {

		int sum = 0;

		// 如果任务足够小就计算任务

		boolean canCompute = (end - start) <= THRESHOLD;

		if (canCompute) {
			System.out.println(Thread.currentThread().getName() + " start : " + start + "  end:" + end);
			for (int i = start; i <= end; i++) {

				sum += i;

			}

		} else {

			// 如果任务大于阀值，就分裂成两个子任务计算

			int middle = (start + end) / 2;

			ForkJoinTest leftTask = new ForkJoinTest(start, middle); 

			ForkJoinTest rightTask = new ForkJoinTest(middle + 1, end);

			// 执行子任务

			leftTask.fork();

			rightTask.fork();

			// 等待子任务执行完，并得到其结果

			int leftResult = leftTask.join();

			int rightResult = rightTask.join();

			// 合并子任务

			sum = leftResult + rightResult;

		}

		return sum;

	}

	public static void main(String[] args) {

		ForkJoinPool forkJoinPool = new ForkJoinPool();

		// 生成一个计算任务，负责计算1+2+3+4

		ForkJoinTest task = new ForkJoinTest(1, 400);

		// 执行一个任务

		ForkJoinTask<Integer> result = forkJoinPool.submit(task);

		try {

			System.out.println(result.get());

		} catch (InterruptedException e) {

		} catch (ExecutionException e) {

		}

	}

}