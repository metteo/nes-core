package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.inject.PpuRegModule
import net.novaware.nes.core.test.TestBus
import net.novaware.nes.core.util.Bin
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.UBYTE_0
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PpuMemDeviceSpec extends Specification {

    def priOam = PpuMemModule.providePrimaryObjAttrMemory()
    def secOam = PpuMemModule.provideSecondaryObjAttrMemory()
    def palette = PpuMemModule.providePaletteMemory()

    def currentViewPort = PpuRegModule.provideCurrentViewPort()
    def temporaryViewPort = PpuRegModule.provideTempViewPort()
    def writeRegister = PpuRegModule.provideSecondWrite()
    def dataReadBuffer = PpuRegModule.provideDataReadBuffer()
    def statusRegister = PpuRegModule.provideStatus()
    def vramAddressIncrement = PpuRegModule.provideVRAMAddressIncrement()
    def backgroundPatternTable = PpuRegModule.provideBackgroundPatternTable()
    def spritePatternTable = PpuRegModule.provideSpritePatternTable()
    def spriteSize = PpuRegModule.provideSpriteSize()
    def masterSlaveSelect = PpuRegModule.provideMasterSlaveSelect()
    def vBlankInterruptEnabled = PpuRegModule.provideVBlankInterruptEnabled()

    def emphasizeRed = PpuRegModule.provideEmphasizeRed()
    def emphasizeGreen = PpuRegModule.provideEmphasizeGreen()
    def emphasizeBlue = PpuRegModule.provideEmphasizeBlue()
    def renderSprite = PpuRegModule.provideRenderSprite()
    def renderBackground = PpuRegModule.provideRenderBackground()
    def maskSprite = PpuRegModule.provideMaskSprite()
    def maskBackground = PpuRegModule.provideMaskBackground()
    def greyscale = PpuRegModule.provideGreyscale()
    def priOamAddress = PpuRegModule.providePrimaryObjAttrAddress()
    def secOamAddress = PpuRegModule.provideSecondaryObjAttrAddress()
    def resetLock = PpuRegModule.provideResetLock()

    def "should construct correctly"() {
        given:
        MemoryBus ppuBus = Mock()
        def ppuMemDevice = newPpuMemDev(ppuBus)

        expect:
        ppuMemDevice.getName() == "CPU<->PPU"
        ppuMemDevice.getStartAddress() == PPU_REGISTERS_START
        ppuMemDevice.getEndAddress() == PPU_REGISTERS_MIRROR_END
    }

    private PpuMemDevice newPpuMemDev(MemoryBus ppuBus) {
        new PpuMemDevice(
            ppuBus,
            palette,
            priOam,
            secOam,
            priOamAddress,
            secOamAddress,
            currentViewPort,
            temporaryViewPort,
            writeRegister,
            dataReadBuffer,
            statusRegister,
            vramAddressIncrement,
            backgroundPatternTable,
            spritePatternTable,
            spriteSize,
            masterSlaveSelect,
            vBlankInterruptEnabled,

            emphasizeRed,
            emphasizeGreen,
            emphasizeBlue,
            renderSprite,
            renderBackground,
            maskSprite,
            maskBackground,
            greyscale,

            resetLock
        )
    }

    private MemoryBus newCpuBus() {
        MemoryBus ppuBus = Mock()
        def ppuMemDevice = newPpuMemDev(ppuBus)
        new TestBus(ppuMemDevice)
    }

    TestBus newPpuBus() {
        PhysicalMemory ppuMem = new PhysicalMemory(
                "PPU",
                PpuMemMap.MEMORY_START,
                PpuMemMap.MEMORY_END,
                PpuMemMap.MEMORY_SIZE
        )

        new TestBus(ppuMem)
    }

    def "should redirect reads to PPU bus"() {
        given:
        def ppuBus = newPpuBus()
        ppuBus.write(0x2000, 0x34)

        def ppuMemDevice = newPpuMemDev(ppuBus)
        def cpuBus = new TestBus(ppuMemDevice)

        when:
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x20))
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x00))

        // first read returns previous value. second read returns actual data
        def wrongData = cpuBus.access(ushort(0x2007 + mirror)).read().data()
        def ppuData = cpuBus.access(ushort(0x2007 + mirror)).read().data()

        then:
        wrongData == UBYTE_0 // no prev value
        ppuData == ubyte(0x34)

        where:
        mirror << [0, 8 , 0x1FF8, 0x1FF0]
    }

    def "should redirect writes to PPU bus"() {
        given:
        def ppuBus = newPpuBus()

        def ppuMemDevice = newPpuMemDev(ppuBus)
        def cpuBus = new TestBus(ppuMemDevice)

        def value = ubyte(0x34)

        when:
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x20))
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x00))

        cpuBus.access(ushort(0x2007 + mirror)).write().data(value)

        then:
        ppuBus.read(0x2000) == value

        where:
        mirror << [0, 8 , 0x1FF8, 0x1FF0]
    }

    def "should redirect reads to Palette memory"() {
        given:
        def ppuBus = newPpuBus()

        def ppuMemDevice = newPpuMemDev(ppuBus)
        def cpuBus = new TestBus(ppuMemDevice)

        def value = ubyte(0x08)

        palette.setColor(PaletteMemory.Section.BACKGROUND, 0, 0, value)

        when:
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x3F))
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x00))

        // TODO: immediate read returns previous value. needs ppu cycle to get actual data?
        def ppuData = cpuBus.access(ushort(0x2007 + mirror)).read().data()

        then:
        ppuData == value

        where:
        mirror << [0, 8 , 0x1FF8, 0x1FF0]
    }

    def "should redirect writes to Palette memory"() {
        given:
        def ppuBus = newPpuBus()

        def ppuMemDevice = newPpuMemDev(ppuBus)
        def cpuBus = new TestBus(ppuMemDevice)

        def value = ubyte(0x08)

        when:
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x3F))
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x00))

        cpuBus.access(ushort(0x2007 + mirror)).write().data(value)

        then:
        palette.getColor(PaletteMemory.Section.BACKGROUND, 0, 0) == value

        where:
        mirror << [0, 8 , 0x1FF8, 0x1FF0]
    }

    def "should return PPU status and clear vBlank/secondWrite flags"() {
        given:
        def cpuBus = newCpuBus()

        writeRegister.set(true)
        statusRegister.verticalBlank = vb
        statusRegister.spriteZeroHit = s0h
        statusRegister.spriteOverflow = so

        when:
        def statusBits = cpuBus.access(PPU_STATUS_REGISTER).read().data()
        statusRegister.cycle()

        then:
        Bin.s(statusBits) == Bin.s(ubyte(bits))
        !statusRegister.verticalBlank
        !writeRegister.get()

        where:
        vb    | s0h   | so    || bits
        true  | false | true  || 0b1010_0000
        false | true  | false || 0b0100_0000
        // TODO: consider 3 isolated cases, plus delayed writes
    }

    def "should update PPU control"() {
        given:
        def cpuBus = newCpuBus()

        when:
        cpuBus.access(PPU_CONTROL_REGISTER).write().data(ubyte(bits))

        then:
        temporaryViewPort.nameTable == nn
        vramAddressIncrement.getAsInt() == i
        spritePatternTable.getAsInt() == s
        backgroundPatternTable.getAsInt() == b
        spriteSize.get() == h
        masterSlaveSelect.get() == p
        vBlankInterruptEnabled.get() == v

        where:
        bits        || nn   | i  | s      | b      | h     | p     | v
        0b1010_1010 || 0b10 |  1 | 0x1000 | 0x0000 | true  | false | true
        0b0101_0101 || 0b01 | 32 | 0x0000 | 0x1000 | false | true  | false
        // TODO: consider 7 isolated cases
        // TODO: test writes are ignored when resetLock is active
    }

    def "should update PPU mask"() {
        given:
        def cpuBus = newCpuBus()

        when:
        cpuBus.access(PPU_MASK_REGISTER).write().data(ubyte(bits))

        // FIXME: make the delay nicer than this
        renderSprite.cycle()
        renderSprite.cycle()
        renderSprite.cycle()
        renderBackground.cycle()
        renderBackground.cycle()
        renderBackground.cycle()
        renderBackground.cycle()

        then:
        emphasizeRed.get() == er
        emphasizeGreen.get() == eg
        emphasizeBlue.get() == eb
        renderSprite.get() == rs
        renderBackground.get() == rb
        maskSprite.get() == ms
        maskBackground.get() == mb
        greyscale.get() == gs

        where:
        bits        || eb    | eg    | er    | rs    | rb    | ms    | mb    | gs
        0b0000_0000 || false | false | false | false | false | true  | true  | false
        0b0000_0110 || false | false | false | false | false | false | false | false
        0b1010_1010 || true  | false | true  | false | true  | true  | false | false
        0b0101_0101 || false | true  | false | true  | false | false | true  | true
        // TODO: consider 8 isolated cases
        // TODO: test writes are ignored when resetLock is active
    }

    def "should update OAM Address"() {
        given:
        def cpuBus = newCpuBus()

        when:
        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).write().data(ubyte(0x12))

        then:
        priOamAddress.getAsInt() == 0x12
    }

    def "should read from OAM"() {
        given:
        def cpuBus = newCpuBus()
        def addr = ubyte(address)
        def val = ubyte(value)

        priOam.write(addr, val)

        when:
        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).write().data(addr)
        def read = cpuBus.access(PPU_OAM_DATA_REGISTER).read().data()

        then:
        priOamAddress.get() == addr // no increment
        sint(read) == actualVal

        where:
        address | value || actualVal
        0x12    | 0x34  || 0x34 & 0xE3 // attributes unused bits as 0s
        0x13    | 0x35  || 0x35
    }

    def "should write to OAM"() {
        given:
        def cpuBus = newCpuBus()

        when:
        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).write().data(ubyte(0x12))
        cpuBus.access(PPU_OAM_DATA_REGISTER).write().data(ubyte(0x34))
        cpuBus.access(PPU_OAM_DATA_REGISTER).write().data(ubyte(0x56))

        then:
        priOamAddress.getAsInt() == 0x14 // incremented
        priOam.read(ubyte(0x12)) == ubyte(0x34 & 0xE3) // attributes unused bits as 0s
        priOam.read(ubyte(0x13)) == ubyte(0x56)
    }

    def "should update PPU scroll"() {
        given:
        def cpuBus = newCpuBus()

        when:
        cpuBus.access(PPU_SCROLL_REGISTER).write().data(ubyte(xScroll))
        cpuBus.access(PPU_SCROLL_REGISTER).write().data(ubyte(yScroll))

        then:
        temporaryViewPort.getCoarseX() == coarseX
        currentViewPort.getFineX() == fineX

        temporaryViewPort.getCoarseY() == coarseY
        temporaryViewPort.getFineY() == fineY

        temporaryViewPort.getNameTable() == 0
        currentViewPort.getNameTable() == 0

        where:
        xScroll     | yScroll     || coarseX | fineX | coarseY | fineY
        0b00000_000 | 0b00000_000 || 0b00000 | 0b000 | 0b00000 | 0b000
        0b11111_000 | 0b00000_000 || 0b11111 | 0b000 | 0b00000 | 0b000
        0b00000_111 | 0b00000_000 || 0b00000 | 0b111 | 0b00000 | 0b000
        0b00000_000 | 0b11111_000 || 0b00000 | 0b000 | 0b11111 | 0b000
        0b00000_000 | 0b00000_111 || 0b00000 | 0b000 | 0b00000 | 0b111
        0b11111_111 | 0b11111_111 || 0b11111 | 0b111 | 0b11111 | 0b111
    }
}
