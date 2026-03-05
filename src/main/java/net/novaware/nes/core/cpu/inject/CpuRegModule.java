package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.A;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MD;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SP;
import static net.novaware.nes.core.cpu.inject.CpuVarName.X;
import static net.novaware.nes.core.cpu.inject.CpuVarName.Y;

@Module
public interface CpuRegModule {

    // FIXME: use custom qualifier annotation for cpu, ppu, apu with enum value instead of string

    // TODO: falling edge triggered, not high / low state
    DataRegister nmi = new ByteRegister("NMI");
    DataRegister irq = new ByteRegister("IRQ");
    DataRegister reset = new ByteRegister("RST");
    // TODO: SOH interrupt for sprite 0 hit wake up call

    // TODO: add separate irq/nmi latch registers?

    @Provides
    @BoardScope
    @CpuVar(CC)
    static CycleCounter provideCpuCycleCounter() {
        return new CycleCounter("CPUCC");
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
        return new ShortRegister("MAR");
    }

    @Provides
    @BoardScope
    @CpuVar(MD)
    static ByteRegister provideMemoryData() {
        return new ByteRegister("MDR");
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

    /** @see InstructionGroup#ordinal() TODO: use aaa000cc bits */
    @Provides
    @BoardScope
    @CpuVar(DI)
    static ByteRegister provideDecodedInstruction() {
        return new ByteRegister("DIR"); // TODO: consider holding enum value or handler to improve perf
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
    @CpuVar(SP)
    static ByteRegister provideStackPointer() {
        return new ByteRegister("S");
    }
}
