package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

@FunctionalInterface
public interface UByteSupplier {
    @Unsigned byte getAsUByte();
}
