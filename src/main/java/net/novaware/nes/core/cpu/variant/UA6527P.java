package net.novaware.nes.core.cpu.variant;

import net.novaware.nes.core.tv.ColorPal;

/**
 * Dendy / Pegasus PAL variant
 * CPU + DMA + APU + IO
 */
public interface UA6527P {

    int    CPU_CLOCK_DIVIDER   = 15;
    double CPU_CLOCK_FREQUENCY = ColorPal.SUBCARRIER * 6 / CPU_CLOCK_DIVIDER; // TODO: move Fsc * 6 to clocks

    int    APU_CLOCK_DIVIDER   = CPU_CLOCK_DIVIDER * 2;
    double APU_CLOCK_FREQUENCY = ColorPal.SUBCARRIER * 6 / APU_CLOCK_DIVIDER;

    // Frame Counter in the docs
    double APU_COUNTER_FREQUENCY = ColorPal.P_FRAME_RATE; // TODO: or 59?
}
