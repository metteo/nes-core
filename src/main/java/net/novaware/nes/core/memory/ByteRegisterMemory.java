package net.novaware.nes.core.memory;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public class ByteRegisterMemory implements MemoryDevice, Nameable {

    private final String name;

    private final @Unsigned short startAddress;
    private final @Unsigned short endAddress;

    private final ByteRegister[] registers;
    private final int mask;

    private int index;

    public ByteRegisterMemory(
        String name,
        @Unsigned short startAddress,
        @Unsigned short endAddress,

        ByteRegister[] registers
    ) {
        this.name = name;
        this.startAddress = startAddress;
        this.endAddress = endAddress;

        this.registers = registers;
        this.mask = registers.length - 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return endAddress;
    }

    @Override
    public void specify(@Unsigned short address) {
        int addressInt = sint(address) - sint(startAddress);
        index = addressInt & mask;
    }

    @Override
    public @Unsigned byte readByte() {
        return registers[index].get();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        registers[index].set(data);
    }
}
