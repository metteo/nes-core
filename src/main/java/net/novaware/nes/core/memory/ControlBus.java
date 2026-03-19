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

    interface Device {}

    interface ReadOnlyDevice extends Device {
        /**
         * 3 cases:
         *  - listen - check existing bus data, return without changing (for snooping)
         *  - override - ignore existing, set own
         *  - bus conflict - AND own data with existing data
         */
        void onRead();
    }

    interface WriteOnlyDevice extends Device {
        void onWrite();
    }

    interface ReadWriteDevice extends ReadOnlyDevice, WriteOnlyDevice {}

    // endregion
}
