package com.codefee.mcfunmod.feature.economy.logic;

/**
 * Pure Java economy account model for Eldrin currency with balance safety,
 * overflow checking, and immutable transaction results.
 */
public class EconomyAccount {
	/**
	 * Maximum allowed balance in Eldrin for an account to prevent arithmetic overflows.
	 */
	public static final long MAX_BALANCE = 1_000_000_000_000_000L; // 1 quadrillion Eldrin

	private long balance;

	public EconomyAccount() {
		this(0L);
	}

	public EconomyAccount(long initialBalance) {
		this.balance = Math.clamp(initialBalance, 0L, MAX_BALANCE);
	}

	public long getBalance() {
		return balance;
	}

	public boolean has(long amount) {
		return amount >= 0 && balance >= amount;
	}

	/**
	 * Safely deposits the given amount of Eldrin into this account.
	 *
	 * @param amount amount to deposit (must be positive)
	 * @param reason context/audit reason
	 * @return transaction result
	 */
	public TransactionResult deposit(long amount, String reason) {
		if (amount <= 0) {
			return TransactionResult.invalidAmount(balance, amount, reason);
		}
		if (MAX_BALANCE - balance < amount) {
			return TransactionResult.overflow(balance, amount, reason);
		}

		long previous = balance;
		balance += amount;
		return TransactionResult.success(previous, balance, amount, reason);
	}

	/**
	 * Safely withdraws the given amount of Eldrin from this account if funds are sufficient.
	 *
	 * @param amount amount to withdraw (must be positive)
	 * @param reason context/audit reason
	 * @return transaction result
	 */
	public TransactionResult withdraw(long amount, String reason) {
		if (amount <= 0) {
			return TransactionResult.invalidAmount(balance, amount, reason);
		}
		if (balance < amount) {
			return TransactionResult.insufficientFunds(balance, amount, reason);
		}

		long previous = balance;
		balance -= amount;
		return TransactionResult.success(previous, balance, amount, reason);
	}

	/**
	 * Sets the account balance to a specified non-negative amount of Eldrin.
	 *
	 * @param newBalance target balance (must be between 0 and MAX_BALANCE)
	 * @param reason     context/audit reason
	 * @return transaction result
	 */
	public TransactionResult setBalance(long newBalance, String reason) {
		if (newBalance < 0) {
			return TransactionResult.invalidAmount(balance, newBalance, reason);
		}
		if (newBalance > MAX_BALANCE) {
			return TransactionResult.overflow(balance, newBalance, reason);
		}

		long previous = balance;
		balance = newBalance;
		return TransactionResult.success(previous, balance, newBalance, reason);
	}

	/**
	 * Safely transfers Eldrin from one account to another atomically.
	 *
	 * @param from   source account
	 * @param to     destination account
	 * @param amount amount to transfer (must be positive)
	 * @param reason context/audit reason
	 * @return transaction result from the perspective of the source account
	 */
	public static TransactionResult transfer(EconomyAccount from, EconomyAccount to, long amount, String reason) {
		if (amount <= 0) {
			return TransactionResult.invalidAmount(from.balance, amount, reason);
		}
		if (from.balance < amount) {
			return TransactionResult.insufficientFunds(from.balance, amount, reason);
		}
		if (MAX_BALANCE - to.balance < amount) {
			return TransactionResult.overflow(to.balance, amount, reason);
		}

		from.balance -= amount;
		to.balance += amount;
		return TransactionResult.success(from.balance + amount, from.balance, amount, reason);
	}
}
