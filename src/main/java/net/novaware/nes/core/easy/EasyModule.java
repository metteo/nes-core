package net.novaware.nes.core.easy;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;

@Module
public interface EasyModule {

    @Provides
    @BoardScope
    @CpuVar(BUS)
    static MemoryBus provideMemoryBus() {
        MemoryBus bus = new EasyBus();
        return bus;
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
