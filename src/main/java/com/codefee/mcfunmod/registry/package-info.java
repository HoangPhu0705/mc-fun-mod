/**
 * REGISTRY: every {@code Registry.register(...)} call of the mod, one class per registry type.
 *
 * <p>Want a new item/block/entity/sound/creative tab? Add a constant to the matching
 * {@code Mod*} class here, put the class itself in {@code content}, and add its
 * model/recipe/loot/lang through {@code src/datagen}.
 *
 * <p>Registration happens once, from {@link ModRegistries}, in a fixed order.
 * No gameplay logic here.
 */
package com.codefee.mcfunmod.registry;
