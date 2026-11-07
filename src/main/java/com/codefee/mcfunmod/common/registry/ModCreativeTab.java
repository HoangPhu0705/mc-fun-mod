package com.codefee.mcfunmod.common.registry;

/**
 * The mod's creative mode tab ("MC Fun Mod"), shared by all features.
 *
 * <p>Registered once in {@code init()}, called from {@link com.codefee.mcfunmod.McFunMod}.
 * Features add their own items to it from their entry class with Fabric's
 * {@code CreativeModeTabEvents.modifyOutputEvent(KEY)}, so this class never lists features.
 */
public final class ModCreativeTab {
	private ModCreativeTab() {
	}

	// public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, McFunMod.id("main"));
	// public static void init() { Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, FabricCreativeModeTab.builder()...build()); }
}
