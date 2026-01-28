package net.novaware.nes.core.memory;

import net.novaware.nes.core.register.ByteRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class ByteRegisterMemory extends AbstractAddressable implements Addressable {

    final ByteRegister[] registers; // TODO: make private
    private final int size;

    public ByteRegisterMemory(String name, ByteRegister[] registers) {
        super(name);

        this.registers = registers;
        this.size = registers.length;
    }

    @Override
    public @Unsigned byte read(@Unsigned short address) {
        int addressInt = uint(address);
        int index = addressInt % size;

        return registers[index].get();
    }

    @Override
    public void write(@Unsigned short address, @Unsigned byte data) {
        int addressInt = uint(address);
        int index = addressInt % size;

        registers[index].set(data);
    }
}
