package net.novaware.nes.core.cpu.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.apu.memory.ApuMemDevice;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.dma.memory.DmaMemDevice;
import net.novaware.nes.core.io.register.IoRegFile;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DMA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.JOY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.OS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ZP;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_SIZE;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IRQ_VECTOR;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.NMI_VECTOR;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.OAM_SEGMENT_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.OAM_SEGMENT_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_SIZE;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RES_VECTOR;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.STACK_SEGMENT_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.STACK_SEGMENT_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.TIMER_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.TIMER_REGISTERS_SIZE;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.TIMER_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.ZERO_PAGE_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.ZERO_PAGE_START;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

@Module
public interface CpuMemModule {

    @Provides
    @BoardScope
    @CpuVar(RAM)
    static MemoryDevice.ReadWrite provideMemory() {
        PhysicalMemory ram = new PhysicalMemory(RAM.name(), RAM_START, RAM_MIRROR_END, RAM_SIZE)
            .fill(() -> UBYTE_MAX_VALUE); // TODO: use configurable filler, nestest.nes expects 0x00s
        return ram;
    }

    @Binds
    @BoardScope
    @CpuVar(DMA)
    MemoryDevice.WriteOnly bindOamDma(DmaMemDevice dmaMemDevice);

    @Binds
    @BoardScope
    @CpuVar(CpuVarName.APU)
    MemoryDevice.ReadWrite bindApuStatus(ApuMemDevice apuMemDevice);

    @Provides
    @BoardScope
    @CpuVar(JOY)
    static MemoryDevice.ReadWrite provideJoy(IoRegFile joyRegFile) {
        return new ByteRegisterMemory( // FIXME: replace with a proper device separating reads and writes
                "JOY_REGS",
                IO_REGISTERS_START, IO_REGISTERS_END,
                new ByteRegister[]{ joyRegFile.getJoy1Data(), joyRegFile.getJoy2Data() }
        );
    }

    @Provides
    @BoardScope
    @CpuVar(CpuVarName.ATM)
    static MemoryDevice.ReadWrite provideApuTestModeRegs() { // TODO: change into a proper device
        return new PhysicalMemory("ATM", APU_TEST_REGISTERS_START, APU_TEST_REGISTERS_END, APU_TEST_REGISTERS_SIZE);
    }

    @Provides
    @BoardScope
    @CpuVar(CpuVarName.TMR)
    static MemoryDevice.ReadWrite provideTimerRegs() { // TODO: change into proper device
        return new PhysicalMemory("TMR", TIMER_REGISTERS_START, TIMER_REGISTERS_END, TIMER_REGISTERS_SIZE);
    }

    @Binds
    @BoardScope
    @CpuVar(BUS)
    MemoryBus bindMemoryBus(CpuBus cpuBus);

    @Provides
    @BoardScope
    @CpuVar(ZP)
    static SegmentRegister provideZeroPage() {
        SegmentRegister zeroPage = new SegmentRegister(ZP.name());
        zeroPage.setStart(ZERO_PAGE_START);
        zeroPage.setEnd(ZERO_PAGE_END);

        return zeroPage;
    }

    @Provides
    @BoardScope
    @CpuVar(SS)
    static SegmentRegister provideStackSegment() {
        SegmentRegister stackSegment = new SegmentRegister(SS.name());
        stackSegment.setStart(STACK_SEGMENT_START);
        stackSegment.setEnd(STACK_SEGMENT_END);

        return stackSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(OS)
    static SegmentRegister provideOamSegment() {
        SegmentRegister oamSegment = new SegmentRegister(OS.name());
        oamSegment.setStart(OAM_SEGMENT_START);
        oamSegment.setEnd(OAM_SEGMENT_END);

        return oamSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(CS)
    static SegmentRegister provideCodeSegment() {
        return new SegmentRegister(CS.name()); // configured during cartridge attach
    }

    @Provides
    @BoardScope
    @CpuVar(DS)
    static SegmentRegister provideDataSegment() {
        return new SegmentRegister(DS.name()); // configured during cartridge attach
    }

    @Provides
    @BoardScope
    @CpuVar(NMI)
    static ShortRegister provideNmiVector() {
        ShortRegister vector = new ShortRegister(NMI.doc());
        vector.set(NMI_VECTOR);

        return vector;
    }

    @Provides
    @BoardScope
    @CpuVar(RES)
    static ShortRegister provideResVector() {
        ShortRegister vector = new ShortRegister(RES.doc());
        vector.set(RES_VECTOR);

        return vector;
    }

    @Provides
    @BoardScope
    @CpuVar(IRQ)
    static ShortRegister provideIrqVector() {
        ShortRegister vector = new ShortRegister(IRQ.doc());
        vector.set(IRQ_VECTOR);

        return vector;
    }
}
