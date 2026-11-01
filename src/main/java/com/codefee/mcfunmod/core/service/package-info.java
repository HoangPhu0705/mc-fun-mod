/**
 * Use cases / domain services: one class per action the mod performs
 * (e.g. {@code GrantRewardService}, {@code StartMinigameService}).
 *
 * <p>Services receive their collaborators (ports) through the constructor. They never
 * look anything up statically and never touch Minecraft directly, which keeps them
 * testable with fakes in {@code src/test}.
 */
package com.codefee.mcfunmod.core.service;
