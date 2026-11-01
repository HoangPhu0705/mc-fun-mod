/**
 * MIXINS (common/server side). Last resort: use only when no Fabric API event exists.
 *
 * <p>Rules:
 * <ul>
 *   <li>Every mixin must be listed in {@code mcfunmod.mixins.json}.</li>
 *   <li>A mixin method only forwards to a {@code core.service} or fires one of our own
 *       events. No logic here; mixins are the hardest code to debug and update.</li>
 *   <li>Prefer {@code @Inject} over {@code @Overwrite}/{@code @Redirect} for compatibility.</li>
 *   <li>Nothing outside this package may reference a mixin class (enforced by {@code ArchitectureTest}).</li>
 * </ul>
 */
package com.hoangphu.mcfunmod.mixin;
