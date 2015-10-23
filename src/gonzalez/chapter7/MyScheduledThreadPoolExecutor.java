package gonzalez.chapter7;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

	public MyScheduledThreadPoolExecutor(int corePoolSize) {
		super(corePoolSize);
	}

	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
			RunnableScheduledFuture<V> task) {
		MyScheduledTask<V> myTask = new MyScheduledTask<V>(runnable, null,
				task, this);
		return myTask;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		ScheduledFuture<?> task = super.scheduleAtFixedRate(command,
				initialDelay, period, unit);
		MyScheduledTask<?> myTask = (MyScheduledTask<?>) task;
		myTask.setPeriod(TimeUnit.MILLISECONDS.convert(period, unit));
		return task;
	}

	public static void main(String[] args) throws Exception {

		MyScheduledThreadPoolExecutor executor = new MyScheduledThreadPoolExecutor(
				2);

		Task task = new Task();
		System.out.printf("Main: %s\n", new Date());

		executor.schedule(task, 1, TimeUnit.SECONDS);

		TimeUnit.SECONDS.sleep(3);

		task = new Task();
		System.out.printf("Main: %s\n", new Date());

		executor.scheduleAtFixedRate(task, 1, 3, TimeUnit.SECONDS);

		TimeUnit.SECONDS.sleep(10);

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);

		System.out.printf("Main: End of the program.\n");
	}

}

class MyScheduledTask<V> extends FutureTask<V> implements
		RunnableScheduledFuture<V> {

	private RunnableScheduledFuture<V> task;

	private ScheduledThreadPoolExecutor executor;

	private long period;

	private long startDate;

	public MyScheduledTask(Runnable runnable, V result,
			RunnableScheduledFuture<V> task,
			ScheduledThreadPoolExecutor executor) {
		super(runnable, result);
		this.task = task;
		this.executor = executor;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		if (!isPeriodic()) {
			return task.getDelay(unit);
		} else {
			if (startDate == 0) {
				return task.getDelay(unit);
			} else {
				Date now = new Date();
				long delay = startDate - now.getTime();
				return unit.convert(delay, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public int compareTo(Delayed o) {
		return task.compareTo(o);
	}

	@Override
	public boolean isPeriodic() {
		return task.isPeriodic();
	}

	@Override
	public void run() {
		if (isPeriodic() && (!executor.isShutdown())) {
			Date now = new Date();
			startDate = now.getTime() + period;
			executor.getQueue().add(this);
		}

		System.out.printf("Pre-MyScheduledTask: %s\n", new Date());
		System.out.printf("MyScheduledTask: Is Periodic:%s\n", isPeriodic());
		super.runAndReset();
		System.out.printf("Post-MyScheduledTask: %s\n", new Date());
	}

	public void setPeriod(long period) {
		this.period = period;
	}
}

class Task implements Runnable {

	@Override
	public void run() {
		System.out.printf("Task: Begin.\n");
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.printf("Task: End.\n");
	}

	public Integer get() {
		// TODO Auto-generated method stub
		return null;
	}
 
}