package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.register.InstructionRegister;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PA;

/**
 * Current implementation is almost compatible with nestest.nes golden nestest.log
 */
public class DiagnosticUnit implements Unit, Runnable {

    private static final boolean logging = !true; // TODO: parametrize, but keep as compile time constant somehow

    @Used
    private final ShortRegister prefetchAddress;

    @Used private final ByteRegister currentInstruction;
    private final ShortRegister currentOperand;
    private final InstructionRegister decodedInstruction;
    private final DelegatingRegister decodedOperand;
    private final CpuRegFile registers;
    private final CycleCounter cycleCounter;
    private final MemoryBus cpuBus;

    @Inject
    public DiagnosticUnit(
        @CpuVar(PA) ShortRegister prefetchAddress,
        @CpuVar(CI) ByteRegister currentInstruction,
        @CpuVar(CO) ShortRegister currentOperand,

        @CpuVar(DI) InstructionRegister decodedInstruction,
        @CpuVar(DO) DelegatingRegister decodedOperand,

        CpuRegFile registers,

        @CpuVar(CC) CycleCounter cycleCounter,
        @CpuVar(BUS)MemoryBus cpuBus
    ) {
        this.prefetchAddress = prefetchAddress;
        this.currentInstruction = currentInstruction;
        this.currentOperand = currentOperand;
        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.cpuBus = cpuBus;
    }

    public void run() {
        if (logging) { run0(); }
    }

    private void run0() {
        StringBuilder log = new StringBuilder();

        log.append(Hex.s(prefetchAddress.get()));
        log.append("  ");

        log.append(Hex.s(currentInstruction.get())).append(" ");

        InstructionGroup instruction = decodedInstruction.getGroup();
        AddressingMode addressing = decodedInstruction.getAddressing();

        // decodedOperand configured for memory breaks cycle accuracy
        @Unsigned byte operandData = cpuBus.peek(currentOperand.get());

        String absolutePreview = instruction != InstructionGroup.JUMP_TO_LOCATION
                && instruction != InstructionGroup.JUMP_TO_SUBROUTINE
                ? " = " + Hex.s(operandData)
                : "     ";

        switch(addressing.size()) {
            case 0 -> log.append("  ")                       .append(" ").append("  ");
            case 1 -> log.append(Hex.s(currentOperand.low())).append(" ").append("  ");
            case 2 -> log.append(Hex.s(currentOperand.low())).append(" ").append(Hex.s(currentOperand.high()));
        }

        log.append("  ");

        log.append(instruction.mnemonic()).append(" ");

        switch (addressing) {
            case IMMEDIATE ->
                    log.append(addressing.format().replace("BYTE", "$" + Hex.s(currentOperand.low())))
                            .append("                        ");

            case RELATIVE ->
                    log.append(addressing.format().replace("SBYTE", "$" + Hex.s(decodedOperand.getAddress())))
                            .append("                       ");

            case ZERO_PAGE ->
                    log.append(addressing.format().replace("BYTE", "$" + Hex.s(currentOperand.low())))
                            .append(" = ").append(Hex.s(operandData))
                            .append("                    ");

            case ABSOLUTE ->
                    log.append(addressing.format().replace("WORD", "$" + Hex.s(currentOperand.get())))
                            .append(absolutePreview)
                            .append("                  ");

            case ZERO_PAGE_X_INDIRECT ->
                    log.append(addressing.format().replace("BYTE", "$" + Hex.s(currentOperand.low())))
                            .append(" @ NN = NNNN = ").append("NN")
                            .append("    ");

            case ZERO_PAGE_INDIRECT_Y_R, ZERO_PAGE_INDIRECT_Y_W ->
                    log.append(addressing.format().replace("BYTE", "$" + Hex.s(currentOperand.low())))
                            .append(" = NNNN @ NNNN = ").append("NN")
                            .append("  ");

            case ZERO_PAGE_X, ZERO_PAGE_Y ->
                    log.append(addressing.format().replace("BYTE", "$" + Hex.s(currentOperand.low())))
                            .append(" @ NN = ").append("NN")
                            .append("             ");

            case ABSOLUTE_INDIRECT ->
                    log.append(addressing.format().replace("WORD", "$" + Hex.s(currentOperand.get())))
                            .append(" = ").append(Hex.s(decodedOperand.getAddress()))
                            .append("              ");

            case ABSOLUTE_X_R, ABSOLUTE_X_W, ABSOLUTE_Y_R, ABSOLUTE_Y_W ->
                    log.append(addressing.format().replace("WORD", "$" + Hex.s(currentOperand.get())))
                            .append(" @ NNNN = NN")
                            .append("         ");

            case IMPLIED     -> log.append("                            ");
            case ACCUMULATOR -> log.append("A                           ");
            default          -> log.append("                            "); // 28 spaces
        }

        log.append("A:").append(Hex.s(registers.a().get())).append(" ");
        log.append("X:").append(Hex.s(registers.x().get())).append(" ");
        log.append("Y:").append(Hex.s(registers.y().get())).append(" ");
        log.append("P:").append(Hex.s(registers.status().get().get())).append(" ");
        log.append("SP:").append(Hex.s(registers.sp().get())).append(" ");

        log.append("PPU:").append("   ,   ").append(" ");

        // we are counting the cycles as they happen, so at the end of instruction decode some were already added
        log.append("CYC:").append(cycleCounter.getValue() - cycleCounter.getSubValue());

        System.out.println(log);
    }
}
