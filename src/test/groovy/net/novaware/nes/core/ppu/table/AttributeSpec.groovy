package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.inject.PpuRegModule
import net.novaware.nes.core.ppu.register.ViewPortRegister
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte

class AttributeSpec extends Specification {

    def "should return sub attribute value"() {
        given:
        ViewPortRegister current = PpuRegModule.provideCurrentViewPort()
        current.setCoarseY(y)
        current.setCoarseX(x)

        when:
        def subAttribute = Attribute.asPalette(ubyte(attribute), current)

        then:
        sint(subAttribute) == data

        where:
        attribute     | y          | x         | data | comment
        0b11_10_01_00 | 0b000_0_0  | 0b000_0_0 | 0b00 | "TL"
        0b11_10_01_00 | 0b000_0_0  | 0b000_1_0 | 0b01 | "TR"
        0b11_10_01_00 | 0b000_1_0  | 0b000_0_0 | 0b10 | "BL"
        0b11_10_01_00 | 0b000_1_0  | 0b000_1_0 | 0b11 | "BR"
    }

    def "should calculate subattribute mask"() {
        expect:
        Attribute.subMask(y, x) == mask

        where:
        y | x | mask
        //      0bBR_BL_TR_TL
        0 | 0 | 0b00_00_00_11
        0 | 1 | 0b00_00_11_00
        1 | 0 | 0b00_11_00_00
        1 | 1 | 0b11_00_00_00
    }
}
