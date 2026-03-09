package net.novaware.nes.core.cart.internal

import net.novaware.nes.core.file.NesFileBuilder
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class CartridgeImplSpec extends Specification {

    def "should correctly read from program data"() {
        given:
        def nesFile = NesFileBuilder.marioBros().build()
        def programData = nesFile.data().program()
        def cartridge = new CartridgeImpl(nesFile)

        def program = cartridge.getProgram()
        def video = cartridge.getVideo()

        program.specifyThen(ushort(0x6000)).writeByte(ubyte(0xAB))
        program.specifyThen(ushort(0x7FFF)).writeByte(ubyte(0xCD))

        expect:
        program.specifyThen(ushort(0x6000)).readByte() == ubyte(0xAB)
        program.specifyThen(ushort(0x7FFF)).readByte() == ubyte(0xCD)

        program.specifyThen(ushort(0x8000)).readByte() == programData.get(0)
        program.specifyThen(ushort(0xBFFF)).readByte() == programData.get(0x3FFF)

        // mirroring in second 16 KB
        program.specifyThen(ushort(0xC000)).readByte() == programData.get(0)
        program.specifyThen(ushort(0xFFFF)).readByte() == programData.get(0x3FFF)
    }
}
