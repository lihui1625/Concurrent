package gonzalez.chapter6;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongTest {

	public static void main(String[] args) {
		Account account = new Account();
		account.setBalance(1000);

		Company company = new Company(account);
		Thread companyThread = new Thread(company);

		Bank bank = new Bank(account);
		Thread bankThread = new Thread(bank);

		System.out.printf("Account : Initial Balance: %d\n",
				account.getBalance());

		companyThread.start();
		bankThread.start();

		try {
			companyThread.join();
			bankThread.join();
			System.out.printf("Account : Final Balance: %d\n",
					account.getBalance());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}

class Account {

	private AtomicLong balance;

	public Account() {
		balance = new AtomicLong();
	}

	public long getBalance() {
		return balance.get();
	}

	public void setBalance(long balance) {
		this.balance.set(balance);
	}

	public void addAmount(long amount) {
		this.balance.getAndAdd(amount);
	}

	public void subtractAmount(long amount) {
		this.balance.getAndAdd(-amount);
	}
}

class Company implements Runnable {

	private Account account;

	public Company(Account account) {
		this.account = account;
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			account.addAmount(1000);
		}
	}
}

class Bank implements Runnable {

	private Account account;

	public Bank(Account account) {
		this.account = account;
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			account.subtractAmount(1000);
		}
	}

}
