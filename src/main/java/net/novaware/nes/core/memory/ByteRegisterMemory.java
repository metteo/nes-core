package net.novaware.nes.core.memory;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.sint;

public class ByteRegisterMemory implements MemoryDevice, Nameable {

    private final String name;

    final ByteRegister[] registers; // TODO: make private
    private int index;
    private final int size;

    public ByteRegisterMemory(String name, ByteRegister[] registers) {
        this.name = name;
        this.registers = registers;
        this.size = registers.length;
    }

    @Override
    public void specify(@Unsigned short address) {
        int addressInt = sint(address);
        index = addressInt % size;
    }

    @Override
    public @Unsigned byte readByte() {
        return registers[index].get();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        registers[index].set(data);
    }

    @Override
    public String getName() {
        return name;
    }
}
