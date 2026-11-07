package com.codefee.mcfunmod.feature.economy;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import com.codefee.mcfunmod.McFunMod;
import com.codefee.mcfunmod.feature.economy.audit.TransactionAuditService;
import com.codefee.mcfunmod.feature.economy.command.BalanceCommand;
import com.codefee.mcfunmod.feature.economy.logic.EconomyAccount;
import com.codefee.mcfunmod.feature.economy.logic.TransactionLogEntry;
import com.codefee.mcfunmod.feature.economy.logic.TransactionResult;
import com.codefee.mcfunmod.feature.economy.logic.TransactionType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.level.ServerPlayer;

/**
 * Entry class and public API for the economy feature using the single fantasy currency: {@code Eldrin}.
 *
 * <p>Other features (e.g. shops, quests, dungeon rewards, teleport fees) MUST interact with the economy
 * exclusively through the static methods on this class.
 */
public final class Economy {
	public static final String CURRENCY_NAME = "Eldrin";
	public static final String CURRENCY_SYMBOL = "✦";

	public static final AttachmentType<Long> ELDRIN_BALANCE = AttachmentRegistry.create(
			McFunMod.id("eldrin_balance"),
			builder -> builder.initializer(() -> 0L).persistent(Codec.LONG).copyOnDeath());

	private static TransactionAuditService auditService;

	private Economy() {
	}

	/**
	 * Called once during mod initialization (from {@link McFunMod#onInitialize()}).
	 */
	public static void init() {
		auditService = new TransactionAuditService();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				BalanceCommand.register(dispatcher));
		McFunMod.LOGGER.info("Economy feature initialized (currency: {})", CURRENCY_NAME);
	}

	/**
	 * Returns the transaction audit service instance.
	 */
	public static TransactionAuditService getAuditService() {
		return auditService;
	}

	/**
	 * Gets the current Eldrin balance of a player.
	 *
	 * @param player target player
	 * @return Eldrin balance (>= 0)
	 */
	public static long getBalance(ServerPlayer player) {
		Objects.requireNonNull(player, "player cannot be null");
		return player.getAttachedOrElse(ELDRIN_BALANCE, 0L);
	}

	/**
	 * Checks if a player has at least the specified amount of Eldrin.
	 *
	 * @param player target player
	 * @param amount amount required
	 * @return true if player balance >= amount
	 */
	public static boolean has(ServerPlayer player, long amount) {
		return amount >= 0 && getBalance(player) >= amount;
	}

	/**
	 * Safely deposits Eldrin into a player's account and records an audit log entry.
	 *
	 * @param player target player
	 * @param amount amount to deposit (must be positive)
	 * @param reason context/audit description
	 * @return transaction result
	 */
	public static TransactionResult deposit(ServerPlayer player, long amount, String reason) {
		Objects.requireNonNull(player, "player cannot be null");
		long current = getBalance(player);
		EconomyAccount account = new EconomyAccount(current);
		TransactionResult result = account.deposit(amount, reason);

		if (result.success()) {
			player.setAttached(ELDRIN_BALANCE, account.getBalance());
		}

		recordAudit(player, TransactionType.DEPOSIT, amount, result);
		return result;
	}

	/**
	 * Safely withdraws Eldrin from a player's account if sufficient funds exist,
	 * recording an audit log entry.
	 *
	 * @param player target player
	 * @param amount amount to withdraw (must be positive)
	 * @param reason context/audit description
	 * @return transaction result
	 */
	public static TransactionResult withdraw(ServerPlayer player, long amount, String reason) {
		Objects.requireNonNull(player, "player cannot be null");
		long current = getBalance(player);
		EconomyAccount account = new EconomyAccount(current);
		TransactionResult result = account.withdraw(amount, reason);

		if (result.success()) {
			player.setAttached(ELDRIN_BALANCE, account.getBalance());
		}

		recordAudit(player, TransactionType.WITHDRAW, amount, result);
		return result;
	}

	/**
	 * Sets the player's Eldrin balance directly (used by admin commands).
	 *
	 * @param player     target player
	 * @param newBalance target balance (must be non-negative)
	 * @param reason     context/audit description
	 * @return transaction result
	 */
	public static TransactionResult setBalance(ServerPlayer player, long newBalance, String reason) {
		Objects.requireNonNull(player, "player cannot be null");
		long current = getBalance(player);
		EconomyAccount account = new EconomyAccount(current);
		TransactionResult result = account.setBalance(newBalance, reason);

		if (result.success()) {
			player.setAttached(ELDRIN_BALANCE, account.getBalance());
		}

		recordAudit(player, TransactionType.SET, newBalance, result);
		return result;
	}

	/**
	 * Transfers Eldrin from one player to another atomically.
	 *
	 * @param from   source player
	 * @param to     target player
	 * @param amount amount to transfer (must be positive)
	 * @param reason context/audit description
	 * @return transaction result from source player's perspective
	 */
	public static TransactionResult transfer(ServerPlayer from, ServerPlayer to, long amount, String reason) {
		Objects.requireNonNull(from, "source player cannot be null");
		Objects.requireNonNull(to, "target player cannot be null");

		if (from.getUUID().equals(to.getUUID())) {
			return TransactionResult.invalidAmount(getBalance(from), amount, "cannot_transfer_to_self");
		}

		long fromBalance = getBalance(from);
		long toBalance = getBalance(to);
		EconomyAccount fromAcc = new EconomyAccount(fromBalance);
		EconomyAccount toAcc = new EconomyAccount(toBalance);

		TransactionResult result = EconomyAccount.transfer(fromAcc, toAcc, amount, reason);
		if (result.success()) {
			from.setAttached(ELDRIN_BALANCE, fromAcc.getBalance());
			to.setAttached(ELDRIN_BALANCE, toAcc.getBalance());
		}

		if (auditService != null) {
			String fromReason = "transfer_to:" + to.getScoreboardName() + (reason != null ? " (" + reason + ")" : "");
			String toReason = "transfer_from:" + from.getScoreboardName() + (reason != null ? " (" + reason + ")" : "");
			auditService.record(new TransactionLogEntry(
					Instant.now(),
					from.getUUID(),
					from.getScoreboardName(),
					TransactionType.TRANSFER,
					amount,
					fromBalance,
					fromAcc.getBalance(),
					result.status(),
					fromReason));
			if (result.success()) {
				auditService.record(new TransactionLogEntry(
						Instant.now(),
						to.getUUID(),
						to.getScoreboardName(),
						TransactionType.TRANSFER,
						amount,
						toBalance,
						toAcc.getBalance(),
						result.status(),
						toReason));
			}
		}

		return result;
	}

	/**
	 * Formats an Eldrin amount in plain text (e.g. {@code 1,500 Eldrin}).
	 */
	public static String format(long amount) {
		return NumberFormat.getNumberInstance(Locale.US).format(amount) + " " + CURRENCY_NAME;
	}

	/**
	 * Formats an Eldrin amount in color-coded chat text (e.g. {@code §b1,500 Eldrin ✦}).
	 */
	public static String formatColored(long amount) {
		return "§b" + NumberFormat.getNumberInstance(Locale.US).format(amount) + " " + CURRENCY_NAME + " " + CURRENCY_SYMBOL;
	}

	private static void recordAudit(ServerPlayer player, TransactionType type, long amount, TransactionResult result) {
		if (auditService != null) {
			auditService.record(new TransactionLogEntry(
					Instant.now(),
					player.getUUID(),
					player.getScoreboardName(),
					type,
					amount,
					result.previousBalance(),
					result.newBalance(),
					result.status(),
					result.reason()));
		}
	}
}
