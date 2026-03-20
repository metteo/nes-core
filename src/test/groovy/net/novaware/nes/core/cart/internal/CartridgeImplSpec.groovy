package net.novaware.nes.core.cart.internal

import net.novaware.nes.core.file.NesFileBuilder
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.sint

class CartridgeImplSpec extends Specification {

    def "should correctly read from program data"() {
        given:
        def nesFile = NesFileBuilder.marioBros().build()
        def programData = nesFile.data().program()
        def cartridge = new CartridgeImpl(nesFile)

        def program = new TestBus(cartridge.getCpuBusDevice())
        def video = cartridge.getPpuBusDevice()

        program.write(0x6000, 0xAB)
        program.write(0x7FFF, 0xCD)

        expect:
        program.read(0x6000) == 0xAB
        program.read(0x7FFF) == 0xCD

        program.read(0x8000) == sint(programData.get(0))
        program.read(0xBFFF) == sint(programData.get(0x3FFF))

        // mirroring in second 16 KB
        program.read(0xC000) == sint(programData.get(0))
        program.read(0xFFFF) == sint(programData.get(0x3FFF))
    }
}
