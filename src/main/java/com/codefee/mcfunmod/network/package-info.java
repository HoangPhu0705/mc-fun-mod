/**
 * NETWORKING (server side + shared payload definitions).
 *
 * <p>Payload records and their {@code StreamCodec}s live in {@code network.payload} because
 * both sides need them. Server receivers ({@code ServerPlayNetworking.registerGlobalReceiver})
 * live here; client receivers live in {@code client.network}.
 *
 * <p>Never trust client data: validate in the receiver, then call a {@code core.service}.
 */
package com.codefee.mcfunmod.network;
