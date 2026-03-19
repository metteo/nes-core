package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

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

    // region Experimental API

    interface Mode {}

    interface Read extends Mode {
        @Unsigned byte data();
    }

    interface Write extends Mode {
        void data(@Unsigned byte data);
    }

    interface Line extends Read, Write {}

    class TempLine implements Line {

        private boolean openBus = true;
        private @Unsigned byte previous = UBYTE_MAX_VALUE;
        private @Unsigned byte current = UBYTE_MAX_VALUE;

        public boolean isOpenBus() {
            return openBus;
        }

        public @Unsigned byte cycle() {
            @Unsigned byte result = openBus ? previous : current;

            openBus = true;
            previous = current;
            current = UBYTE_MAX_VALUE;

            return result;
        }

        @Override
        public @Unsigned byte data() {
            return current;
        }

        @Override
        public void data(@Unsigned byte data) {
            openBus = false;
            this.current = data;
        }
    }

    class OpenCircuit implements Line {

        private @Unsigned byte value = UBYTE_MAX_VALUE;

        @Override
        public @Unsigned byte data() {
            return value;
        }

        @Override
        public void data(@Unsigned byte data) {
            value = data;
        }
    }

    interface Device {
        void onAttach(DataBus.Line dataLine);
        void onDetach();
    }

    // endregion
}
