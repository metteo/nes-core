package net.novaware.nes.core.memory;

public enum BusOp {
    /**
     * {@link AddressBus#specify(short)}
     */
    ADDRESS,
    /**
     * {@link DataBus#readByte()}
     */
    READ,
    /**
     * {@link DataBus#writeByte(byte)}
     */
    WRITE,
    ;
}
