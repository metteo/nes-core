package net.novaware.nes.core;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector only instantiates once per {@link net.novaware.nes.core.Board}. Not inherited.
 *
 * @see jakarta.inject.Scope @Scope
 * @see jakarta.inject.Singleton @Singleton
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface BoardScope {
}
