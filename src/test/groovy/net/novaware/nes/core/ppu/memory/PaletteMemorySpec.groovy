package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.BACKGROUND
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.FOREGROUND
import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
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
        def palette = PpuMemModule.paletteMemory()

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
    }
}
