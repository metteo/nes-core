package net.novaware.nes.core.memory;

/**
 * Keeps track if current cycle is a read or write operation.
 * {@link DataBus} methods specify implicitly the RW̅ signal
 */
public interface ControlBus {

    BusOp currentOp();

    // region Experimental API

    interface Line {
        DataBus.Read read();

        DataBus.Write write();
    }

    // endregion
}
