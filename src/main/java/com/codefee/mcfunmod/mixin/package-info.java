/**
 * MIXINS (common/server side). Last resort: use only when no Fabric API event exists.
 *
 * <p>Mixin classes must stay under this one package (the mixin config points at it), so each feature
 * gets a sub-package: {@code mixin/luckycoin/FooMixin} is listed as {@code "luckycoin.FooMixin"}
 * in {@code mcfunmod.mixins.json}.
 *
 * <p>Rules:
 * <ul>
 *   <li>A mixin method only forwards to its feature (entry class or {@code logic/}). No logic here.</li>
 *   <li>Prefer {@code @Inject} over {@code @Overwrite}/{@code @Redirect} for compatibility with other mods.</li>
 *   <li>Nothing may reference a mixin class (enforced by {@code ArchitectureTest}): they do not exist at runtime.</li>
 * </ul>
 */
package com.codefee.mcfunmod.mixin;
