package com.codefee.mcfunmod.feature.economy.logic;

/**
 * Outcome status of a transaction attempt.
 */
public enum TransactionStatus {
	SUCCESS,
	INSUFFICIENT_FUNDS,
	INVALID_AMOUNT,
	OVERFLOW
}
