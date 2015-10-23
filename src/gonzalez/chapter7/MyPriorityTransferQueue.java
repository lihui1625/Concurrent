package gonzalez.chapter7;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MyPriorityTransferQueue<E> extends PriorityBlockingQueue<E>
		implements TransferQueue<E> {
 

	public static void main(String[] args) throws Exception {

		MyPriorityTransferQueue<Event> buffer = new MyPriorityTransferQueue<Event>();

		Producer producer = new Producer(buffer);
		Thread producerThreads[] = new Thread[10];
		for (int i = 0; i < producerThreads.length; i++) {
			producerThreads[i] = new Thread(producer);
			producerThreads[i].start();
		}

		Consumer consumer = new Consumer(buffer);
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();

		System.out.printf("Main: Buffer: Consumer count: %d\n",
				buffer.getWaitingConsumerCount());

		Event myEvent = new Event("Core Event", 0);
		buffer.transfer(myEvent);
		System.out.printf("Main: My Event has ben transfered.\n");

		for (int i = 0; i < producerThreads.length; i++) {
			try {
				producerThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		TimeUnit.SECONDS.sleep(1);

		System.out.printf("Main: Buffer: Consumer count: %d\n",
				buffer.getWaitingConsumerCount());

		myEvent = new Event("Core Event 2", 0);
		buffer.transfer(myEvent);

		consumerThread.join();

		System.out.printf("Main: End of the program\n");
	}

	private AtomicInteger counter;

	private LinkedBlockingQueue<E> transfered;

	private ReentrantLock lock;

	public MyPriorityTransferQueue() {
		counter = new AtomicInteger(0);
		lock = new ReentrantLock();
		transfered = new LinkedBlockingQueue<E>();
	}

	@Override
	public boolean tryTransfer(E e) {
		lock.lock();
		boolean value;
		if (counter.get() == 0) {
			value = false;
		} else {
			put(e);
			value = true;
		}
		lock.unlock();
		return value;
	}

	@Override
	public void transfer(E e) throws InterruptedException {
		lock.lock();
		if (counter.get() != 0) {
			put(e);
			lock.unlock();
		} else {
			transfered.add(e);
			lock.unlock();
			synchronized (e) {
				e.wait();
			}
		}
	}

	@Override
	public boolean tryTransfer(E e, long timeout, TimeUnit unit)
			throws InterruptedException {
		lock.lock();
		if (counter.get() != 0) {
			put(e);
			lock.unlock();
			return true;
		} else {
			transfered.add(e);
			long newTimeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
			lock.unlock();
			e.wait(newTimeout);
			lock.lock();

			if (transfered.contains(e)) {
				transfered.remove(e);
				lock.unlock();
				return false;
			} else {
				lock.unlock();
				return true;
			}
		}
	}

	@Override
	public boolean hasWaitingConsumer() {
		return (counter.get() != 0);
	}

	@Override
	public int getWaitingConsumerCount() {
		return counter.get();
	}

	@Override
	public E take() throws InterruptedException {
		lock.lock();
		counter.incrementAndGet();

		E value = transfered.poll();
		if (value == null) {
			lock.unlock();
			value = super.take();
			lock.lock();

		} else {
			synchronized (value) {
				value.notify();
			}
		}

		counter.decrementAndGet();
		lock.unlock();
		return value;
	}
}

class Event implements Comparable<Event> {

	private String thread;

	private int priority;

	public Event(String thread, int priority) {
		this.thread = thread;
		this.priority = priority;
	}

	public String getThread() {
		return thread;
	}

	public int getPriority() {
		return priority;
	}

	public int compareTo(Event e) {
		if (this.priority > e.getPriority()) {
			return -1;
		} else if (this.priority < e.getPriority()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class Producer implements Runnable {

	private MyPriorityTransferQueue<Event> buffer;

	public Producer(MyPriorityTransferQueue<Event> buffer) {
		this.buffer = buffer;
	}

	public void run() {
		for (int i = 0; i < 100; i++) {
			Event event = new Event(Thread.currentThread().getName(), i);
			buffer.put(event);
		}
	}
}

class Consumer implements Runnable {

	private MyPriorityTransferQueue<Event> buffer;

	public Consumer(MyPriorityTransferQueue<Event> buffer) {
		this.buffer = buffer;
	}

	@Override
	public void run() {
		for (int i = 0; i < 1002; i++) {
			try {
				Event value = buffer.take();
				System.out.printf("Consumer: %s: %d\n", value.getThread(),
						value.getPriority());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
