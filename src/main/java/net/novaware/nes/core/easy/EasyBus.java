package net.novaware.nes.core.easy;

import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.register.ByteRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.easy.EasyMap.CARTRIDGE_END;
import static net.novaware.nes.core.easy.EasyMap.CARTRIDGE_SIZE;
import static net.novaware.nes.core.easy.EasyMap.CARTRIDGE_START;
import static net.novaware.nes.core.easy.EasyMap.KEY_BYTE;
import static net.novaware.nes.core.easy.EasyMap.PICTURE_SEGMENT_END;
import static net.novaware.nes.core.easy.EasyMap.PICTURE_SEGMENT_SIZE;
import static net.novaware.nes.core.easy.EasyMap.PICTURE_SEGMENT_START;
import static net.novaware.nes.core.easy.EasyMap.RAM_END;
import static net.novaware.nes.core.easy.EasyMap.RAM_START;
import static net.novaware.nes.core.easy.EasyMap.RNG_BYTE;
import static net.novaware.nes.core.easy.EasyMap.STACK_SEGMENT_END;
import static net.novaware.nes.core.easy.EasyMap.STACK_SEGMENT_SIZE;
import static net.novaware.nes.core.easy.EasyMap.STACK_SEGMENT_START;
import static net.novaware.nes.core.easy.EasyMap.VECTOR_SEGMENT_END;
import static net.novaware.nes.core.easy.EasyMap.VECTOR_SEGMENT_SIZE;
import static net.novaware.nes.core.easy.EasyMap.VECTOR_SEGMENT_START;
import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Easy 6502 Bus implementation
 *
 * @see <a href="https://skilldrick.github.io/easy6502/">Easy6502 by skilldrick</a>
 */
public class EasyBus implements MemoryBus {

    private ByteRegister rng = new ByteRegister("RNG"); // 0x00FE
    private ByteRegister key = new ByteRegister("KEY"); // 0x00FF

    private final MemoryDevice ram = new PhysicalMemory(EasyMap.RAM_SIZE, sint(RAM_START));
    private final MemoryDevice regs = new ByteRegisterMemory("EZ_REGS", sint(RNG_BYTE), new ByteRegister[]{ rng, key });
    private final MemoryDevice stack = new PhysicalMemory(STACK_SEGMENT_SIZE, sint(STACK_SEGMENT_START));
    private final MemoryDevice vram = new PhysicalMemory(PICTURE_SEGMENT_SIZE, sint(PICTURE_SEGMENT_START));
    private final MemoryDevice rom = new PhysicalMemory(CARTRIDGE_SIZE, sint(CARTRIDGE_START));
    private final MemoryDevice vectors = new PhysicalMemory(VECTOR_SEGMENT_SIZE, sint(VECTOR_SEGMENT_START));

    private MemoryDevice currentSegment = ram;
    private @Unsigned short currentAddress; // translated into specific segment range

    @Override
    public void specify(@Unsigned short address) {
        currentAddress = address;

        int addrVal = sint(address);
        if (sint(RAM_START) <= addrVal && addrVal <= sint(RAM_END)) {
            currentSegment = ram;
        } else if (addrVal == sint(RNG_BYTE) || addrVal == sint(KEY_BYTE)) {
            currentSegment = regs;
        } else if (sint(STACK_SEGMENT_START) <= addrVal && addrVal <= sint(STACK_SEGMENT_END)) {
            currentSegment = stack;
        } else if (sint(PICTURE_SEGMENT_START) <= addrVal && addrVal <= sint(PICTURE_SEGMENT_END)) {
            currentSegment = vram;
        } else if (sint(CARTRIDGE_START) <= addrVal && addrVal <= sint(CARTRIDGE_END)) {
            currentSegment = rom;
        } else if (sint(VECTOR_SEGMENT_START) <= addrVal && addrVal <= sint(VECTOR_SEGMENT_END)) {
            currentSegment = vectors;
        } else {
            throw new IllegalStateException("impossibru!");
        }

        currentSegment.specify(currentAddress);
    }

    @Override
    public @Unsigned byte readByte() {
        return currentSegment.readByte();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentSegment.writeByte(data);
    }

    @Override
    public void attach(MemoryDevice memoryDevice) {
        throw new UnsupportedOperationException("not implemented!");
    }
}
