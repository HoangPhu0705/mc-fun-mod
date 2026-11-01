package com.hoangphu.mcfunmod.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Composition root (client side). Runs only on the game client, never on the dedicated server.
 *
 * <p>Client code may use common code from {@code src/main}; common code must never use this side.
 */
public class McFunModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Intended startup order:
		// 1. render   -> ModRenderers.init()
		// 2. input    -> ModKeyBindings.init()
		// 3. network  -> ClientNetworking.init()
	}
}
