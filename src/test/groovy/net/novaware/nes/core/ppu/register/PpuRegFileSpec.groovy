package net.novaware.nes.core.ppu.register

import spock.lang.Specification
import spock.lang.Subject

import static net.novaware.nes.core.TestNesCore.newTestNesCore
import static net.novaware.nes.core.util.UTypes.*

class PpuRegFileSpec extends Specification {

    @Subject
    PpuRegFile registers = newTestNesCore().newPpuRegisters()

    def "all boolean registers are initialized and reachable"() {
        expect:
        registers.booleanRegisters.size() == 12

        registers.secondWrite.getName() == "PPU.W"
        registers.hBlank.getName() == "PPU.HB"

        registers.vBlankInterruptEnabled.getName() == "PPU.CTRL.V"
        registers.masterSlaveSelect.getName() == "PPU.CTRL.P"
        registers.spriteSize.getName() == "PPU.CTRL.H"

        registers.emphasizeRed.getName() == "PPU.MASK.R"
        registers.emphasizeGreen.getName() == "PPU.MASK.G"
        registers.emphasizeBlue.getName() == "PPU.MASK.B"
        registers.renderSprite.getName() == "PPU.MASK.s"
        registers.renderBackground.getName() == "PPU.MASK.b"
        registers.maskSprite.getName() == "PPU.MASK.M"
        registers.maskBackground.getName() == "PPU.MASK.m"
        registers.greyscale.getName() == "PPU.MASK.G"

        registers.oddFrame.getName() == "PPU.OF"
    }

    def "all data registers are initialized and reachable"() {
        expect:
        registers.dataRegisters.size() == 3
        registers.dataReadBuffer.getName() == "PPU.DATA.R"
        registers.vramAddressIncrement.getName() == "PPU.CTRL.I"
        registers.oamAddress.getName() == "PPU.OAM"
    }

    def "all address registers are initialized and reachable"() {
        expect:
        registers.addressRegisters.size() == 2
        registers.backgroundPatternTable.getName() == "PPU.CTRL.B"
        registers.spritePatternTable.getName() == "PPU.CTRL.S"
    }

    def "should reset Control registers"() {
        given:
        with(registers) {
            vBlankInterruptEnabled.set(true)
            masterSlaveSelect.set(true)
            spriteSize.set(true)
            backgroundPatternTable.set(USHORT_MAX_VALUE)
            spritePatternTable.set(USHORT_MAX_VALUE)
            vramAddressIncrement.set(UBYTE_MAX_VALUE)
        }

        when:
        registers.resetControl()

        then:
        with(registers) {
            !vBlankInterruptEnabled.get()
            !masterSlaveSelect.get()
            !spriteSize.get()
            backgroundPatternTable.get() == USHORT_0
            spritePatternTable.get() == USHORT_0
            vramAddressIncrement.get() == ubyte(1)
        }
    }

    def "should reset Mask registers"() {
        given:
        with(registers) {
            emphasizeRed.set(true)
            emphasizeGreen.set(true)
            emphasizeBlue.set(true)
            renderSprite.set(true)
            renderBackground.set(true)
            maskSprite.set(true)
            maskBackground.set(true)
            greyscale.set(true)
        }

        when:
        registers.resetMask()

        then:
        with(registers) {
            !emphasizeRed.get()
            !emphasizeGreen.get()
            !emphasizeBlue.get()
            !renderSprite.get()
            !renderBackground.get()
            !maskSprite.get()
            !maskBackground.get()
            !greyscale.get()
        }
    }
}
