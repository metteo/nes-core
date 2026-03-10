package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PA;

public class DiagnosticUnit implements Unit, Runnable {

    private static final boolean logging = !true; // TODO: parametrize, but keep as compile time constant somehow

    @Used
    private final ShortRegister prefetchAddress;

    @Used private final ByteRegister currentInstruction;
    private final ShortRegister currentOperand;
    private final ByteRegister decodedInstruction;
    private final DelegatingRegister decodedOperand;
    private final CpuRegFile registers;
    private final CycleCounter cycleCounter;

    @Inject
    public DiagnosticUnit(
        @CpuVar(PA) ShortRegister prefetchAddress,
        @CpuVar(CI) ByteRegister currentInstruction,
        @CpuVar(CO) ShortRegister currentOperand,

        @CpuVar(DI) ByteRegister decodedInstruction,
        @CpuVar(DO) DelegatingRegister decodedOperand,

        CpuRegFile registers,

        @CpuVar(CC) CycleCounter cycleCounter
    ) {
        this.prefetchAddress = prefetchAddress;
        this.currentInstruction = currentInstruction;
        this.currentOperand = currentOperand;
        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;
        this.registers = registers;
        this.cycleCounter = cycleCounter;
    }

    public void run() {
        if (logging) { run0(); }
    }

    private void run0() {
        StringBuilder log = new StringBuilder();

        log.append(Hex.s(prefetchAddress.get()));
        log.append("  ");

        log.append(Hex.s(currentInstruction.get())).append(" ");

        Instruction instruction = InstructionRegistry.fromOpcode(currentInstruction.get());
        AddressingMode addressing = instruction.addressingMode();

        switch(addressing.size()) {
            case 0 -> log.append("  ").append(" ").append("  ");
            case 1 -> log.append(Hex.s(currentOperand.low())).append(" ").append("  ");
            case 2 -> log.append(Hex.s(currentOperand.low())).append(" ").append(Hex.s(currentOperand.high()));
        }

        log.append("  ");

        log.append(instruction.group().mnemonic()).append(" ");

        log.append("                            ");

        log.append("A:").append(Hex.s(registers.a().get())).append(" ");
        log.append("X:").append(Hex.s(registers.x().get())).append(" ");
        log.append("Y:").append(Hex.s(registers.y().get())).append(" ");
        log.append("P:").append(Hex.s(registers.status().get().get())).append(" ");
        log.append("SP:").append(Hex.s(registers.sp().get())).append(" ");

        log.append("PPU:").append("   ,   ").append(" ");

        // we are counting the cycles as they happen, so at the end of instruction there are already added
        log.append("CYC:").append(cycleCounter.getValue() - cycleCounter.getSubValue());

        System.out.println(log);
    }
}
