package com.codefee.mcfunmod.feature.economy.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codefee.mcfunmod.feature.economy.logic.TransactionLogEntry;
import com.codefee.mcfunmod.feature.economy.logic.TransactionStatus;
import com.codefee.mcfunmod.feature.economy.logic.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionAuditServiceTest {

	@Test
	@DisplayName("Records transaction to memory buffer and appends to file")
	void recordsToMemoryAndFile(@TempDir Path tempDir) throws IOException {
		Path logFile = tempDir.resolve("audit.log");
		TransactionAuditService service = new TransactionAuditService(logFile);

		UUID player1 = UUID.randomUUID();
		UUID player2 = UUID.randomUUID();

		TransactionLogEntry e1 = new TransactionLogEntry(
				Instant.now(), player1, "PlayerOne", TransactionType.DEPOSIT, 100L, 0L, 100L,
				TransactionStatus.SUCCESS, "quest");
		TransactionLogEntry e2 = new TransactionLogEntry(
				Instant.now(), player2, "PlayerTwo", TransactionType.WITHDRAW, 40L, 100L, 60L,
				TransactionStatus.SUCCESS, "shop_buy");
		TransactionLogEntry e3 = new TransactionLogEntry(
				Instant.now(), player1, "PlayerOne", TransactionType.WITHDRAW, 20L, 100L, 80L,
				TransactionStatus.SUCCESS, "teleport_fee");

		service.record(e1);
		service.record(e2);
		service.record(e3);

		// Check memory queries
		List<TransactionLogEntry> recentAll = service.getRecentLogs(10);
		assertEquals(3, recentAll.size());
		assertEquals(e3, recentAll.get(0)); // newest first

		List<TransactionLogEntry> p1Logs = service.getRecentLogsForPlayer(player1, 10);
		assertEquals(2, p1Logs.size());
		assertEquals(e3, p1Logs.get(0));
		assertEquals(e1, p1Logs.get(1));

		List<TransactionLogEntry> p2Logs = service.getRecentLogsForPlayer(player2, 10);
		assertEquals(1, p2Logs.size());
		assertEquals(e2, p2Logs.get(0));

		// Check file content
		List<String> fileLines = Files.readAllLines(logFile);
		assertEquals(3, fileLines.size());
		assertTrue(fileLines.get(0).contains("PlayerOne"));
		assertTrue(fileLines.get(0).contains("quest"));
		assertTrue(fileLines.get(0).contains("100 Eldrin"));
		assertTrue(fileLines.get(1).contains("PlayerTwo"));
		assertTrue(fileLines.get(1).contains("40 Eldrin"));
		assertTrue(fileLines.get(2).contains("teleport_fee"));
	}
}
