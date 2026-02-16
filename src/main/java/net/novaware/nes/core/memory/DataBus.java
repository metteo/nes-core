package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Bus interface hiding memory details from CPU
 *
 * https://stackoverflow.com/questions/8134545/difference-between-memory-bus-and-address-bus
 * http://www-mdp.eng.cam.ac.uk/web/library/enginfo/mdp_micro/lecture1/lecture1-3-1.html
 */
public interface DataBus {

    /**
     * Read byte from memory under address specified using {@link AddressBus#specify(short)}
     *
     * @return byte of data
     */
    @Unsigned byte readByte();

    /**
     * Write byte into memory under address specified using {@link AddressBus#specify(short)}
     */
    void writeByte(final @Unsigned byte data);
}
