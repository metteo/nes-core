package net.novaware.nes.core.memory;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.memory.RecordingBus.OpType.*;
import static net.novaware.nes.core.memory.RecordingBus.OpType.READ;
import static net.novaware.nes.core.memory.RecordingBus.OpType.WRITE;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public class RecordingBus implements MemoryBus {

    public record Op (
            OpType type,
            @Unsigned short address,
            @Unsigned byte data
    ) {
        public Op {}

        public Op(OpType type, int address, int data) {
            this(type, ushort(address), ubyte(data));
        }

        public String toTest() {
            String type = switch (this.type) {
                case ACCESS -> "ACCESS,";
                case READ   -> "READ,  ";
                case WRITE  -> "WRITE, ";
            };
            return "new Op(" + type + " 0x" + Hex.s(address).toUpperCase() + ", 0x" + Hex.s(data).toUpperCase() + "),";
        }
    }

    public enum OpType {
        ACCESS, READ, WRITE
    }

    private @Unsigned byte[] memory = new byte[0xFFFF];

    private final CycleCounter cycleCounter;
    private List<Op> activity = new ArrayList<>();

    private ShortRegister memoryAddress;
    private ByteRegister memoryData;

    public RecordingBus(
            CycleCounter cycleCounter,
            ShortRegister memoryAddress,
            ByteRegister memoryData
    ) {
        this.cycleCounter = cycleCounter;
        this.memoryAddress = memoryAddress;
        this.memoryData = memoryData;
    }

    public RecordingBus() {
        this.cycleCounter = new CycleCounter("CPUCC");
        this.memoryAddress = new ShortRegister("MAR");
        this.memoryData = new ByteRegister("MDR");
    }

    public @Unsigned short address() {
        return memoryAddress.get();
    }

    public @Unsigned byte data() {
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

        activity.add(new Op(ACCESS, address, UBYTE_0));
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte b = memory[memoryAddress.getAsInt()];
        memoryData.set(b);
        activity.add(new Op(READ, memoryAddress.get(), b));

        return b;
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        memoryData.set(data);
        memory[memoryAddress.getAsInt()] = data;
        activity.add(new Op(WRITE, memoryAddress.get(), data));
    }

    @Override
    public void attach(MemoryDevice memoryDevice) {
        throw new UnsupportedOperationException("not implemented!");
    }
}
