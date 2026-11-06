/**
 * FEATURES: one sub-package per gameplay feature. Everything a feature needs lives in its folder.
 *
 * <pre>
 * feature/luckycoin/
 *   LuckyCoin.java     entry class: init() registers this feature's items, events, commands, payloads
 *   logic/             pure Java rules, no Minecraft imports; unit tested in src/test
 *   item/              Item subclasses
 *   block/             Block and BlockEntity subclasses
 *   entity/            Entity subclasses and their AI goals
 *   event/             Fabric event listeners
 *   command/           Brigadier commands
 *   network/           payload records and server-side receivers
 * </pre>
 *
 * <p>Create only the folders the feature needs. A feature with no rules of its own has no {@code logic/}.
 *
 * <p>Rules (enforced by {@code ArchitectureTest}):
 * <ul>
 *   <li>{@code logic/} imports no Minecraft, Fabric or Mixin classes.</li>
 *   <li>A feature never imports another feature's internals. If it must use another feature, it goes
 *       through that feature's entry class only. Code needed by several features moves to {@code common}.</li>
 *   <li>No dependency cycles between features.</li>
 * </ul>
 *
 * <p>Not enforced, but expected: the Minecraft-side folders ({@code item/}, {@code event/}, ...) stay thin.
 * They read what they need from Minecraft, call {@code logic/}, and apply the result.
 *
 * <p>Client-only parts of a feature (renderers, screens, key bindings) live in
 * {@code src/client/.../client/feature/<name>/}. See docs/ARCHITECTURE.md for a step-by-step guide.
 */
package com.codefee.mcfunmod.feature;
