package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.register.CpuRegFile
import net.novaware.nes.core.cpu.register.CpuInsFile

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ControlFlowSpec extends ControlUnitBaseSpec {

    CpuRegFile regs
    CpuInsFile extRegs
    StackEngine stackEngine
    ControlFlow flow

    def setup() {
        regs = registers
        extRegs = factory.newExtRegisters()
        stackEngine = factory.newStackEngine()
        flow = factory.newControlFlow()

        regs.sp().setAsByte(0xFD)
    }

    def "should call a subroutine and return from it"() {
        given:
        flow.prefetchAddress.setAsShort(0x1231)
        regs.pc().setAsShort(0x1234)
        extRegs.dor().configureMemory(bus, ushort(0x1278))

        when:
        flow.call()

        then:
        regs.pc().getAsInt() == 0x1278

        bus.specifyThen(ushort(0x01FC)).readByte() == ubyte(0x33) // ^
        bus.specifyThen(ushort(0x01FD)).readByte() == ubyte(0x12) // |

        and: "return"
        flow.returnFromCall()

        then:
        regs.pc().getAsInt() == 0x1234
        stackEngine.address() == ushort(0x01FD)
    }
}
