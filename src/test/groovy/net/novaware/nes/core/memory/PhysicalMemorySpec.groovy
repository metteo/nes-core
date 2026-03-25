package net.novaware.nes.core.memory

import net.novaware.nes.core.test.TestBus
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
        def ram = new PhysicalMemory("RAM", RAM_START, RAM_MIRROR_END, RAM_SIZE)
        def memory = new TestBus(ram)

        when:
        memory.access(ushort(address)).write().data(ubyte(data))

        then:
        memory.access(ushort(address + 0 * 0x800)).read().data() == ubyte(data)
        memory.access(ushort(address + 1 * 0x800)).read().data() == ubyte(data)
        memory.access(ushort(address + 2 * 0x800)).read().data() == ubyte(data)
        memory.access(ushort(address + 3 * 0x800)).read().data() == ubyte(data)

        where:
        address | data
        0x0000  | 0x01
        0x0400  | 0xAB
        0x07FF  | 0x02
    }

    def "should offset addresses if not starting at 0x0000"() {
        given:
        def ram = new PhysicalMemory("?", ushort(0x1000), ushort(0x1FFF), 0x1000)
        def memory = new TestBus(ram)

        when:
        memory.access(ushort(address)).write().data(ubyte(data))

        then:
        memory.access(ushort(address)).read().data() == ubyte(data)

        where:
        address | data
        0x1000  | 0x01
        0x1400  | 0xAB
        0x1FFF  | 0x02
    }
}
