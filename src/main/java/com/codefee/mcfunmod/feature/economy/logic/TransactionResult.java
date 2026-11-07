package com.codefee.mcfunmod.feature.economy.logic;

/**
 * Immutable outcome of an attempted economy transaction.
 *
 * @param success         whether the transaction changed the account balance
 * @param status          detailed outcome status
 * @param previousBalance balance before the operation
 * @param newBalance      balance after the operation (identical to previousBalance if failed)
 * @param amount          the requested amount
 * @param reason          context description for audit trails
 */
public record TransactionResult(
		boolean success,
		TransactionStatus status,
		long previousBalance,
		long newBalance,
		long amount,
		String reason
) {
	public static TransactionResult success(long previousBalance, long newBalance, long amount, String reason) {
		return new TransactionResult(true, TransactionStatus.SUCCESS, previousBalance, newBalance, amount, reason);
	}

	public static TransactionResult insufficientFunds(long currentBalance, long requestedAmount, String reason) {
		return new TransactionResult(false, TransactionStatus.INSUFFICIENT_FUNDS, currentBalance, currentBalance, requestedAmount, reason);
	}

	public static TransactionResult invalidAmount(long currentBalance, long requestedAmount, String reason) {
		return new TransactionResult(false, TransactionStatus.INVALID_AMOUNT, currentBalance, currentBalance, requestedAmount, reason);
	}

	public static TransactionResult overflow(long currentBalance, long requestedAmount, String reason) {
		return new TransactionResult(false, TransactionStatus.OVERFLOW, currentBalance, currentBalance, requestedAmount, reason);
	}
}
