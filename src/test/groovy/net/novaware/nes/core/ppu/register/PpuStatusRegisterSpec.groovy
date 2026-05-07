package net.novaware.nes.core.ppu.register

import spock.lang.Specification

class PpuStatusRegisterSpec extends Specification {

    def "should hold ppu flags"() {
        given:
        def register = new PpuStatusRegister()

        register.setVerticalBlank(verticalBlank)
        register.setSpriteZeroHit(spriteZeroHit)
        register.setSpriteOverflow(spriteOverflow)

        expect:
        register.isVerticalBlank() == verticalBlank
        register.isSpriteZeroHit() == spriteZeroHit
        register.isSpriteOverflow() == spriteOverflow
        register.toString() == "PPU.STATUS: " + toString

        where:
        verticalBlank | spriteZeroHit | spriteOverflow | toString
        true          | false         | false          | "V_______"
        false         | true          | false          | "_S______"
        false         | false         | true           | "__O_____"
        true          | true          | true           | "VSO_____"
    }
}
