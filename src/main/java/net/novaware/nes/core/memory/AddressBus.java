package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface AddressBus<T extends AddressBus<T>> {

    /**
     * Specify memory location for read or write.
     * @param address
     *
     * @see <a href="https://en.wikipedia.org/wiki/Bus_(computing)#Address_bus">Address Bus</a>
     */
    void specify(final @Unsigned short address);

    T specifyAnd(@Unsigned short address);
}
