package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BRK;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RDY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.S0H;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SOV;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;

@Module
public interface CpuSignalModule {

    @Provides
    @BoardScope
    @CpuVar(BRK)
    static LevelDetector provideBrkDetector() {
        return new LevelDetector(BRK.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(IRQ)
    static LevelDetector provideIrqDetector() {
        return new LevelDetector(IRQ.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(NMI)
    static EdgeDetector provideNmiDetector() {
        return new EdgeDetector(NMI.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(S0H)
    static LevelDetector provideS0hDetector() {
        return new LevelDetector(S0H.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(RES)
    static LevelDetector provideResDetector() {
        return new LevelDetector(RES.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(RDY)
    static LevelDetector provideRdyDetector() {
        return new LevelDetector(RDY.name(), LOW);
    }

    @Provides
    @BoardScope
    @CpuVar(SOV)
    static EdgeDetector provideSetOverflowDetector() {
        return new EdgeDetector(SOV.name(), LOW);
    }
}
