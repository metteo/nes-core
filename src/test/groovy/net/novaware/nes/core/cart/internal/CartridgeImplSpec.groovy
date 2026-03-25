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
        def video = new TestBus(cartridge.getPpuBusDevice())

        program.write(0x6000, 0xAB)
        program.write(0x7FFF, 0xCD)

        video.write(0x2000, 0x23)
        video.write(0x27FF, 0x45)

        expect:
        program.read(0x6000) == 0xAB
        program.read(0x7FFF) == 0xCD

        program.read(0x8000) == sint(programData.get(0))
        program.read(0xBFFF) == sint(programData.get(0x3FFF))

        // mirroring in second 16 KB
        program.read(0xC000) == sint(programData.get(0))
        program.read(0xFFFF) == sint(programData.get(0x3FFF))

        video.read(0x2000) == 0x23
        video.read(0x27FF) == 0x45

        // TODO: video ram mirroring
    }
}
