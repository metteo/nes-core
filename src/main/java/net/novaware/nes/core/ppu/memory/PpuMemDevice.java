package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.cpu.memory.CpuBusBridge;

public class PpuMemDevice implements CpuBusBridge {

    // TODO: direct < 0x3EFF traffic to PpuBus, >0x3F00 to Palette RAM
}
