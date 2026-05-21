package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.register.InstructionRegister;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.BooleanLatch;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.A;
import static net.novaware.nes.core.cpu.inject.CpuVarName.BRK;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ID;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MD;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RDY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.S0H;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SOV;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SP;
import static net.novaware.nes.core.cpu.inject.CpuVarName.X;
import static net.novaware.nes.core.cpu.inject.CpuVarName.Y;

@Module
public interface CpuRegModule {

    @Provides
    @BoardScope
    @CpuVar(CC)
    static IntegerCounter provideCpuCycleCounter() {
        return new IntegerCounter(CC.doc());
    }

    @Provides
    @BoardScope
    @CpuVar(IC)
    static IntegerCounter provideInstructionCycle() {
        return new IntegerCounter(IC.doc());
    }

    @Provides
    @BoardScope
    @CpuVar(PC)
    static ShortRegister provideProgramCounter() {
        return new ShortRegister("PC");
    }

    @Provides
    @BoardScope
    @CpuVar(MA)
    static ShortRegister provideMemoryAddress() {
        return new ShortRegister("MAR"); // TODO: use CpuVarName enum.name() instead for all regs
    }

    @Provides
    @BoardScope
    @CpuVar(MD)
    static ByteRegister provideMemoryData() {
        return new ByteRegister("MDR");
    }

    @Provides
    @BoardScope
    @CpuVar(PA)
    static ShortRegister providePrefetchAddress() {
        return new ShortRegister(PA.name());
    }

    /** @see Instruction#opcode() */
    @Provides
    @BoardScope
    @CpuVar(CI)
    static ByteRegister provideCurrentInstruction() {
        return new ByteRegister("CIR");
    }

    @Provides
    @BoardScope
    @CpuVar(CO)
    static ShortRegister provideCurrentOperand() {
        return new ShortRegister("COR");
    }

    @Provides
    @BoardScope
    @CpuVar(DI)
    static InstructionRegister provideDecodedInstruction() {
        return new InstructionRegister("DIR");
    }

    @Provides
    @BoardScope
    @CpuVar(DO)
    static DelegatingRegister provideDecodedOperand() {
        return new DelegatingRegister("DOR");
    }

    @Provides
    @BoardScope
    @CpuVar(A)
    static ByteRegister provideAccumulator() {
        return new ByteRegister("A");
    }

    @Provides
    @BoardScope
    @CpuVar(X)
    static ByteRegister provideIndexX() {
        return new ByteRegister("X");
    }

    @Provides
    @BoardScope
    @CpuVar(Y)
    static ByteRegister provideIndexY() {
        return new ByteRegister("Y");
    }

    @Provides
    @BoardScope
    @CpuVar(PS)
    static StatusRegister provideStatus() {
        return new StatusRegister();
    }

    @Provides
    @BoardScope
    @CpuVar(ID)
    static BooleanLatch provideInterruptDisabled(@CpuVar(PS) StatusRegister status) {
        return new BooleanLatch(ID.name(), status::setIrqDisabled);
    }

    @Provides
    @BoardScope
    @CpuVar(SP)
    static ByteRegister provideStackPointer() {
        return new ByteRegister("S");
    }

    // region Pin Registers

    @Provides
    @BoardScope
    @CpuVar(BRK)
    static BooleanRegister provideBrkRegister() {
        return new BooleanRegister(BRK.name());
    }

    @Provides
    @BoardScope
    @CpuVar(RES)
    static BooleanRegister provideResRegister() {
        return new BooleanRegister(RES.name());
    }

    @Provides
    @BoardScope
    @CpuVar(NMI)
    static BooleanRegister provideNmiRegister() {
        return new BooleanRegister(NMI.name());
    }

    @Provides
    @BoardScope
    @CpuVar(IRQ)
    static BooleanRegister provideIrqRegister() {
        return new BooleanRegister(IRQ.name());
    }

    @Provides
    @BoardScope
    @CpuVar(S0H)
    static BooleanRegister provideS0hRegister() {
        return new BooleanRegister(S0H.name());
    }

    @Provides
    @BoardScope
    @CpuVar(RDY)
    static BooleanRegister provideRdyRegister() {
        return new BooleanRegister(RDY.name());
    }

    @Provides
    @BoardScope
    @CpuVar(SOV)
    static BooleanRegister provideSovRegister() {
        return new BooleanRegister(SOV.name());
    }

    // endregion
}
