package net.novaware.nes.core.memory;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Control_bus">Control bus on wikipedia.org</a>
 */
public interface ControlBus {

    interface Line {

        BusOp currentOp();

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
}
