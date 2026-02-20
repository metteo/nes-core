package net.novaware.nes.core.memory;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public class ByteRegisterMemory implements MemoryDevice, Nameable {

    private final String name;

    private final int offset;
    /* package */ final ByteRegister[] registers; // TODO: make private
    private final int size;

    private int index;

    public ByteRegisterMemory(String name, int offset, ByteRegister[] registers) {
        this.name = name;
        this.offset = offset;

        this.registers = registers;
        this.size = registers.length;
    }

    @Override
    public void specify(@Unsigned short address) {
        int addressInt = sint(address) - offset;
        index = addressInt % size; // TODO: remainder is slow, see BankedMemory
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
