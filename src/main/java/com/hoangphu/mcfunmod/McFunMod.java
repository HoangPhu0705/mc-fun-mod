package com.hoangphu.mcfunmod;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composition root (common/server side). The only place where the mod is wired together.
 *
 * <p>Keep this class free of logic: it creates objects and calls {@code init} methods in order.
 */
public class McFunMod implements ModInitializer {
	public static final String MOD_ID = "mcfunmod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Intended startup order:
		// 1. config    -> ModConfig.load()
		// 2. registry  -> ModRegistries.init()
		// 3. services  -> build adapters (adapter/*) and inject them into core services (core/service/*)
		// 4. wiring    -> ModNetworking.init(...), ModCommands.init(...), ModEvents.init(...)

		LOGGER.info("MC Fun Mod initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
