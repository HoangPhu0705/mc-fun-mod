package com.hoangphu.mcfunmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Data generation entrypoint. Run with {@code ./gradlew runDatagen}; output goes to
 * {@code src/main/generated} and must be committed (CI fails if it is stale).
 *
 * <p>Prefer generating JSON (recipes, loot tables, tags, models, lang) over hand-writing it in
 * {@code src/main/resources}: mistakes become compile errors instead of missing textures in game.
 */
public class McFunModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		// FabricDataGenerator.Pack pack = generator.createPack();
		// pack.addProvider(ModRecipeProvider::new);
		// pack.addProvider(ModLootTableProvider::new);
		// pack.addProvider(ModTagProvider::new);
		// pack.addProvider(ModModelProvider::new);
		// pack.addProvider(ModLanguageProvider::new);
	}
}
