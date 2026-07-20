package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.memory.PpuBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class AttributePrinterSpec extends Specification {

    def segment = PpuMemModule.provideAttributeTable0Segment()

    def vram = new PhysicalMemory("VRAM",
            LAYOUT_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
            LAYOUT_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)

    def bus = newPpuBus()

    def attrs = new AttributeTable("attrs", segment, bus)

    def stringWriter = new StringWriter()
    def printWriter = new PrintWriter(stringWriter)

    def newPpuBus() {
        def b = new PpuBus()
        b.attachCartridge(vram)
        b
    }

    def "should construct an instance"() {
        given:
        AttributeTable table = Mock()
        PrintWriter writer = Mock()

        when:
        def instance = new AttributePrinter(table, writer)
        def string = instance.toString()

        then:
        1 * table.toString() >> "attrs (23C0:23FF)"
        string == "Printer.attrs (23C0:23FF)"
    }

    def "should print attribute table bytes"() {
        given:
        bus.access(ushort(0x23C0)).write().data(ubyte(0xAA))
        bus.access(ushort(0x23C7)).write().data(ubyte(0xBB))
        bus.access(ushort(0x23F8)).write().data(ubyte(0xCC))
        bus.access(ushort(0x23FF)).write().data(ubyte(0xDD))

        def printer = new AttributePrinter(attrs, printWriter)

        when:
        printer.printAllBytes()
        def art = stringWriter.toString()

        then:
        art.trim() == """
            |AA 00 00 00 00 00 00 BB 
            |00 00 00 00 00 00 00 00 
            |00 00 00 00 00 00 00 00 
            |00 00 00 00 00 00 00 00 
            |00 00 00 00 00 00 00 00 
            |00 00 00 00 00 00 00 00 
            |00 00 00 00 00 00 00 00 
            |CC 00 00 00 00 00 00 DD
        """.stripMargin().trim()
    }

    def "should print attribute"() {
        given:

        //                        0bBR_BL_TR_TL
        bus.access(ushort(0x23C0)).write().data(ubyte(0b10_01_00_11))

        def printer = new AttributePrinter(attrs, printWriter)

        when:
        printer.print(0, 0)
        String art = stringWriter.toString()
        // println art

        then:
        art.trim() == """
            |██  
            |░░▓▓
        """.stripMargin().trim()
    }

    def "should print attribute table"() {
        given:
        //                        0bBR_BL_TR_TL
        bus.access(ushort(0x23C0)).write().data(ubyte(0b11_10_01_00))
        bus.access(ushort(0x23C7)).write().data(ubyte(0b00_11_10_01))
        bus.access(ushort(0x23F8)).write().data(ubyte(0b01_00_11_10))
        bus.access(ushort(0x23FF)).write().data(ubyte(0b10_01_00_11))

        def printer = new AttributePrinter(attrs, printWriter)

        when:
        printer.printAll()
        String art = stringWriter.toString()
        // println art

        then:
        art.trim() == """
            |  ░░                        ░░▓▓
            |▓▓██                        ██  
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |                                
            |▓▓██                        ██  
            |  ░░                        ░░▓▓
        """.stripMargin().trim()
    }
}
