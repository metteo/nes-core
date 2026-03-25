package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Bus interface hiding memory details from CPU
 *
 * https://stackoverflow.com/questions/8134545/difference-between-memory-bus-and-address-bus
 * http://www-mdp.eng.cam.ac.uk/web/library/enginfo/mdp_micro/lecture1/lecture1-3-1.html
 */
public interface DataBus {

    interface Mode {}

    interface Read extends Mode {
        @Unsigned byte data();
    }

    interface Write extends Mode {
        void data(@Unsigned byte data);
    }

    interface Line extends Read, Write {}

    interface Device {
        void onAttach(DataBus.Line dataLine);
        void onDetach();
    }
}
