package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Hex;

public abstract class MemBusTable {

    protected final String name;
    protected final SegmentRegister segment; // TODO: consider immutable variant
    protected final MemoryBus bus;

    protected MemBusTable(
        String name,
        SegmentRegister segment,
        MemoryBus bus
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
