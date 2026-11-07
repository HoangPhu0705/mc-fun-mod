package com.codefee.mcfunmod.feature.economy.logic;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyAccountTest {

	@Nested
	@DisplayName("Initial Balance & Queries")
	class BalanceQueries {
		@Test
		void defaultBalanceIsZero() {
			EconomyAccount account = new EconomyAccount();
			assertEquals(0L, account.getBalance());
			assertTrue(account.has(0L));
			assertFalse(account.has(1L));
		}

		@Test
		void customInitialBalance() {
			EconomyAccount account = new EconomyAccount(500L);
			assertEquals(500L, account.getBalance());
			assertTrue(account.has(250L));
			assertTrue(account.has(500L));
			assertFalse(account.has(501L));
		}

		@Test
		void negativeInitialBalanceClampedToZero() {
			EconomyAccount account = new EconomyAccount(-100L);
			assertEquals(0L, account.getBalance());
		}
	}

	@Nested
	@DisplayName("Deposit Operations")
	class Deposits {
		@Test
		void validDepositIncreasesBalance() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.deposit(50L, "quest_reward");

			assertTrue(result.success());
			assertEquals(TransactionStatus.SUCCESS, result.status());
			assertEquals(100L, result.previousBalance());
			assertEquals(150L, result.newBalance());
			assertEquals(50L, result.amount());
			assertEquals("quest_reward", result.reason());
			assertEquals(150L, account.getBalance());
		}

		@Test
		void zeroDepositFailsWithInvalidAmount() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.deposit(0L, "test");

			assertFalse(result.success());
			assertEquals(TransactionStatus.INVALID_AMOUNT, result.status());
			assertEquals(100L, result.previousBalance());
			assertEquals(100L, result.newBalance());
			assertEquals(100L, account.getBalance());
		}

		@Test
		void negativeDepositFailsWithInvalidAmount() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.deposit(-25L, "exploit_attempt");

			assertFalse(result.success());
			assertEquals(TransactionStatus.INVALID_AMOUNT, result.status());
			assertEquals(100L, account.getBalance());
		}

		@Test
		void depositExceedingMaxBalanceFailsWithOverflow() {
			EconomyAccount account = new EconomyAccount(EconomyAccount.MAX_BALANCE - 10L);
			TransactionResult result = account.deposit(20L, "overflow_test");

			assertFalse(result.success());
			assertEquals(TransactionStatus.OVERFLOW, result.status());
			assertEquals(EconomyAccount.MAX_BALANCE - 10L, account.getBalance());
		}
	}

	@Nested
	@DisplayName("Withdraw Operations")
	class Withdrawals {
		@Test
		void validWithdrawDecreasesBalance() {
			EconomyAccount account = new EconomyAccount(300L);
			TransactionResult result = account.withdraw(120L, "shop_purchase");

			assertTrue(result.success());
			assertEquals(TransactionStatus.SUCCESS, result.status());
			assertEquals(300L, result.previousBalance());
			assertEquals(180L, result.newBalance());
			assertEquals(120L, result.amount());
			assertEquals(180L, account.getBalance());
		}

		@Test
		void exactBalanceWithdrawLeavesZero() {
			EconomyAccount account = new EconomyAccount(200L);
			TransactionResult result = account.withdraw(200L, "teleport_fee");

			assertTrue(result.success());
			assertEquals(0L, account.getBalance());
		}

		@Test
		void withdrawMoreThanBalanceFailsWithInsufficientFunds() {
			EconomyAccount account = new EconomyAccount(50L);
			TransactionResult result = account.withdraw(100L, "expensive_item");

			assertFalse(result.success());
			assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, result.status());
			assertEquals(50L, result.previousBalance());
			assertEquals(50L, result.newBalance());
			assertEquals(50L, account.getBalance());
		}

		@Test
		void zeroOrNegativeWithdrawFailsWithInvalidAmount() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult r1 = account.withdraw(0L, "test");
			TransactionResult r2 = account.withdraw(-50L, "test");

			assertFalse(r1.success());
			assertFalse(r2.success());
			assertEquals(TransactionStatus.INVALID_AMOUNT, r1.status());
			assertEquals(TransactionStatus.INVALID_AMOUNT, r2.status());
			assertEquals(100L, account.getBalance());
		}
	}

	@Nested
	@DisplayName("Set Balance Operations")
	class SetBalance {
		@Test
		void validSetBalanceUpdatesAccount() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.setBalance(500L, "admin_set");

			assertTrue(result.success());
			assertEquals(TransactionStatus.SUCCESS, result.status());
			assertEquals(100L, result.previousBalance());
			assertEquals(500L, result.newBalance());
			assertEquals(500L, account.getBalance());
		}

		@Test
		void negativeSetBalanceFails() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.setBalance(-50L, "negative_set");

			assertFalse(result.success());
			assertEquals(TransactionStatus.INVALID_AMOUNT, result.status());
			assertEquals(100L, account.getBalance());
		}

		@Test
		void overflowSetBalanceFails() {
			EconomyAccount account = new EconomyAccount(100L);
			TransactionResult result = account.setBalance(EconomyAccount.MAX_BALANCE + 1L, "too_large");

			assertFalse(result.success());
			assertEquals(TransactionStatus.OVERFLOW, result.status());
			assertEquals(100L, account.getBalance());
		}
	}

	@Nested
	@DisplayName("Transfer Operations")
	class Transfers {
		@Test
		void validTransferBetweenAccounts() {
			EconomyAccount sender = new EconomyAccount(500L);
			EconomyAccount recipient = new EconomyAccount(100L);

			TransactionResult result = EconomyAccount.transfer(sender, recipient, 150L, "player_pay");

			assertTrue(result.success());
			assertEquals(350L, sender.getBalance());
			assertEquals(250L, recipient.getBalance());
		}

		@Test
		void transferWithInsufficientFundsFailsWithoutMutatingBalances() {
			EconomyAccount sender = new EconomyAccount(50L);
			EconomyAccount recipient = new EconomyAccount(100L);

			TransactionResult result = EconomyAccount.transfer(sender, recipient, 100L, "player_pay");

			assertFalse(result.success());
			assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, result.status());
			assertEquals(50L, sender.getBalance());
			assertEquals(100L, recipient.getBalance());
		}

		@Test
		void transferZeroOrNegativeFails() {
			EconomyAccount sender = new EconomyAccount(100L);
			EconomyAccount recipient = new EconomyAccount(100L);

			TransactionResult result = EconomyAccount.transfer(sender, recipient, 0L, "player_pay");
			assertFalse(result.success());
			assertEquals(TransactionStatus.INVALID_AMOUNT, result.status());
			assertEquals(100L, sender.getBalance());
			assertEquals(100L, recipient.getBalance());
		}
	}

	@Nested
	@DisplayName("Audit Log Entry")
	class AuditLog {
		@Test
		void auditLogEntryFormatsStringCorrectly() {
			UUID uuid = UUID.randomUUID();
			Instant now = Instant.now();
			TransactionLogEntry entry = new TransactionLogEntry(
					now,
					uuid,
					"Alex",
					TransactionType.DEPOSIT,
					150L,
					100L,
					250L,
					TransactionStatus.SUCCESS,
					"quest_reward:goblin_camp");

			String str = entry.toLogString();
			assertNotNull(str);
			assertTrue(str.contains("DEPOSIT"));
			assertTrue(str.contains("Alex"));
			assertTrue(str.contains(uuid.toString()));
			assertTrue(str.contains("amount=150 Eldrin"));
			assertTrue(str.contains("before=100"));
			assertTrue(str.contains("after=250"));
			assertTrue(str.contains("quest_reward:goblin_camp"));
		}
	}
}
