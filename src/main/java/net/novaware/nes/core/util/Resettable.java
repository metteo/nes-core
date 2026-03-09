package net.novaware.nes.core.util;

// FIXME: this is confusing with hardware reset sequence
public interface Resettable {

    default void reset() {}
}
