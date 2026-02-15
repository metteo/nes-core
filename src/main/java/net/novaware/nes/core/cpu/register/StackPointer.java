package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.register.ByteRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class StackPointer extends ByteRegister {

    private final @Unsigned short page = MemoryMap.STACK_SEGMENT_START;

    public StackPointer(String name) {
        super(name);
    }

    public @Unsigned short address() {
        return ushort(addressAsInt());
    }

    public int addressAsInt() {
        return uint(page) | getAsInt();
    }

    public void decrement() {
        setAsByte(getAsInt() - 1);
    }

    public void increment() {
        setAsByte(getAsInt() + 1);
    }
}
