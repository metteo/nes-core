package net.novaware.nes.core.io.memory

import net.novaware.nes.core.register.ByteRegister
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte

class JoyDataDeviceSpec extends Specification {

    def pri = new ByteRegister("JOY1.PRI")
    def exp = new ByteRegister("JOY1.EXP")
    def mic = new ByteRegister("JOY1.MIC")

    def newJoyDataDevice() {
        new JoyDataDevice("JOY1", IO_REGISTERS_START, pri, exp, mic)
    }

    def "should construct an instance"() {
        when:
        def joy1 = newJoyDataDevice()

        then:
        joy1.getName() == "JOY1"
        joy1.getStartAddress() == IO_REGISTERS_START
        joy1.getEndAddress() == IO_REGISTERS_START
        joy1.toString() == "JOY1 (0x4016): 0b0000_0000"
    }

    def "should read a bit and shift primary register"() {
        given:
        def joy1 = newJoyDataDevice()

        def joy1Bus = new TestBus(joy1, null)

        pri.set(ubyte(input))

        when:
        (numOfReads - 1).times { joy1Bus.read(sint(IO_REGISTERS_START))}

        def joy1Byte = joy1Bus.read(sint(IO_REGISTERS_START))

        then:
        pri.getAsInt() == primary
        joy1Byte == output

        where:
        input       | numOfReads || primary     | output
        0b1000_0000 | 1          || 0b0000_0001 | 0b0000_0001
        0b1000_0000 | 2          || 0b0000_0011 | 0b0000_0000
        0b0100_0000 | 2          || 0b0000_0011 | 0b0000_0001
        0b0000_0001 | 8          || 0b1111_1111 | 0b0000_0001
    }
}
