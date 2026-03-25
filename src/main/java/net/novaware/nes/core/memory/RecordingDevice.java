package net.novaware.nes.core.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_START;
import static net.novaware.nes.core.memory.BusOp.ADDRESS_ACCESS;
import static net.novaware.nes.core.memory.BusOp.CONTROL_READ;
import static net.novaware.nes.core.memory.BusOp.CONTROL_WRITE;
import static net.novaware.nes.core.memory.BusOp.DATA_READ;
import static net.novaware.nes.core.memory.BusOp.DATA_WRITE;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class RecordingDevice implements MemoryDevice.ReadWrite {

    public record Op (
            BusOp type,
            @Unsigned short address,
            @Unsigned byte data
    ) {
        public Op {}

        public Op(BusOp type, int address, int data) {
            this(type, ushort(address), ubyte(data));
        }

        public String toTest() {
            String type = switch (this.type) {
                case ADDRESS_ACCESS -> "ADDRESS_ACCESS,";
                case CONTROL_READ   -> "CONTROL_READ,  ";
                case CONTROL_WRITE  -> "CONTROL_WRITE, ";
                case DATA_READ      -> "DATA_READ,     ";
                case DATA_WRITE     -> "DATA_WRITE,    ";
            };
            return "new Op(" + type + " 0x" + Hex.s(address).toUpperCase() + ", 0x" + Hex.s(data).toUpperCase() + "),";
        }
    }

    private DataBus.Line dataLine = new OpenLine();

    private @Unsigned short addressLatch;
    private @Unsigned byte dataLatch;

    private final CycleCounter cycleCounter;
    private List<Op> activity = new ArrayList<>();

    @Inject
    public RecordingDevice(
        @CpuVar(CC) CycleCounter cycleCounter
    ) {
        this.cycleCounter = cycleCounter;
    }

    public void record() {
        activity.clear();
        cycleCounter.mark();
    }

    public long cycles() {
        return cycleCounter.diff();
    }

    public CycleCounter cycleCounter() {
        return cycleCounter;
    }

    public List<Op> activity() {
        return activity;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return MEMORY_START;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return MEMORY_END;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        this.addressLatch = address;

        activity.add(new Op(ADDRESS_ACCESS, address, UBYTE_0));
    }

    @Override
    public void onRead() {
        dataLatch = dataLine.data(); // listen without altering

        activity.add(new Op(CONTROL_READ, addressLatch, UBYTE_0));
        activity.add(new Op(DATA_READ, addressLatch, dataLatch));
    }

    @Override
    public void onWrite() {
        dataLatch = dataLine.data();

        activity.add(new Op(CONTROL_WRITE, addressLatch, UBYTE_0));
        activity.add(new Op(DATA_WRITE, addressLatch, dataLatch));
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }

    @Override
    public String toString() {
        return "REC (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
