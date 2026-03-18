package net.novaware.nes.core.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public class RecordingBus implements MemoryBus {

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

    private @Unsigned byte[] memory = new byte[CpuMemMap.MEMORY_SIZE];

    private final CycleCounter cycleCounter;
    private BusOp currentOp;

    private List<Op> activity = new ArrayList<>();

    private ShortRegister memoryAddress;
    private ByteRegister memoryData;

    @Inject
    public RecordingBus(
        @CpuVar(CC) CycleCounter cycleCounter
    ) {
        this.cycleCounter = cycleCounter;
        this.currentOp = BusOp.ADDRESS_ACCESS;

        // TODO: change these into latches?
        this.memoryAddress = new ShortRegister("MAR");
        this.memoryData = new ByteRegister("MDR");
    }

    public @Unsigned short address() {
        return memoryAddress.get();
    }

    public @Unsigned byte data2() { // TODO: figure out the conflict
        return memoryData.get();
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
    public void specify(@Unsigned short address) {
        cycleCounter.increment();

        memoryAddress.set(address);

        currentOp = BusOp.ADDRESS_ACCESS;
        activity.add(new Op(currentOp, address, UBYTE_0));
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte b = memory[memoryAddress.getAsInt()];
        memoryData.set(b);

        currentOp = BusOp.DATA_READ;
        activity.add(new Op(currentOp, memoryAddress.get(), b));

        return b;
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        memoryData.set(data);
        memory[memoryAddress.getAsInt()] = data;

        currentOp = BusOp.DATA_WRITE;
        activity.add(new Op(currentOp, memoryAddress.get(), data));
    }

    @Override
    public BusOp currentOp() {
        return currentOp;
    }

    @Override
    public void attach(MemoryDevice memoryDevice) {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        return specifyThen(address);
    }

    @Override
    public Read read() {
        return this;
    }

    @Override
    public Write write() {
        return this;
    }

    @Override
    public void data(@Unsigned byte data) {
        writeByte(data);
    }

    @Override
    public @Unsigned byte data() {
        return readByte();
    }
}
