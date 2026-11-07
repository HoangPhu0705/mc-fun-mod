/**
 * CLIENT-ONLY parts of features, mirroring {@code feature} in src/main.
 *
 * <pre>
 * client/feature/luckycoin/
 *   LuckyCoinClient.java   entry class: init() registers this feature's client parts
 *   render/                entity / block entity renderers, models, HUD overlays
 *   screen/                GUI screens
 *   keybind/               key mappings
 *   network/               receivers for server-to-client payloads
 * </pre>
 *
 * <p>A client feature may use its own feature's classes in src/main (e.g. the renderer for
 * {@code feature.luckycoin.entity.CoinEntity}). Other features: entry class only, same as on the server side.
 * Screens and key bindings change game state by sending a payload to the server, never directly.
 */
package com.codefee.mcfunmod.client.feature;
