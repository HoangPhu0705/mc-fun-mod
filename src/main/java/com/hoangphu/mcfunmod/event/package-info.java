/**
 * EVENTS: subscriptions to Fabric API callbacks ({@code ServerTickEvents},
 * {@code ServerPlayConnectionEvents}, {@code PlayerBlockBreakEvents}, {@code UseItemCallback}, ...).
 *
 * <p>Each listener extracts what it needs from the event, converts it via an adapter and
 * calls a {@code core.service}. A listener body should be a few lines; if it grows, the
 * logic belongs in core.
 *
 * <p>Split {@link ModEvents} by topic ({@code PlayerEvents}, {@code TickEvents}, ...) once it gets long.
 */
package com.hoangphu.mcfunmod.event;
