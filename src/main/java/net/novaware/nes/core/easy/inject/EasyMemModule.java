package net.novaware.nes.core.easy.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.easy.memory.EasyBus;
import net.novaware.nes.core.easy.memory.EasyMemMap;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.JOY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RNG;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.easy.memory.EasyMemMap.PICTURE_SEGMENT_END;
import static net.novaware.nes.core.easy.memory.EasyMemMap.PICTURE_SEGMENT_SIZE;
import static net.novaware.nes.core.easy.memory.EasyMemMap.PICTURE_SEGMENT_START;
import static net.novaware.nes.core.easy.memory.EasyMemMap.RAM_END;
import static net.novaware.nes.core.easy.memory.EasyMemMap.RAM_SIZE;
import static net.novaware.nes.core.easy.memory.EasyMemMap.RAM_START;
import static net.novaware.nes.core.easy.memory.EasyMemMap.STACK_SEGMENT_END;
import static net.novaware.nes.core.easy.memory.EasyMemMap.STACK_SEGMENT_SIZE;
import static net.novaware.nes.core.easy.memory.EasyMemMap.STACK_SEGMENT_START;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LC;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

@Module
public interface EasyMemModule {

    @Provides
    @BoardScope
    @CpuVar(RAM)
    static MemoryDevice.ReadWrite provideMemory() {
        return new PhysicalMemory("RAM", RAM_START, RAM_END, RAM_SIZE)
            .fill(() -> UBYTE_MAX_VALUE); // TODO: use configurable filler
    }

    @Provides
    @BoardScope
    @CpuVar(SS)
    static MemoryDevice.ReadWrite provideStack() {
        return new PhysicalMemory("STACK", STACK_SEGMENT_START, STACK_SEGMENT_END, STACK_SEGMENT_SIZE)
            .fill(() -> UBYTE_MAX_VALUE); // TODO: use configurable filler
    }

    @Provides
    @BoardScope
    @PpuVar(LC)
    static IntegerCounter provideLineCounter() {
        return new IntegerCounter(LC.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(DC)
    static IntegerCounter provideDotCounter() {
        return new IntegerCounter(DC.doc());
    }

    @Provides
    @BoardScope
    @CpuVar(PPU)
    static MemoryDevice.ReadWrite provideVideoMemory() {
        return new PhysicalMemory("VRAM", PICTURE_SEGMENT_START, PICTURE_SEGMENT_END, PICTURE_SEGMENT_SIZE)
            .fill(() -> UBYTE_MAX_VALUE); // TODO: use configurable filler;
    }

    @Binds
    @BoardScope
    @CpuVar(BUS)
    MemoryBus bindMemoryBus(EasyBus easyBus);

    @Provides
    @BoardScope
    @CpuVar(RNG)
    static DelegatingRegister provideRng(@CpuVar(BUS) MemoryBus memoryBus) {
        DelegatingRegister reg = new DelegatingRegister(RNG.name());
        reg.configureMemory(memoryBus, EasyMemMap.RNG_BYTE);
        return reg;
    }

    @Provides
    @BoardScope
    @CpuVar(JOY)
    static DelegatingRegister provideJoy(@CpuVar(BUS) MemoryBus memoryBus) {
        DelegatingRegister reg = new DelegatingRegister(JOY.name());
        reg.configureMemory(memoryBus, EasyMemMap.JOY_BYTE);
        return reg;
    }

    @Provides
    @BoardScope
    @CpuVar(SS)
    static SegmentRegister provideStackSegment() {
        SegmentRegister stackSegment = new SegmentRegister(SS.name());
        stackSegment.setStart(EasyMemMap.STACK_SEGMENT_START);

        return stackSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(CS)
    static SegmentRegister provideCodeSegment() {
        return new SegmentRegister(CS.name());
    }

    @Provides
    @BoardScope
    @CpuVar(NMI)
    static ShortRegister provideNmiVector() {
        ShortRegister vector = new ShortRegister(NMI.doc());
        vector.set(EasyMemMap.NMI_VECTOR);

        return vector;
    }

    @Provides
    @BoardScope
    @CpuVar(RES)
    static ShortRegister provideResVector() {
        ShortRegister vector = new ShortRegister(RES.doc());
        vector.set(EasyMemMap.RES_VECTOR);

        return vector;
    }

    @Provides
    @BoardScope
    @CpuVar(IRQ)
    static ShortRegister provideIrqVector() {
        ShortRegister vector = new ShortRegister(IRQ.doc());
        vector.set(EasyMemMap.IRQ_VECTOR);

        return vector;
    }
}
