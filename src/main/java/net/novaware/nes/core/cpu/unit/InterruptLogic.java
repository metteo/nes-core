package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class InterruptLogic implements Unit {

    public static final @Unsigned short NMI_VECTOR = ushort(0xFFFA);
    public static final @Unsigned short RES_VECTOR = ushort(0xFFFC);
    public static final @Unsigned short IRQ_VECTOR = ushort(0xFFFE);

    @Used
    private final CpuRegFile registers;

    @Used
    private final StackEngine stackEngine;

    @Used
    private final LevelDetector irqDetector;

    @Used
    private final EdgeDetector nmiDetector;

    @Used
    private final AddressGen agu;

    @Inject
    public InterruptLogic(
        CpuRegFile registers,
        StackEngine stackEngine,
        @CpuVar(IRQ) LevelDetector irqDetector,
        @CpuVar(NMI) EdgeDetector nmiDetector,
        AddressGen agu
    ) {
        this.registers = registers;
        this.stackEngine = stackEngine;
        this.irqDetector = irqDetector;
        this.nmiDetector = nmiDetector;
        this.agu = agu;
    }

    public void forceBreak() { // TODO: test how NMI can override the BRK
        int pcVal = registers.pc().getAsInt();
        int retVal = pcVal + 1; // +2 in total: +1 when fetching opcode and +1 here

        stackEngine.push(ushort(retVal));
        stackEngine.pushStatus(true);

        registers.status().setIrqDisabled(true);

        @Unsigned short address = agu.fetchAddress(IRQ_VECTOR);
        registers.pc().set(address);
    }

    public void interruptRequest() {
        int pcVal = registers.pc().getAsInt();

        stackEngine.push(ushort(pcVal));
        stackEngine.pushStatus(false);

        registers.status().setIrqDisabled(true);

        @Unsigned short address = agu.fetchAddress(IRQ_VECTOR);
        registers.pc().set(address);
    }

    public void nonMaskableInterrupt() { // TODO: same code as irq and brk?
        int pcVal = registers.pc().getAsInt();

        stackEngine.push(ushort(pcVal));
        stackEngine.pushStatus(false);

        registers.status().setIrqDisabled(true);

        @Unsigned short address = agu.fetchAddress(NMI_VECTOR);
        registers.pc().set(address);
    }

    public void returnFromInterrupt() {
        stackEngine.pullStatus(); // TODO: what about disable interrupt flag?

        stackEngine.pull(registers.pc());
    }

    /**
     * @return true if interrupt detected
     */
    public void sample() {
        if (nmiDetector.isActive()) {
            nonMaskableInterrupt();
            return;
        }

        if (irqDetector.isActive() && !registers.status().isIrqDisabled()) { // TODO: verify that irqDisable delay is honored
            interruptRequest();
        }
    }
}
