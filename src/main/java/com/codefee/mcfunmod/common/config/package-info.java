/**
 * Server config shared by all features ({@code config/mcfunmod.json}).
 *
 * <p>{@link ModConfig} loads the file once at startup into an immutable record. Each feature gets
 * its own section in it and receives only that section, so features do not read each other's settings.
 */
package com.codefee.mcfunmod.common.config;
