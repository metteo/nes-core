package net.novaware.nes.core.memory;

public enum BusOp {
    /**
     * {@link AddressBus.Line#access(short)}
     */
    ADDRESS_ACCESS,

    /**
     * {@link ControlBus.Line#read()}
     */
    CONTROL_READ,

    /**
     * {@link ControlBus.Line#write()}
     */
    CONTROL_WRITE,

    /**
     * {@link DataBus.Read#data()}
     */
    DATA_READ,

    /**
     * {@link DataBus.Write#data(byte)}
     */
    DATA_WRITE,
    ;
}
