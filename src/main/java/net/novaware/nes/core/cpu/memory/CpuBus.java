package net.novaware.nes.core.cpu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.apu.register.ApuRegisters;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.ShortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntPredicate;

import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_IO_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_TEST_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.APU_TEST_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_SIZE;
import static net.novaware.nes.core.cpu.memory.MemoryMap.CARTRIDGE_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_MIRROR_3_END;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_SIZE;
import static net.novaware.nes.core.cpu.memory.MemoryMap.RAM_START;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

// TODO: move to cpu part since ppu has it's own bus
public class CpuBus implements MemoryBus {

    public static final IntPredicate RAM_RANGE      = a -> sint(RAM_START) <= a                && a <= sint(RAM_MIRROR_3_END);
    public static final IntPredicate PPU_REGS_RANGE = a -> sint(PPU_REGISTERS_START) <= a      && a <= sint(PPU_REGISTERS_MIRROR_END);
    public static final IntPredicate APU_IO_RANGE   = a -> sint(APU_IO_REGISTERS_START) <= a   && a <= sint(APU_IO_REGISTERS_END);
    public static final IntPredicate APU_TEST_RANGE = a -> sint(APU_TEST_REGISTERS_START) <= a && a <= sint(APU_TEST_REGISTERS_END);
    public static final IntPredicate CARTRIDGE_RANGE = a -> sint(CARTRIDGE_START) <= a         && a <= sint(CARTRIDGE_END);

    private final CycleCounter cycleCounter;

    // TODO: MemoryBus only directs calls to respective MemoryDevices, it doesn't own them / create them
    private PhysicalMemory ram = new PhysicalMemory(RAM_SIZE);
    private ByteRegisterMemory ppuRegs = new PpuRegFile().asByteRegisterMemory();
    private ByteRegisterMemory apuIoRegs = new ApuRegisters().asByteRegisterMemory();
    private MemoryDevice cartridge = new PhysicalMemory(CARTRIDGE_SIZE, sint(CARTRIDGE_START)); // TODO: temporary

    // TODO: this belongs to memory bus as currentAddress variable / currentData variable for open bus
    // TODO: figure out better register names
    private AddressRegister memoryAddress = new ShortRegister("MAR?");
    private DataRegister memoryData = new ByteRegister("MDR?");

    private MemoryDevice currentSegment = ram;
    private IntPredicate currentRange = RAM_RANGE;
    private @Unsigned short currentAddress; // translated into specific segment range

    @Inject
    public CpuBus(
            @CpuVar(CC) CycleCounter cycleCounter
    ) {
        this.cycleCounter = cycleCounter;
    }

    @Override
    public void specify(@Unsigned short address) {
        memoryAddress.set(address);
        currentAddress = address;
        cycleCounter.increment();

        int addressInt = sint(address);

//        if (currentRange.test(addressInt)) {
//            return; // fast track
//        }

        if (RAM_RANGE.test(addressInt)) { // TODO: maybe switch to BankedMemory
            currentRange = RAM_RANGE;
            currentAddress = ushort(addressInt & sint(RAM_END));
            currentSegment = ram;

        } else if (PPU_REGS_RANGE.test(addressInt)) {
            currentRange = PPU_REGS_RANGE;
            currentSegment = ppuRegs;

        } else if (APU_IO_RANGE.test(addressInt)) {
            currentRange = APU_IO_RANGE;
            currentSegment = apuIoRegs;

        } else if (APU_TEST_RANGE.test(addressInt)) {
            // TODO: open bus
            throw new RuntimeException("TODO: open bus");

        } else if (CARTRIDGE_RANGE.test(addressInt)) { // TODO: cartridge can listen to full address range
            currentRange = CARTRIDGE_RANGE;
            currentSegment = cartridge;
        }
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte data = currentSegment.specifyThen(currentAddress).readByte();

        memoryData.set(data);
        return data;
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        memoryData.set(data);
        currentSegment.specifyThen(currentAddress).writeByte(data);
    }

    // FIXME: temp for design / testing ideas
    /* package */ ByteRegister getPpuRegs(int idx) {
        return ppuRegs.registers[idx];
    }

    // TODO: use attach to the bus for all memory devices, not only cartridge
    // TODO: try to implement page based indexing instead of if/else
    @Override
    public void attach(MemoryDevice memoryDevice) { // FIXME: what about the address range?
        cartridge = memoryDevice;
    }
}
