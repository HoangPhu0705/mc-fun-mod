/**
 * GAME TESTS: tests that run inside a real headless dedicated server ({@code ./gradlew runGameTest}).
 *
 * <p>Use for behavior that needs the actual game: block placement, entity interaction,
 * commands, mixins applying correctly. One class per feature ({@code FooGameTests}), each test
 * method annotated with Fabric's {@code @GameTest} and the class listed under the
 * {@code fabric-gametest} entrypoint in this source set's {@code fabric.mod.json}.
 *
 * <p>Pure logic is cheaper to test in {@code src/test} with plain JUnit.
 */
package com.codefee.mcfunmod.gametest;
