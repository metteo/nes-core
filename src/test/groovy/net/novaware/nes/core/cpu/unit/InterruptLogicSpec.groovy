package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.register.CpuRegFile
import net.novaware.nes.core.cpu.register.Status
import net.novaware.nes.core.util.Hex

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class InterruptLogicSpec extends ControlUnitBaseSpec {

    CpuRegFile regs
    def interrupts = factory.newInterruptLogic()

    def setup() {
        regs = registers
    }

    def "should force break (eg software irq)"() {
        given:
        regs.pc().setAsShort(0x1234)
        regs.sp().setAsByte(0xFD)
        regs.status().initialize() // I=1

        Status status = regs.status().get()

        when:
        interrupts.forceBreak()

        then:
        regs.pc().getAsInt() == 0xFFFE
        regs.sp().getAsInt() == 0xFD - 3

        //                                                      0bNV1B_DIZC
        bus.specifyThen(ushort(0x01FB)).readByte() == ubyte(0b0011_0100) // ^
        bus.specifyThen(ushort(0x01FC)).readByte() == ubyte(0x36)        // |
        bus.specifyThen(ushort(0x01FD)).readByte() == ubyte(0x12)        // |

        status.isIrqDisabled()
        status.getBreak()

        regs.status().isIrqDisabled()

        and: "return from it"
        interrupts.returnFromInterrupt()

        then:
        Hex.s(regs.pc().get()) == "1236"
        regs.sp().getAsInt() == 0xFD
        status.isIrqDisabled()
        status.getBreak()

        regs.status().isIrqDisabled() // immediate
    }
}
