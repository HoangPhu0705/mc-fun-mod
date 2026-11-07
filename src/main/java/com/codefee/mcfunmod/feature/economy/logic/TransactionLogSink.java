package com.codefee.mcfunmod.feature.economy.logic;

/**
 * Consumer sink for transaction audit records.
 */
@FunctionalInterface
public interface TransactionLogSink {
	void record(TransactionLogEntry entry);
}
