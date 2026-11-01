/**
 * CONFIG: reading/writing the mod's config file ({@code config/mcfunmod.json} on the server).
 *
 * <p>Loads the file into an immutable record and exposes it to core through a
 * {@code core.port.ConfigSource}-style interface. Core never reads files itself.
 */
package com.hoangphu.mcfunmod.config;
