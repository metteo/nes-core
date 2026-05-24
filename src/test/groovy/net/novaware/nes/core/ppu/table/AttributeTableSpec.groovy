package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ushort

class AttributeTableSpec extends Specification {

    def segment = PpuMemModule.provideAttributeTable0Segment()

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new AttributeTable("attrs", segment, bus)

        then:
        instance.getName() == "attrs"
        instance.toString() == "attrs (23C0:23FF)"
    }

    def "should print attribute table bytes"() {
        given:
        def vram = new PhysicalMemory("VRAM",
                NAME_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
                NAME_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)

        def bus = new TestBus(vram)

        def attrs = new AttributeTable("attrs", segment, bus)

        bus.write(ushort(0x23C0), 0xAA)
        bus.write(ushort(0x23C7), 0xBB)
        bus.write(ushort(0x23F8), 0xCC)
        bus.write(ushort(0x23FF), 0xDD)

        when:
        String art = attrs.printAttributeBytes()

        then:
        art.trim() == """
            AA 00 00 00 00 00 00 BB 
            00 00 00 00 00 00 00 00 
            00 00 00 00 00 00 00 00 
            00 00 00 00 00 00 00 00 
            00 00 00 00 00 00 00 00 
            00 00 00 00 00 00 00 00 
            00 00 00 00 00 00 00 00 
            CC 00 00 00 00 00 00 DD
        """.stripIndent(12).trim()
    }

    def "should print attribute table"() {
        given:
        def vram = new PhysicalMemory("VRAM",
                NAME_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
                NAME_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)

        def bus = new TestBus(vram)

        def attrs = new AttributeTable("attrs", segment, bus)

        bus.write(ushort(0x23C0), 0b11_10_01_00)
        bus.write(ushort(0x23C7), 0b00_11_10_01)
        bus.write(ushort(0x23F8), 0b01_00_11_10)
        bus.write(ushort(0x23FF), 0b10_01_00_11)

        when:
        String art = attrs.printAttributeBits(false)

        then:
        art.trim() == """
            ░▒░░░░░░░░░░░░▒▓
            ▓█░░░░░░░░░░░░█░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ░░░░░░░░░░░░░░░░
            ▓█░░░░░░░░░░░░█░
            ░▒░░░░░░░░░░░░▒▓
        """.stripIndent(12).trim()

        //println attrs.printAttributeBits(true)
    }
}
