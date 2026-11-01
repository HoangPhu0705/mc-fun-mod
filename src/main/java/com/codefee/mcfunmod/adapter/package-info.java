/**
 * ADAPTERS: implementations of {@code core.port} interfaces backed by Minecraft / Fabric.
 *
 * <p>Put here: classes like {@code MinecraftPlayerGateway implements PlayerGateway} that
 * translate between Minecraft objects ({@code ServerPlayer}, {@code ServerLevel}, ...) and
 * core model types.
 *
 * <p>Rules:
 * <ul>
 *   <li>Only this package implements {@code core.port} interfaces (tests excluded).</li>
 *   <li>Keep them thin: convert data and call Minecraft. Decisions belong in {@code core.service}.</li>
 *   <li>Adapters are created once in {@link com.codefee.mcfunmod.McFunMod} and passed into services.</li>
 * </ul>
 */
package com.codefee.mcfunmod.adapter;
