/**
 * CONTENT: Minecraft subclasses that define game objects (custom {@code Block}, {@code Item},
 * {@code Entity}, {@code BlockEntity}).
 *
 * <p>These classes override Minecraft hooks ({@code use}, {@code tick}, {@code onPlace}, ...)
 * and delegate any non-trivial decision to a {@code core.service}. They are registered in
 * {@code registry}, never instantiated anywhere else.
 */
package com.hoangphu.mcfunmod.content;
