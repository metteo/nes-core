package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ushort

class AttributeTableSpec extends Specification {

    def segment = PpuMemModule.provideAttributeTable0Segment()

    def vram = new PhysicalMemory("VRAM",
            LAYOUT_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
            LAYOUT_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)

    def bus = new TestBus(vram)

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new AttributeTable("attrs", segment, bus)

        then:
        instance.getName() == "attrs"
        instance.toString() == "attrs (23C0:23FF)"
    }

    def "should return attribute value"() {
        bus.write(ushort(0x23C0), 0xAA)
        bus.write(ushort(0x23C7), 0xBB)
        bus.write(ushort(0x23F8), 0xCC)
        bus.write(ushort(0x23FF), 0xDD)

        def attrs = new AttributeTable("attrs", segment, bus)

        when:
        def attribute = attrs.getAttribute(row, col)

        then:
        sint(attribute) == data

        where:
        row | col || data
        0   | 0   || 0xAA
        0   | 7   || 0xBB
        7   | 0   || 0xCC
        7   | 7   || 0xDD
    }
}
