package com.codefee.mcfunmod.feature.economy.command;

import java.util.Collection;
import java.util.List;

import com.codefee.mcfunmod.feature.economy.Economy;
import com.codefee.mcfunmod.feature.economy.logic.EconomyAccount;
import com.codefee.mcfunmod.feature.economy.logic.TransactionLogEntry;
import com.codefee.mcfunmod.feature.economy.logic.TransactionResult;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers commands for inspecting balances and performing transactions in {@code Eldrin}.
 */
public final class BalanceCommand {
	private BalanceCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// 1. /balance [player]
		dispatcher.register(Commands.literal("balance")
				.executes(BalanceCommand::showSelfBalance)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(ctx -> showTargetBalance(ctx, EntityArgument.getPlayer(ctx, "player")))));

		// 2. /pay <target> <amount>
		dispatcher.register(Commands.literal("pay")
				.then(Commands.argument("target", EntityArgument.player())
						.then(Commands.argument("amount", LongArgumentType.longArg(1L, EconomyAccount.MAX_BALANCE))
								.executes(BalanceCommand::executePay))));

		// 3. /eldrin ...
		dispatcher.register(Commands.literal("eldrin")
				.executes(BalanceCommand::showSelfBalance)
				// /eldrin balance [player]
				.then(Commands.literal("balance")
						.executes(BalanceCommand::showSelfBalance)
						.then(Commands.argument("player", EntityArgument.player())
								.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
								.executes(ctx -> showTargetBalance(ctx, EntityArgument.getPlayer(ctx, "player")))))
				// /eldrin give <targets> <amount> [reason]
				.then(Commands.literal("give")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("amount", LongArgumentType.longArg(1L, EconomyAccount.MAX_BALANCE))
										.executes(ctx -> executeGive(ctx, null))
										.then(Commands.argument("reason", StringArgumentType.greedyString())
												.executes(ctx -> executeGive(ctx, StringArgumentType.getString(ctx, "reason")))))))
				// /eldrin take <targets> <amount> [reason]
				.then(Commands.literal("take")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("amount", LongArgumentType.longArg(1L, EconomyAccount.MAX_BALANCE))
										.executes(ctx -> executeTake(ctx, null))
										.then(Commands.argument("reason", StringArgumentType.greedyString())
												.executes(ctx -> executeTake(ctx, StringArgumentType.getString(ctx, "reason")))))))
				// /eldrin set <targets> <amount> [reason]
				.then(Commands.literal("set")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("amount", LongArgumentType.longArg(0L, EconomyAccount.MAX_BALANCE))
										.executes(ctx -> executeSet(ctx, null))
										.then(Commands.argument("reason", StringArgumentType.greedyString())
												.executes(ctx -> executeSet(ctx, StringArgumentType.getString(ctx, "reason")))))))
				// /eldrin pay <target> <amount>
				.then(Commands.literal("pay")
						.then(Commands.argument("target", EntityArgument.player())
								.then(Commands.argument("amount", LongArgumentType.longArg(1L, EconomyAccount.MAX_BALANCE))
										.executes(BalanceCommand::executePay))))
				// /eldrin history <target> [limit]
				.then(Commands.literal("history")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.argument("target", EntityArgument.player())
								.executes(ctx -> executeHistory(ctx, 5))
								.then(Commands.argument("limit", IntegerArgumentType.integer(1, 20))
										.executes(ctx -> executeHistory(ctx, IntegerArgumentType.getInteger(ctx, "limit"))))))
		);
	}

	private static int showSelfBalance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		long balance = Economy.getBalance(player);
		ctx.getSource().sendSuccess(() -> Component.literal(
				"§6[Economy] §eYour balance: " + Economy.formatColored(balance)), false);
		return 1;
	}

	private static int showTargetBalance(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		long balance = Economy.getBalance(target);
		ctx.getSource().sendSuccess(() -> Component.literal(
				"§6[Economy] §f" + target.getScoreboardName() + "§e's balance: "
						+ Economy.formatColored(balance)), false);
		return 1;
	}

	private static int executeGive(CommandContext<CommandSourceStack> ctx, String reason) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		long amount = LongArgumentType.getLong(ctx, "amount");
		String auditReason = "admin_give:" + ctx.getSource().getTextName()
				+ (reason != null && !reason.isBlank() ? " (" + reason + ")" : "");

		int successCount = 0;
		for (ServerPlayer target : targets) {
			TransactionResult result = Economy.deposit(target, amount, auditReason);
			if (result.success()) {
				successCount++;
				target.sendSystemMessage(Component.literal(
						"§6[Economy] §aYou received " + Economy.formatColored(amount)
								+ "§a. New balance: " + Economy.formatColored(result.newBalance())));
				ctx.getSource().sendSuccess(() -> Component.literal(
						"§6[Economy] §aGave " + Economy.formatColored(amount) + "§a to §f" + target.getScoreboardName()
								+ "§a. New balance: " + Economy.formatColored(result.newBalance())), true);
			} else {
				ctx.getSource().sendFailure(Component.literal(
						"§cFailed to give to " + target.getScoreboardName() + ": " + result.status()));
			}
		}
		return successCount;
	}

	private static int executeTake(CommandContext<CommandSourceStack> ctx, String reason) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		long amount = LongArgumentType.getLong(ctx, "amount");
		String auditReason = "admin_take:" + ctx.getSource().getTextName()
				+ (reason != null && !reason.isBlank() ? " (" + reason + ")" : "");

		int successCount = 0;
		for (ServerPlayer target : targets) {
			TransactionResult result = Economy.withdraw(target, amount, auditReason);
			if (result.success()) {
				successCount++;
				target.sendSystemMessage(Component.literal(
						"§6[Economy] §c" + Economy.formatColored(amount)
								+ " §chas been deducted from your balance. New balance: "
								+ Economy.formatColored(result.newBalance())));
				ctx.getSource().sendSuccess(() -> Component.literal(
						"§6[Economy] §cTook " + Economy.formatColored(amount) + "§c from §f" + target.getScoreboardName()
								+ "§c. New balance: " + Economy.formatColored(result.newBalance())), true);
			} else {
				ctx.getSource().sendFailure(Component.literal(
						"§cFailed to take from " + target.getScoreboardName() + ": " + result.status()));
			}
		}
		return successCount;
	}

	private static int executeSet(CommandContext<CommandSourceStack> ctx, String reason) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		long amount = LongArgumentType.getLong(ctx, "amount");
		String auditReason = "admin_set:" + ctx.getSource().getTextName()
				+ (reason != null && !reason.isBlank() ? " (" + reason + ")" : "");

		int successCount = 0;
		for (ServerPlayer target : targets) {
			TransactionResult result = Economy.setBalance(target, amount, auditReason);
			if (result.success()) {
				successCount++;
				target.sendSystemMessage(Component.literal(
						"§6[Economy] §eYour balance has been set to " + Economy.formatColored(result.newBalance())));
				ctx.getSource().sendSuccess(() -> Component.literal(
						"§6[Economy] §aSet §f" + target.getScoreboardName() + "§a's balance to "
								+ Economy.formatColored(result.newBalance())), true);
			} else {
				ctx.getSource().sendFailure(Component.literal(
						"§cFailed to set balance for " + target.getScoreboardName() + ": " + result.status()));
			}
		}
		return successCount;
	}

	private static int executePay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer sender = ctx.getSource().getPlayerOrException();
		ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
		long amount = LongArgumentType.getLong(ctx, "amount");

		if (sender.getUUID().equals(target.getUUID())) {
			ctx.getSource().sendFailure(Component.literal("§cYou cannot pay yourself."));
			return 0;
		}

		TransactionResult result = Economy.transfer(sender, target, amount, "player_pay");
		if (result.success()) {
			sender.sendSystemMessage(Component.literal(
					"§6[Economy] §aYou sent " + Economy.formatColored(amount) + "§a to §f" + target.getScoreboardName()
							+ "§a. Your new balance: " + Economy.formatColored(result.newBalance())));
			target.sendSystemMessage(Component.literal(
					"§6[Economy] §aYou received " + Economy.formatColored(amount) + "§a from §f" + sender.getScoreboardName()
							+ "§a. Your new balance: " + Economy.formatColored(Economy.getBalance(target))));
			return 1;
		}

		ctx.getSource().sendFailure(Component.literal(
				"§cPayment failed: " + result.status()));
		return 0;
	}

	private static int executeHistory(CommandContext<CommandSourceStack> ctx, int limit) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
		if (Economy.getAuditService() == null) {
			ctx.getSource().sendFailure(Component.literal("§cAudit service is not available."));
			return 0;
		}

		List<TransactionLogEntry> logs = Economy.getAuditService().getRecentLogsForPlayer(target.getUUID(), limit);
		if (logs.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.literal(
					"§6[Economy] §7No recent transaction logs found for §f" + target.getScoreboardName()), false);
			return 1;
		}

		ctx.getSource().sendSuccess(() -> Component.literal(
				"§6[Economy] === Recent " + logs.size() + " transaction(s) for §f" + target.getScoreboardName() + "§6 ==="), false);
		for (TransactionLogEntry entry : logs) {
			String color = entry.status().name().equals("SUCCESS") ? "§a" : "§c";
			ctx.getSource().sendSuccess(() -> Component.literal(
					String.format("§7- %s[%s] %s §7(prev: %s§7, new: %s§7, reason: §f%s§7)",
							color,
							entry.type(),
							Economy.formatColored(entry.amount()),
							Economy.formatColored(entry.previousBalance()),
							Economy.formatColored(entry.newBalance()),
							entry.reason())), false);
		}
		return logs.size();
	}
}
