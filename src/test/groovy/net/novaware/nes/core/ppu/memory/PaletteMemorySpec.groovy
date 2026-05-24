package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.BACKGROUND
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.FOREGROUND
import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.ProbeUtil.probeDevice
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PaletteMemorySpec extends Specification {

    def "should decode correct color parameters"() {
        expect:
        PaletteMemory.getColorPosition(section, palette, offset) == position

        where:
        section    | palette | offset || position
        BACKGROUND | 0       | 0      || 0x0
        BACKGROUND | 1       | 0      || 0x4
        BACKGROUND | 2       | 0      || 0x8
        BACKGROUND | 3       | 0      || 0xC

        FOREGROUND | 0       | 0      || 0x0
        FOREGROUND | 1       | 0      || 0x4
        FOREGROUND | 2       | 0      || 0x8
        FOREGROUND | 3       | 0      || 0xC

        BACKGROUND | 0       | 1      || 0x01
        BACKGROUND | 1       | 2      || 0x06
        BACKGROUND | 3       | 3      || 0x0F

        FOREGROUND | 0       | 1      || 0x11
        FOREGROUND | 2       | 2      || 0x1A
        FOREGROUND | 3       | 3      || 0x1F
    }

    def "should decode correct color address"() {
        given:
        def palette = PpuMemModule.providePaletteMemory()

        expect:
        palette.getColorPosition(ushort(address)) == position

        where:
        address || position
        // background 0s
        0x3F00  || 0x0
        0x3F04  || 0x4
        0x3F08  || 0x8
        0x3F0C  || 0xC
        // foregrounds 0s (mirror)
        0x3F10  || 0x0
        0x3F14  || 0x4
        0x3F18  || 0x8
        0x3F1C  || 0xC
        // sample bg, fg (no mirror)
        0x3F02  || 0x02
        0x3F1E  || 0x1E
        // palette 4 (end of address mirror)
        0x3FFC  || 0x0C
        0x3FFD  || 0x1D
        0x3FFE  || 0x1E
        0x3FFF  || 0x1F
    }

    def "should construct palette memory"() {
        given:
        def palette = new PaletteMemory(
                "PALETTE",
                PALETTE_RAM_START,
                PALETTE_RAM_MIRROR_END,
                PALETTE_RAM_SIZE
        )
        def paletteBus = new TestBus(palette)

        when:
        palette.setColor(FOREGROUND, 3, 0, ubyte(0x08))
        paletteBus.access(ushort(0x3F02)).write().data(ubyte(0x10))

        then:
        palette.getName() == "PALETTE"
        palette.getStartAddress() == PALETTE_RAM_START
        palette.getEndAddress() == PALETTE_RAM_MIRROR_END

        paletteBus.access(ushort(0x3F1C)).read().data() == ubyte(0x8)
        palette.getColor(BACKGROUND, 0, 2) == ubyte(0x10)

        probeDevice(palette, 0x3F1C) == 0x8
        probeDevice(palette, 0x3F02) == 0x10
    }

    def "should print all palette colors"() {
        given:
        def palette = new PaletteMemory(
                "PALETTE",
                PALETTE_RAM_START,
                PALETTE_RAM_MIRROR_END,
                PALETTE_RAM_SIZE
        )

        def paletteBus = new TestBus(palette)

        def colors = [
//      Pal 0                         Pal 1                         Pal 2                         Pal 3
/* B */ 0x2F, 0x2E, 0x2D, 0x2C, /* */ 0x2B, 0x2A, 0x29, 0x28, /* */ 0x27, 0x26, 0x25, 0x24, /* */ 0x23, 0x22, 0x21, 0x20,
/* F */ 0x3F, 0x3E, 0x3D, 0x3C, /* */ 0x3B, 0x3A, 0x39, 0x38, /* */ 0x37, 0x36, 0x35, 0x34, /* */ 0x33, 0x32, 0x31, 0x30,
        ]

        def start = sint(PALETTE_RAM_START)
        for(int i = 0; i < colors.size(); i++) {
            paletteBus.write(start + i, colors[i])
        }

        when:
        def colorArt = palette.printColors()

        then:
        colorArt.trim() == """
            BACKGROUND\t3F 2E 2D 2C \t3B 2A 29 28 \t37 26 25 24 \t33 22 21 20 \t
            FOREGROUND\t3F 3E 3D 3C \t3B 3A 39 38 \t37 36 35 34 \t33 32 31 30 \t
        """.stripIndent(12).trim()

        //println colorArt
    }
}
