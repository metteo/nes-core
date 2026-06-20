package net.novaware.nes.core.io.memory

import net.novaware.nes.core.register.BooleanRegister
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START
import static net.novaware.nes.core.util.UTypes.sint

class JoyStrobeDeviceSpec extends Specification {

    def strobeRegister = new BooleanRegister("JOY1.STROBE")

    def newJoyStrobeDevice() {
        new JoyStrobeDevice("JOY1.STROBE", IO_REGISTERS_START, strobeRegister)
    }

    def "should construct instance"() {
        when:
        def strobe = newJoyStrobeDevice()

        then:
        strobe.getName() == "JOY1.STROBE"
        strobe.getStartAddress() == IO_REGISTERS_START
        strobe.getEndAddress() == IO_REGISTERS_START
        strobe.toString() == "JOY1.STROBE (0x4016): 0"
    }

    def "should write to strobe register"() {
        given:
        def strobe = newJoyStrobeDevice()

        def strobeBus = new TestBus(null, strobe)

        when:
        strobeBus.write(sint(IO_REGISTERS_START), 0b1)

        then:
        strobeRegister.get()
    }
}
