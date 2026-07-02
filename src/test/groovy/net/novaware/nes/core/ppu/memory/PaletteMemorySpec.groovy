package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte

class PaletteMemorySpec extends Specification {

    def "should decode correct color address"() {
        given:
        def palette = PpuMemModule.providePaletteMemory()

        expect:
        palette.toPosition(ubyte(address)) == position

        where:
        address || position
        // background 0s
        0x00    || 0x0
        0x04    || 0x4
        0x08    || 0x8
        0x0C    || 0xC
        // foregrounds 0s (mirror)
        0x10    || 0x0
        0x14    || 0x4
        0x18    || 0x8
        0x1C    || 0xC
        // sample bg, fg (no mirror)
        0x02    || 0x02
        0x1E    || 0x1E
        // palette 4 (end of address mirror)
        0xFC    || 0x0C
        0xFD    || 0x1D
        0xFE    || 0x1E
        0xFF    || 0x1F
    }

    def "should construct palette memory"() {
        given:
        def palette = new PaletteMemory("PALETTE")

        when:
        palette.write(ubyte(0x1C), ubyte(0x08))
        palette.write(ubyte(0x02), ubyte(0x10))

        then:
        palette.getName() == "PALETTE"

        palette.read(ubyte(0x1C)) == ubyte(0x8)
        palette.read(ubyte(0x22)) == ubyte(0x10)
    }
}
