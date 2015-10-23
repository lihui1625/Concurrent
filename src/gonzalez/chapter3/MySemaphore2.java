package gonzalez.chapter3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MySemaphore2 {

	public static void main(String args[]) {

		MySemaphore2 printQueue = new MySemaphore2();

		Thread thread[] = new Thread[10];
		for (int i = 0; i < 10; i++) {
			thread[i] = new Thread(new MultiJob(printQueue), "Thread" + i);
		}

		for (int i = 0; i < 10; i++) {
			thread[i].start();
		}
	}

	private final Semaphore semaphore;

	// 1. 如我们之前提到的，你将实现semaphores来修改print queue例子。打开PrintQueue类并声明一个boolean
	// array名为 freePrinters。这个array储存空闲的等待打印任务的和正在打印文档的printers。
	private boolean freePrinters[];

	// 2. 接着，声明一个名为lockPrinters的Lock对象。将要使用这个对象来保护freePrinters array的访问。
	private Lock lockPrinters;

	// 3. 修改类的构造函数并初始化新声明的对象们。freePrinters array
	// 有3个元素，全部初始为真值。semaphore用3作为它的初始值。
	public MySemaphore2() {

		semaphore = new Semaphore(3);
		freePrinters = new boolean[3];

		for (int i = 0; i < 3; i++) {
			freePrinters[i] = true;
		}
		lockPrinters = new ReentrantLock();
	}

	// 4. 修改printJob()方法。它接收一个称为document的对象最为唯一参数。
	public void printJob(Object document) {

		// 5. 首先，调用acquire()方法获得semaphore的访问。由于此方法会抛出
		// InterruptedException异常，所以必须加入处理它的代码。
		try {
			semaphore.acquire();

			// 6. 接着使用私有方法 getPrinter()来获得被安排打印任务的打印机的号码。
			int assignedPrinter = getPrinter();

			// 7. 然后， 随机等待一段时间来实现模拟打印文档的行。
			long duration = (long) (Math.random() * 10);
			System.out
					.printf("%s: PrintQueue: Printing a Job in Printer%d during %d seconds\n",
							Thread.currentThread().getName(), assignedPrinter,
							duration);
			TimeUnit.SECONDS.sleep(duration);

			// 8. 最后，调用release() 方法来解放semaphore并标记打印机为空闲，通过在对应的freePrinters
			// array引索内分配真值。
			freePrinters[assignedPrinter] = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
	}

	 
	private int getPrinter() {

		// 10. 首先，声明一个int变量来保存printer的引索值。
		int ret = -1;

		// 11. 然后， 获得lockPrinters对象 object的访问。
		try {
			lockPrinters.lock();

			// 12. 然后，在freePrinters
			// array内找到第一个真值并在一个变量中保存这个引索值。修改值为false，因为等会这个打印机就会被使用。
			for (int i = 0; i < freePrinters.length; i++) {
				if (freePrinters[i]) {
					ret = i;
					freePrinters[i] = false;
					break;
				}
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lockPrinters.unlock();
		}
		return ret;
	}
}

class MultiJob implements Runnable {
	private MySemaphore2 printQueue;

	public MultiJob(MySemaphore2 printQueue) {
		this.printQueue = printQueue;
	}

	@Override
	public void run() {

		System.out.printf("%s: Going to print a job\n", Thread.currentThread()
				.getName());

		printQueue.printJob(new Object());

		System.out.printf("%s: The document has been printed\n", Thread
				.currentThread().getName());
	}
}
