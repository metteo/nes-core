package net.novaware.nes.core.board.inject;

import jakarta.inject.Scope;
import net.novaware.nes.core.board.Board;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector only instantiates once per {@link Board}. Not inherited.
 *
 * @see jakarta.inject.Scope @Scope
 * @see jakarta.inject.Singleton @Singleton
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface BoardScope {
}
