package com.codefee.mcfunmod;

import com.codefee.mcfunmod.feature.economy.Economy;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod entry point (common/server side). Only wires things together: no gameplay logic here.
 */
public class McFunMod implements ModInitializer {
	public static final String MOD_ID = "mcfunmod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Startup order:
		// 1. common   -> ModConfig.load(), ModCreativeTab.init()
		// 2. features -> one line per feature, e.g. LuckyCoin.init();
		//                a feature that uses another feature's entry class goes after it.
		Economy.init();

		LOGGER.info("MC Fun Mod initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
