package net.novaware.nes.core.cpu.variant;

import net.novaware.nes.core.tv.ColorNtsc;

/**
 * Brazil / Argentina variant
 * CPU + DMA + APU + IO
 */
public interface UA6527 {

    // FIXME: remove cpu / apu frequency from other variants since it's master clock specific
    int CPU_CLOCK_DIVIDER = 12;
    int APU_CLOCK_DIVIDER = CPU_CLOCK_DIVIDER * 2;

    // Frame Counter in the docs
    double APU_COUNTER_FREQUENCY = ColorNtsc.P_FRAME_RATE; // TODO: or exact 60?
}
