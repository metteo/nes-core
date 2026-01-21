package net.novaware.nes.core.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @param <T>
 *
 * @see java.util.Optional
 * @see java.util.concurrent.atomic.AtomicReference
 * @see jakarta.xml.ws.Holder
 */
public class Out<T> {
    private @Nullable T value;

    protected Out(@Nullable T value) {
        this.value = value;
    }

    public static <T> Out<T> of(@NonNull T value) {
        return new Out<>(value);
    }

    public static <T> Out<T> empty() {
        return new Out<>(null);
    }

    public @Nullable T put(@NonNull T value) {
        T previous = this.value;
        set(value);
        return previous;
    }

    // TODO: void put with Consumer of prev value

    public Out<T> set(@NonNull T value) {
        this.value = value;

        return this;
    }

    public @Nullable T get() {
        return value;
    }

    public Out<T> clear() {
        this.value = null;

        return this;
    }

    @SuppressWarnings("fenum:return") // TODO: consider disabling fenum for package / project
    public boolean isEmpty() {
        return value == null;
    }

    @SuppressWarnings("fenum:return")
    public boolean isPresent() {
        return value != null;
    }

    // equals & hashCode
    // toString
    // clone, nope!
    // can only be created empty
    // Ref class which is both ways
    // thread safety, no, only to call a method within the same thread
    // escape analysis?
    // fluent functional api?
    // Comparable?
    // primitives?
    // serializable, nope!
    // test
    // only for hot code to prevent allocations
    // allow only set, then get, not multiple sets? to prevent reuse? doesnt apply to Ref?
}
