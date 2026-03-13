package net.novaware.nes.core.memory

import net.novaware.nes.core.register.ByteRegister
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ByteRegisterMemorySpec extends Specification {

    def reg0 = new ByteRegister("0")
    def reg1 = new ByteRegister("1")

    def "should report start and end correctly"() {
        given:
        def registers = [reg0, reg1] as ByteRegister[]
        def memory = new ByteRegisterMemory("test", ushort(0x4000), ushort(0x4001), registers)

        expect:
        memory.getStartAddress() == ushort(0x4000)
        memory.getEndAddress() == ushort(0x4001)
    }

    def "should read and write to correct register"() {
        given:
        def registers = [reg0, reg1] as ByteRegister[]
        def memory = new ByteRegisterMemory("test", ushort(0x4000), ushort(0x4001), registers)

        when:
        memory.specifyThen(ushort(0x4000)).writeByte(ubyte(0x12))
        reg1.setAsByte(0x34)

        then:
        reg0.getAsInt() == 0x12

        when:
        int reg1Val = memory.specifyThen(ushort(0x4001)).readByte()

        then:
        reg1Val == 0x34
    }

    // TODO: test mirroring if address is outside of array size but within addressable range
}
