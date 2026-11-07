/**
 * GAME TESTS: tests that run inside a real headless dedicated server ({@code ./gradlew runGameTest}).
 *
 * <p>One class per feature, mirroring src/main: {@code gametest/feature/luckycoin/LuckyCoinGameTests}.
 * Each test method is annotated with Fabric's {@code @GameTest}; each class is listed under the
 * {@code fabric-gametest} entrypoint in this source set's {@code fabric.mod.json}.
 *
 * <p>Use for behavior that needs the actual game (items used, blocks placed, commands run, mixins applied).
 * Pure rules are cheaper to test in {@code src/test}.
 */
package com.codefee.mcfunmod.gametest;
