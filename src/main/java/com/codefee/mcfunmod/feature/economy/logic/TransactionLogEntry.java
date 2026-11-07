package com.codefee.mcfunmod.feature.economy.logic;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable log entry representing an audit record of an Eldrin economy transaction.
 *
 * @param timestamp       time of transaction
 * @param playerId        UUID of the target player (null if system-wide)
 * @param playerName      last known player name (for quick readability)
 * @param type            transaction type
 * @param amount          amount in Eldrin
 * @param previousBalance balance before the transaction
 * @param newBalance      balance after the transaction
 * @param status          transaction outcome
 * @param reason          reason or source tag (e.g., "quest:goblin_camp", "shop_buy:mana_staff")
 */
public record TransactionLogEntry(
		Instant timestamp,
		UUID playerId,
		String playerName,
		TransactionType type,
		long amount,
		long previousBalance,
		long newBalance,
		TransactionStatus status,
		String reason
) {
	public TransactionLogEntry {
		if (timestamp == null) {
			timestamp = Instant.now();
		}
		if (reason == null) {
			reason = "unspecified";
		}
		if (playerName == null) {
			playerName = playerId != null ? playerId.toString() : "system";
		}
	}

	public String toLogString() {
		return String.format("[%s] [%s] player=%s (uuid=%s) amount=%d Eldrin before=%d after=%d status=%s reason=\"%s\"",
				timestamp,
				type,
				playerName,
				playerId != null ? playerId : "none",
				amount,
				previousBalance,
				newBalance,
				status,
				reason);
	}
}
