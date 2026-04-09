package net.novaware.nes.core.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Collection of assertions for method arguments and object state.
 *
 */
public final class Asserts {

    private static final String MESSAGE_NOT_NULL = "message must not be null";
    private static final String MESSAGE_SUPPLIER_NOT_NULL = "message supplier must not be null";

    private Asserts() {
        //utility class
    }

    public static void assertArgument(final boolean assertion, final String message) {
        requireNonNull(message, MESSAGE_NOT_NULL);

        if (!assertion) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgument(final boolean assertion, final Supplier<String> message) {
        requireNonNull(message, MESSAGE_SUPPLIER_NOT_NULL);

        assertArgument(assertion, message.get());
    }

    @SuppressWarnings("nullness")
    public static <T> @NonNull T assertNonNull(final @Nullable T object, final String message) {
        requireNonNull(message, MESSAGE_NOT_NULL);
        return requireNonNull(object, message);
    }

    @SuppressWarnings("nullness")
    public static <T> @NonNull T assertNonNull(final @Nullable T object, final Supplier<String> message) {
        requireNonNull(message, MESSAGE_SUPPLIER_NOT_NULL);
        return assertNonNull(object, message.get());
    }

    public static void assertState(final boolean assertion, final String message) {
        requireNonNull(message, MESSAGE_NOT_NULL);

        if (!assertion) {
            throw new IllegalStateException(message);
        }
    }

    public static void assertState(final boolean assertion, final Supplier<String> message) {
        requireNonNull(message, MESSAGE_SUPPLIER_NOT_NULL);

        assertState(assertion, message.get());
    }
}
