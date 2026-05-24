package net.novaware.nes.core.cart.internal

import net.novaware.nes.core.file.NesFileBuilder
import net.novaware.nes.core.memory.BankedMemory
import net.novaware.nes.core.ppu.memory.PpuMemMap
import net.novaware.nes.core.test.TestBus
import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import static net.novaware.nes.core.util.Quantity.Unit.BANK_1KB
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE
import static net.novaware.nes.core.util.UTypes.sint

class CartridgeImplSpec extends Specification {

    def "should correctly read from program/video data"() {
        given:
        def nesFile = NesFileBuilder.marioBros().build()
        def programData = nesFile.data().program()
        def cartridge = new CartridgeImpl(nesFile)
        def vram = new BankedMemory("VRAM", PpuMemMap.VRAM_START, new Quantity(1, BANK_1KB))
                .setPhysicalBanks(new Quantity(2, BANK_1KB))
                .allocatePhysicalBanks(() -> UBYTE_MAX_VALUE);

        def program = new TestBus(cartridge.getCpuBusDevice())
        def video = new TestBus(cartridge.getPpuBusDevice(vram))

        program.write(0x6000, 0xAB)
        program.write(0x7FFF, 0xCD)

        video.write(0x2000, 0x23)
        video.write(0x27FF, 0x45)
        video.write(0x2800, 0x67)
        video.write(0x2FFF, 0x89)

        expect:
        // wram
        program.read(0x6000) == 0xAB
        program.read(0x7FFF) == 0xCD

        // program
        program.read(0x8000) == sint(programData.get(0))
        program.read(0xBFFF) == sint(programData.get(0x3FFF))

        // program mirroring in second 16 KB
        program.read(0xC000) == sint(programData.get(0))
        program.read(0xFFFF) == sint(programData.get(0x3FFF))

        // vram
        video.read(0x2000) == 0x23
        video.read(0x27FF) == 0x45
        video.read(0x2800) == 0x67
        video.read(0x2FFF) == 0x89

        // vram horizontal mirroring
        video.read(0x2000) == video.read(0x2400)
        video.read(0x27FF) == video.read(0x23FF)
        video.read(0x2800) == video.read(0x2C00)
        video.read(0x2FFF) == video.read(0x2BFF)
    }
}
