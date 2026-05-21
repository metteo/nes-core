package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.pin.internal.LatchingPin;
import net.novaware.nes.core.pin.internal.ReactivePin;
import net.novaware.nes.core.register.BooleanRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RDY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.S0H;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SOV;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;

@Module
public interface CpuPinModule {

    @Provides
    @BoardScope
    @CpuVar(RES)
    static Pin provideResPin(@CpuVar(RES) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(RES.doc(), Signal.LOW);
        return new ReactivePin(RES.doc(), detector, register::set);
    }

    @Provides
    @BoardScope
    @CpuVar(IRQ)
    static Pin provideIrqPin(@CpuVar(IRQ) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(IRQ.name(), LOW);
        return new ReactivePin(NMI.name(), detector, register::set);
    }

    @Provides
    @BoardScope
    @CpuVar(NMI)
    static Pin provideNmiPin(@CpuVar(NMI) BooleanRegister register) {
        EdgeDetector detector = new EdgeDetector(NMI.name(), LOW);
        return new LatchingPin(NMI.name(), detector, register::set);
    }

    @Provides
    @BoardScope
    @CpuVar(S0H)
    static Pin provideS0hPin(@CpuVar(S0H) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(S0H.name(), LOW);
        return new LatchingPin(S0H.doc(), detector, register::set);
    }

    @Provides
    @BoardScope
    @CpuVar(RDY)
    static Pin provideRdyPin(@CpuVar(RDY) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(RDY.name(), LOW);
        return new ReactivePin(RDY.name(), detector, register::set);
    }

    @Provides
    @BoardScope
    @CpuVar(SOV)
    static Pin provideSovPin(@CpuVar(PS) StatusRegister status) {
        EdgeDetector detector = new EdgeDetector(SOV.name(), LOW);
        return new LatchingPin(SOV.name(), detector, status::setOverflow);
    }
}
