/**
 * Ports: interfaces the core needs from the outside world
 * (e.g. {@code PlayerGateway}, {@code WorldGateway}, {@code ConfigSource}, {@code Clock}).
 *
 * <p>Implementations live in {@code adapter} (real Minecraft) and in {@code src/test} (fakes).
 * Name ports after what the core needs, not after the Minecraft API that provides it.
 */
package com.codefee.mcfunmod.core.port;
