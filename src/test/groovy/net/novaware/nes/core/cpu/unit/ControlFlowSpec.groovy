package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.memory.RecordingBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ControlFlowSpec extends Specification {

    CpuRegisters regs = new CpuRegisters()
    RecordingBus bus = new RecordingBus()
    MemoryMgmt mmu = new MemoryMgmt(regs, bus)
    StackEngine stackEngine = new StackEngine(regs, mmu)
    ControlFlow flow = new ControlFlow(regs, bus.cycleCounter(), stackEngine)

    def "setup"() {
        regs.sp().setAsByte(0xFD)
    }

    def "should call a subroutine and return from it"() {
        given:
        regs.pc().setAsShort(0x1234)
        regs.dor().configureMemory(bus, ushort(0x1278))

        when:
        flow.call()

        then:
        regs.pc().getAsInt() == 0x1278

        bus.specifyThen(ushort(0x01FC)).readByte() == ubyte(0x34) // ^
        bus.specifyThen(ushort(0x01FD)).readByte() == ubyte(0x12) // |

        and: "return"
        flow.returnFromCall()

        then:
        regs.pc().getAsInt() == 0x1234 + 1
        regs.sp().addressAsInt() == 0x01FD
    }
}
