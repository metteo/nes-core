package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.cpu.register.Status
import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.RecordingBus
import spock.lang.Specification

class InterruptLogicSpec extends Specification {

    CpuRegisters regs = new CpuRegisters()
    MemoryBus bus = new RecordingBus()
    MemoryMgmt mmu = new MemoryMgmt(regs, bus)
    StackEngine stackEngine = new StackEngine(regs, mmu)
    InterruptLogic interrupts = new InterruptLogic(regs, stackEngine)

    def "should force break (eg software irq"() {
        given:
        regs.pc().setAsShort(0x1234)
        regs.sp().setAsByte(0xFD)
        regs.status().initialize()

        Status status = regs.status().get()

        when:
        interrupts.forceBreak()

        then:
        regs.pc().getAsInt() == 0xFFFE
        regs.sp().getAsInt() == 0xFD - 3

        status.isIrqDisabled()
        status.getBreak()

        regs.status().isIrqDisabled()
    }
}
