/**
 * Data providers, one per output type and shared by all features: {@code ModModelProvider},
 * {@code ModLanguageProvider} (en_us), {@code ModRecipeProvider}, {@code ModLootTableProvider},
 * {@code ModItemTagProvider}/{@code ModBlockTagProvider}. Add each to {@code McFunModDataGenerator}.
 *
 * <p>Inside a provider, keep one clearly marked section (or private method) per feature, e.g.
 * {@code luckyCoin(translations)}. Split a feature's data into its own class only when its section grows large.
 */
package com.codefee.mcfunmod.datagen.provider;
