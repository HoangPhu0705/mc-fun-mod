/**
 * CORE: the mod's own rules, written in plain Java.
 *
 * <p>Put here: game rules and calculations that make this mod "fun" (cooldowns, scoring,
 * loot rolls, team logic, ...), expressed without Minecraft types.
 *
 * <p>Rules (enforced by {@code ArchitectureTest}):
 * <ul>
 *   <li>No imports from {@code net.minecraft}, {@code com.mojang}, {@code net.fabricmc},
 *       {@code org.spongepowered}, or any other package of this mod.</li>
 *   <li>Everything here must be unit-testable with plain JUnit in {@code src/test}.</li>
 *   <li>Talk to the outside world only through interfaces in {@code core.port}.</li>
 * </ul>
 *
 * <p>Once there is more than one feature, split by feature inside each package
 * ({@code core.model.minigame}, {@code core.service.minigame}, ...).
 */
package com.hoangphu.mcfunmod.core;
