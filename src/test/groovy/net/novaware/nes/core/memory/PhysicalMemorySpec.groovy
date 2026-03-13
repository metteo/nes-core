package net.novaware.nes.core.memory


import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PhysicalMemorySpec extends Specification {

    def "should create basic instance"() {
        when:
        def memory = new PhysicalMemory("RAM", RAM_START, RAM_MIRROR_END, RAM_SIZE)

        then:
        memory.getName() == "RAM"
        memory.getStartAddress() == RAM_START
        memory.getEndAddress() == RAM_MIRROR_END
    }

    def "should mirror addresses outside of specified size" () {
        given:
        def memory = new PhysicalMemory("RAM", RAM_START, RAM_MIRROR_END, RAM_SIZE)

        when:
        memory.specifyThen(ushort(address)).writeByte(ubyte(data))

        then:
        memory.specifyThen(ushort(address + 0 * 0x800)).readByte() == ubyte(data)
        memory.specifyThen(ushort(address + 1 * 0x800)).readByte() == ubyte(data)
        memory.specifyThen(ushort(address + 2 * 0x800)).readByte() == ubyte(data)
        memory.specifyThen(ushort(address + 3 * 0x800)).readByte() == ubyte(data)

        where:
        address | data
        0x0000  | 0x01
        0x0400  | 0xAB
        0x07FF  | 0x02
    }

    def "should offset addresses if not starting at 0x0000"() {
        given:
        def memory = new PhysicalMemory("?", ushort(0x1000), ushort(0x1FFF), 0x1000)

        when:
        memory.specifyThen(ushort(address)).writeByte(ubyte(data))

        then:
        memory.specifyThen(ushort(address)).readByte() == ubyte(data)

        where:
        address | data
        0x1000  | 0x01
        0x1400  | 0xAB
        0x1FFF  | 0x02
    }
}
