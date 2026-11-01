package com.codefee.mcfunmod.event;

/**
 * Registers every Fabric event listener of the mod. Called once from
 * {@link com.codefee.mcfunmod.McFunMod#onInitialize()} after registries and services exist.
 */
public final class ModEvents {
	private ModEvents() {
	}

	// public static void init(...services) { ServerTickEvents.END_SERVER_TICK.register(...); ... }
}
