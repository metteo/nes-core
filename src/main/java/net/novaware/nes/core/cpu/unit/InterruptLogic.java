package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class InterruptLogic implements Unit {

    public static final @Unsigned short NMI_VECTOR = ushort(0xFFFA);
    public static final @Unsigned short RES_VECTOR = ushort(0xFFFC);
    public static final @Unsigned short IRQ_VECTOR = ushort(0xFFFE);

    @Used
    private final CpuRegisters registers;

    @Used
    private final StackEngine stackEngine;

    @Inject
    public InterruptLogic(
        CpuRegisters registers,
        StackEngine stackEngine
    ) {
        this.registers = registers;
        this.stackEngine = stackEngine;
    }

    public void forceBreak() {
        int pcVal = registers.pc().getAsInt();
        int retVal = pcVal + 2;

        stackEngine.push(ushort(retVal));
        stackEngine.pushStatus(); // brk flag = 1

        registers.pc().set(IRQ_VECTOR);
    }

    @Override
    public void reset() {

    }

    public void interruptRequest() {

    }

    public void nonMaskableInterrupt() {

    }

    public void returnFromInterrupt() {
    }
}
