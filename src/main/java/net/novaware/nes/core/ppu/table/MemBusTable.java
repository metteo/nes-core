package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.memory.PpuBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Hex;

public abstract class MemBusTable {

    protected final String name;
    protected final SegmentRegister segment; // TODO: consider immutable variant
    protected final PpuBus bus;

    protected MemBusTable(
        String name,
        SegmentRegister segment,
        PpuBus bus
    ) {
        this.name = name;
        this.segment = segment;
        this.bus = bus;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + Hex.s(segment.getStart()) + ":" + Hex.s(segment.getEnd()) + ")";
    }

}
