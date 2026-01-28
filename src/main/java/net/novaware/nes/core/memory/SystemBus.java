package net.novaware.nes.core.memory;

import net.novaware.nes.core.apu.ApuRegisterFile;
import net.novaware.nes.core.ppu.PpuRegisterFile;
import net.novaware.nes.core.register.ByteRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntPredicate;

import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_IO_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_TEST_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_TEST_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_SIZE;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.PPU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_MIRROR_3_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_SIZE;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_START;
import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

// TODO: move to cpu part since ppu has it's own bus
public class SystemBus implements AddressBus<SystemBus>, DataBus {

    public static final IntPredicate RAM_RANGE      = a -> uint(RAM_START) <= a                && a <= uint(RAM_MIRROR_3_END);
    public static final IntPredicate PPU_REGS_RANGE = a -> uint(PPU_REGISTERS_START) <= a      && a <= uint(PPU_REGISTERS_MIRROR_END);
    public static final IntPredicate APU_IO_RANGE   = a -> uint(APU_IO_REGISTERS_START) <= a   && a <= uint(APU_IO_REGISTERS_END);
    public static final IntPredicate APU_TEST_RANGE = a -> uint(APU_TEST_REGISTERS_START) <= a && a <= uint(APU_TEST_REGISTERS_END);
    public static final IntPredicate CARTRIDGE_RANGE = a -> uint(CARTRIDGE_START) <= a         && a <= uint(CARTRIDGE_END);


    private PhysicalMemory ram = new PhysicalMemory(RAM_SIZE);
    private ByteRegisterMemory ppuRegs = new PpuRegisterFile().asByteRegisterMemory();
    private ByteRegisterMemory apuIoRegs = new ApuRegisterFile().asByteRegisterMemory();
    private PhysicalMemory cartridge = new PhysicalMemory(CARTRIDGE_SIZE); // TODO: temporary

    private @Unsigned short address;

    private Addressable currentSegment = ram;
    private IntPredicate currentRange = RAM_RANGE;
    private @Unsigned short currentAddress;

    @Override
    public void specify(@Unsigned short address) {
        this.address = address;

        int addressInt = uint(address);

//        if (currentRange.test(addressInt)) {
//            return; // fast track
//        }

        if (RAM_RANGE.test(addressInt)) {
            currentRange = RAM_RANGE;
            currentAddress = ushort(addressInt & uint(RAM_END));
            currentSegment = ram;

        } else if (PPU_REGS_RANGE.test(addressInt)) {
            currentRange = PPU_REGS_RANGE;
            currentAddress = ushort(addressInt & uint(PPU_REGISTERS_END));
            currentSegment = ppuRegs;

        } else if (APU_IO_RANGE.test(addressInt)) {
            currentRange = APU_IO_RANGE;
            currentAddress = address;
            currentSegment = apuIoRegs;

        } else if (APU_TEST_RANGE.test(addressInt)) {
            // TODO: open bus
            throw new RuntimeException("TODO: open bus");

        } else if (CARTRIDGE_RANGE.test(addressInt)) {
            currentRange = CARTRIDGE_RANGE;
            currentAddress = address;
            currentSegment = cartridge;
        }
    }

    @Override
    public SystemBus specifyAnd(@Unsigned short address) {
        specify(address);
        return this;
    }

    @Override
    public @Unsigned byte readByte() {
        return currentSegment.read(currentAddress);
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentSegment.write(currentAddress, data);
    }

    // FIXME: temp for design / testing ideas
    /* package */ ByteRegister getPpuRegs(int idx) {
        return ppuRegs.registers[idx];
    }
}
