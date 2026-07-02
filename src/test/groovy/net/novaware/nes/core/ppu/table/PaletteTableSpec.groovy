package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.memory.PaletteMemory
import spock.lang.Specification

import static net.novaware.nes.core.ppu.table.PaletteTable.Layer.BACKGROUND
import static net.novaware.nes.core.ppu.table.PaletteTable.Layer.FOREGROUND
import static net.novaware.nes.core.util.UTypes.ubyte

class PaletteTableSpec extends Specification {

    def "should decode correct color parameters"() {
        expect:
        PaletteTable.getAddress(layer, palette, offset) == ubyte(address)

        where:
        layer      | palette | offset || address
        BACKGROUND | 0       | 0      || 0x0
        BACKGROUND | 1       | 0      || 0x4
        BACKGROUND | 2       | 0      || 0x8
        BACKGROUND | 3       | 0      || 0xC

        FOREGROUND | 0       | 0      || 0x10 // memory is handling shared transparent color
        FOREGROUND | 1       | 0      || 0x14
        FOREGROUND | 2       | 0      || 0x18
        FOREGROUND | 3       | 0      || 0x1C

        BACKGROUND | 0       | 1      || 0x01
        BACKGROUND | 1       | 2      || 0x06
        BACKGROUND | 3       | 3      || 0x0F

        FOREGROUND | 0       | 1      || 0x11
        FOREGROUND | 2       | 2      || 0x1A
        FOREGROUND | 3       | 3      || 0x1F
    }

    def "should print all palette colors"() {
        given:
        def memory = new PaletteMemory("PALETTE")
        def table = new PaletteTable("PAL", memory)

        def stringWriter = new StringWriter()
        def printWriter = new PrintWriter(stringWriter)
        def printer = new PalettePrinter(table, printWriter)

        def colors = [
//      Pal 0                         Pal 1                         Pal 2                         Pal 3
/* B */ 0x2F, 0x2E, 0x2D, 0x2C, /* */ 0x2B, 0x2A, 0x29, 0x28, /* */ 0x27, 0x26, 0x25, 0x24, /* */ 0x23, 0x22, 0x21, 0x20,
/* F */ 0x3F, 0x3E, 0x3D, 0x3C, /* */ 0x3B, 0x3A, 0x39, 0x38, /* */ 0x37, 0x36, 0x35, 0x34, /* */ 0x33, 0x32, 0x31, 0x30,
        ]

        for(int i = 0; i < colors.size(); i++) {
            memory.write(ubyte(i), ubyte(colors[i]))
        }

        when:
        printer.printAll()
        def colorArt = stringWriter.toString()

        then:
        colorArt.trim() == """
            BACKGROUND\t3F 2E 2D 2C \t3B 2A 29 28 \t37 26 25 24 \t33 22 21 20 \t
            FOREGROUND\t3F 3E 3D 3C \t3B 3A 39 38 \t37 36 35 34 \t33 32 31 30 \t
        """.stripIndent(12).trim()

        //println colorArt
    }
}
