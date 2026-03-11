package net.novaware.nes.core.cpu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenBus;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntPredicate;

import static net.novaware.nes.core.cpu.inject.CpuVarName.APU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_FDS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_FDS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_START;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class CpuBus implements MemoryBus {

    public static final IntPredicate RAM_RANGE      = a -> sint(RAM_START) <= a                && a <= sint(RAM_MIRROR_END);
    public static final IntPredicate PPU_REGS_RANGE = a -> sint(PPU_REGISTERS_START) <= a      && a <= sint(PPU_REGISTERS_MIRROR_END);
    public static final IntPredicate APU_RANGE   =    a -> sint(APU_REGISTERS_START) <= a   && a <= sint(APU_REGISTERS_END);
    public static final IntPredicate IO_RANGE   =     a -> sint(IO_REGISTERS_START) <= a   && a <= sint(IO_REGISTERS_END);
    public static final IntPredicate APU_TEST_RANGE = a -> sint(APU_TEST_REGISTERS_START) <= a && a <= sint(APU_TEST_REGISTERS_END);
    public static final IntPredicate CARTRIDGE_FDS_RANGE = a -> sint(CARTRIDGE_FDS_START) <= a && a <= sint(CARTRIDGE_FDS_END);
    public static final IntPredicate CARTRIDGE_RANGE = a -> sint(CARTRIDGE_START) <= a         && a <= sint(CARTRIDGE_END);

    @Used
    private final CycleCounter cycleCounter;

    @Used
    private MemoryDevice ram;

    @Used
    private MemoryDevice ppuRegs;

    @Used
    private MemoryDevice apuRegs;

    @Used
    private MemoryDevice ioRegs;

    @Used
    private MemoryDevice apuTestRegs;

    @Used
    private MemoryDevice cartridge = new OpenBus(CARTRIDGE_START, CARTRIDGE_END);

    private MemoryDevice currentSegment;
    private IntPredicate currentRange;
    private @Unsigned short addressLatch; // translated into specific segment range

    @Inject
    public CpuBus(
        @CpuVar(CC) CycleCounter cycleCounter,
        @CpuVar(RAM) MemoryDevice ram,
        @CpuVar(PPU) MemoryDevice ppuRegs,
        @CpuVar(APU) MemoryDevice apuRegs,
        @CpuVar(APU) MemoryDevice ioRegs, // TODO: IO
        @CpuVar(APU) MemoryDevice apuTestRegs, // TODO: apu test
        @CpuVar(APU) MemoryDevice timer, // TODO: timer
        @CpuVar(APU) MemoryDevice fdsRegs // TODO: FDS
    ) {
        this.cycleCounter = cycleCounter;
        this.ram = ram;
        this.ppuRegs = ppuRegs;
        this.apuRegs = apuRegs;
        this.ioRegs = ioRegs;
        this.apuTestRegs = apuTestRegs;

        currentSegment = ram;
        currentRange = RAM_RANGE;
    }

    @Override
    public void specify(@Unsigned short address) {
        addressLatch = address;
        cycleCounter.increment();

        int addressInt = sint(address);

//        if (currentRange.test(addressInt)) {
//            return; // fast track
//        }

        // TODO: try to implement page based indexing instead of if/else

        if (RAM_RANGE.test(addressInt)) { // TODO: maybe switch to BankedMemory
            currentRange = RAM_RANGE;
            addressLatch = ushort(addressInt & sint(RAM_END));
            currentSegment = ram;

        } else if (PPU_REGS_RANGE.test(addressInt)) {
            currentRange = PPU_REGS_RANGE;
            currentSegment = ppuRegs;

        } else if (APU_RANGE.test(addressInt)) {
            currentRange = APU_RANGE;
            currentSegment = apuRegs;

        } else if (IO_RANGE.test(addressInt)) {
            currentRange = IO_RANGE;
            currentSegment = apuRegs;

        } else if (APU_TEST_RANGE.test(addressInt)) {
            // TODO: open bus
            throw new RuntimeException("TODO: open bus");

        } else if (CARTRIDGE_FDS_RANGE.test(addressInt)) {
            currentRange = CARTRIDGE_FDS_RANGE;
            currentSegment = cartridge;

        } else if (CARTRIDGE_RANGE.test(addressInt)) {
            // TODO: cartridge can listen to full address range, not only 4020-FFFF
            // TODO: cartridge can listen to all address bus calls since they don't break anything
            // TODO: cartridge can listen to all writes since the cpu is the source (multiple devices may accept)
            // TODO: cartridge can only respond to reads if there is no other device responding (how to detect open bus?)
            currentRange = CARTRIDGE_RANGE;
            currentSegment = cartridge;
        }
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte data = currentSegment.specifyThen(addressLatch).readByte();

        return data;
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentSegment.specifyThen(addressLatch).writeByte(data);
    }

    @Override
    public void attach(MemoryDevice memoryDevice) { // FIXME: what about the address range?
        cartridge = memoryDevice;
    }
}
