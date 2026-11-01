package com.hoangphu.mcfunmod.registry;

/**
 * Single entry point that triggers every registry class in dependency order.
 *
 * <p>Intended order: data components, sounds, blocks, items, block entities, entities,
 * creative tabs. Called once from {@link com.hoangphu.mcfunmod.McFunMod#onInitialize()}.
 */
public final class ModRegistries {
	private ModRegistries() {
	}

	// public static void init() { ModDataComponents.init(); ModSounds.init(); ModBlocks.init(); ... }
}
