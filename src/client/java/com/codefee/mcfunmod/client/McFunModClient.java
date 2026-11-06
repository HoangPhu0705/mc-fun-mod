package com.codefee.mcfunmod.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entry point. Runs only on the game client, never on the dedicated server.
 *
 * <p>Client code may use common code from {@code src/main}; common code can never use this side.
 */
public class McFunModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// One line per feature that has client parts, e.g. LuckyCoinClient.init();
	}
}
