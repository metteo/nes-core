package net.novaware.nes.core.cpu.variant;

import net.novaware.nes.core.tv.ColorNtsc;

/**
 * NTSC / RGB variant
 * CPU + DMA + APU + IO
 */
public interface RP2A03 { // TODO: consider moving these into single file as subinterfaces

    int    CPU_CLOCK_DIVIDER   = 12;
    double CPU_CLOCK_FREQUENCY = ColorNtsc.SUBCARRIER * 6 / CPU_CLOCK_DIVIDER; // TODO: move Fsc * 6 to clocks

    int    APU_CLOCK_DIVIDER   = CPU_CLOCK_DIVIDER * 2;
    double APU_CLOCK_FREQUENCY = ColorNtsc.SUBCARRIER * 6 / APU_CLOCK_DIVIDER;

    // Frame Counter in the docs
    double APU_COUNTER_FREQUENCY = ColorNtsc.P_FRAME_RATE; // TODO: or exact 60?
}
