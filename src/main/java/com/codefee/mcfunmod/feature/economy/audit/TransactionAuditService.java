package com.codefee.mcfunmod.feature.economy.audit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.codefee.mcfunmod.McFunMod;
import com.codefee.mcfunmod.feature.economy.logic.TransactionLogEntry;
import com.codefee.mcfunmod.feature.economy.logic.TransactionLogSink;

/**
 * Service responsible for recording and querying economy transaction audit logs.
 *
 * <p>Persists transactions to {@code logs/mcfunmod/economy_transactions.log} and keeps
 * a fast in-memory ring buffer of recent transactions for in-game admin auditing.
 */
public class TransactionAuditService implements TransactionLogSink {
	private static final int MAX_MEMORY_LOGS = 1000;
	private final Path logFilePath;
	private final Deque<TransactionLogEntry> recentEntries = new ArrayDeque<>(MAX_MEMORY_LOGS);

	public TransactionAuditService() {
		this(Path.of("logs", "mcfunmod", "economy_transactions.log"));
	}

	public TransactionAuditService(Path logFilePath) {
		this.logFilePath = logFilePath;
		ensureLogFileExists();
	}

	private void ensureLogFileExists() {
		try {
			if (logFilePath.getParent() != null) {
				Files.createDirectories(logFilePath.getParent());
			}
			if (!Files.exists(logFilePath)) {
				Files.createFile(logFilePath);
			}
		} catch (IOException e) {
			McFunMod.LOGGER.error("Failed to initialize economy audit log file: {}", logFilePath, e);
		}
	}

	@Override
	public synchronized void record(TransactionLogEntry entry) {
		Objects.requireNonNull(entry, "entry cannot be null");

		// 1. Maintain in-memory ring buffer
		if (recentEntries.size() >= MAX_MEMORY_LOGS) {
			recentEntries.removeFirst();
		}
		recentEntries.addLast(entry);

		// 2. Append to persistent log file
		String line = entry.toLogString() + System.lineSeparator();
		try (BufferedWriter writer = Files.newBufferedWriter(
				logFilePath,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE,
				StandardOpenOption.APPEND)) {
			writer.write(line);
		} catch (IOException e) {
			McFunMod.LOGGER.error("Failed to write to economy audit log: {}", line, e);
		}

		// 3. Log to standard logger for server console awareness
		McFunMod.LOGGER.info("[Economy Audit] {}", entry.toLogString());
	}

	/**
	 * Returns the most recent transaction entries up to the requested limit (newest first).
	 */
	public synchronized List<TransactionLogEntry> getRecentLogs(int limit) {
		int count = Math.min(Math.max(limit, 0), recentEntries.size());
		List<TransactionLogEntry> list = new ArrayList<>(recentEntries);
		Collections.reverse(list);
		return list.subList(0, count);
	}

	/**
	 * Returns the most recent transaction entries for a specific player up to the requested limit (newest first).
	 */
	public synchronized List<TransactionLogEntry> getRecentLogsForPlayer(UUID playerId, int limit) {
		Objects.requireNonNull(playerId, "playerId cannot be null");
		List<TransactionLogEntry> matching = new ArrayList<>();
		List<TransactionLogEntry> list = new ArrayList<>(recentEntries);
		Collections.reverse(list);

		for (TransactionLogEntry entry : list) {
			if (playerId.equals(entry.playerId())) {
				matching.add(entry);
				if (matching.size() >= limit) {
					break;
				}
			}
		}
		return matching;
	}
}
