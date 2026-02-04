package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface Addressable {

    @Unsigned byte read(@Unsigned short address);

    void write(@Unsigned short address, @Unsigned byte data);
}
