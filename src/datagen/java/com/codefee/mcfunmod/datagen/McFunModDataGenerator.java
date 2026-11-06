package com.codefee.mcfunmod.datagen;

import com.codefee.mcfunmod.McFunMod;

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
		// pack.addProvider(ModModelProvider::new);
		// pack.addProvider(ModLanguageProvider::new);
		// pack.addProvider(ModRecipeProvider::new);
		// pack.addProvider(ModLootTableProvider::new);
		// pack.addProvider(ModItemTagProvider::new);
	}

	/**
	 * This entrypoint lives in the separate "mcfunmod-datagen" mod (src/datagen), but the files it
	 * generates belong to "mcfunmod". Without this, lang files and recipes land in the wrong namespace.
	 */
	@Override
	public String getEffectiveModId() {
		return McFunMod.MOD_ID;
	}
}
