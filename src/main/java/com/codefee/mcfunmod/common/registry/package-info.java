/**
 * Registration shared by all features.
 *
 * <p>Put here: small helpers that wrap {@code Registry.register} (e.g. {@code register(name, factory, properties)}
 * for items, which also sets the item id), and the mod's creative tab in {@link ModCreativeTab}.
 * Each feature registers its own content by calling these helpers from its entry class.
 */
package com.codefee.mcfunmod.common.registry;
