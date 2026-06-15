package net.novaware.nes.core.ppu.unit

import net.novaware.nes.core.config.VideoStandard
import net.novaware.nes.core.ppu.action.Action
import net.novaware.nes.core.ppu.action.ScanLine
import net.novaware.nes.core.ppu.inject.PpuDepModule
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.inject.PpuRegModule
import net.novaware.nes.core.ppu.memory.PpuBus
import spock.lang.Specification

import java.util.function.Function
import java.util.stream.Stream

import static java.util.stream.Collectors.counting
import static java.util.stream.Collectors.groupingBy
import static net.novaware.nes.core.config.CoreConfigBuilder.ntsc
import static net.novaware.nes.core.config.VideoStandard.NTSC
import static net.novaware.nes.core.config.VideoStandard.PAL

class ControlUnitSpec extends Specification {

    def config = ntsc()
    def cycleCounter = PpuRegModule.provideCycleCounter()
    def scanLineCounter = PpuRegModule.provideLineCounter()
    def dotCounter = PpuRegModule.provideDotCounter()
    def oddFrame = PpuRegModule.provideOddFrame()

    def status = PpuRegModule.provideStatus()
    def hBlank = PpuRegModule.provideHorizontalBlank()
    def vBlankInterruptEnabled = PpuRegModule.provideVBlankInterruptEnabled()
    def vBlankInterrupt = PpuDepModule.provideVBlankInterruptPin(PpuDepModule.provideVBlankInterruptReg())
    def renderSprite = PpuRegModule.provideRenderSprite()
    def renderBackground = PpuRegModule.provideRenderBackground()
    def sprite0Hit = PpuDepModule.provideSprite0HitPin(PpuDepModule.provideSprite0HitReg())
    def currentViewPort = PpuRegModule.provideCurrentViewPort()
    def tempViewPort = PpuRegModule.provideTempViewPort()
    def resetLock = PpuRegModule.provideResetLock()
    def bus = new PpuBus()
    def backgroundPatternTable = PpuRegModule.provideBackgroundPatternTable()
    def spritePatternTable = PpuRegModule.provideSpritePatternTable()
    def videoOut = PpuRegModule.provideVideoOutRegister()
    def paletteMemory = PpuMemModule.providePaletteMemory()

    def "should construct an instance"() {
        when:
        def instance = newCu()

        then:
        instance != null
    }

    def "should initiate scan lines"() {
        given:
        def cu = newCu(vs)

        when:
        def counts = countActions(cu.scanLines)

        then:
        counts.get(ScanLine.RENDER_START) == 1
        counts.get(ScanLine.RENDERING)    == 238
        counts.get(ScanLine.RENDER_END)   == 1
        counts.get(ScanLine.POST_RENDER)  == 1
        counts.get(ScanLine.BLANK_START)  == 1
        counts.get(ScanLine.BLANKING)     == blanking
        counts.get(ScanLine.BLANK_END)    == 1
        counts.get(ScanLine.PRE_RENDER)   == 1
        counts.size() == 8

        where:
        vs   || blanking
        NTSC || 18
        PAL  || 68
    }

    def "should initiate bus actions"() {
        given:
        def cu = newCu()

        when:
        def counts = countActions(cu.busActions)

        then:
        counts.get(Action.ACCESS_NAME_TABLE_ADDRESS)  == 32 + 2 // current + next scan line
        counts.get(Action.READ_NAME_TABLE_DATA)       == 32 + 2
        counts.get(Action.ACCESS_ATTR_TABLE_ADDRESS)  == 32 + 2
        counts.get(Action.READ_ATTR_TABLE_DATA)       == 32 + 2
        counts.get(Action.ACCESS_BG_LO_BITS_ADDRESS)  == 32 + 2 // + 1 // dot 0 addr only TODO: uncomment + 1 or add special
        counts.get(Action.READ_BG_LO_BITS_DATA)       == 32 + 2
        counts.get(Action.ACCESS_BG_HI_BITS_ADDRESS)  == 32 + 2
        counts.get(Action.READ_BG_HI_BITS_DATA)       == 32 + 2
        counts.get(Action.UNUSED_NAME_TABLE_ADDRESS)  ==  8 + 1 // between sprites and last 4 dots
        counts.get(Action.UNUSED_NAME_TABLE_DATA)     ==  8 + 1
        counts.get(Action.IGNORED_NAME_TABLE_ADDRESS) ==  8 + 1 // between sprites and last 4 dots
        counts.get(Action.IGNORED_NAME_TABLE_DATA)    ==  8 + 1
        counts.get(Action.ACCESS_SP_LO_BITS_ADDRESS)  ==  8
        counts.get(Action.READ_SP_LO_BITS_DATA)       ==  8
        counts.get(Action.ACCESS_SP_HI_BITS_ADDRESS)  ==  8
        counts.get(Action.READ_SP_HI_BITS_DATA)       ==  8
        counts.size() == 17 // TODO: should be 16
    }

    def "should initiate oam actions"() {
        given:
        def cu = newCu()

        when:
        def counts = countActions(cu.oamActions)

        then:
        counts.get(Action.CLR_SECONDARY_OAM) == 64
        counts.get(Action.EVAL_PRIMARY_OAM)  == 192
        counts.get(Action.NO_OPERATION)      == 85
        counts.size() == 3

    }

    def "should initiate draw actions"() {
        given:
        def cu = newCu()

        when:
        def counts = countActions(cu.drawActions)

        then:
        counts.get(Action.RENDER) == 256
        counts.get(Action.NO_OPERATION) == 85
        counts.size() == 2
    }

    def "should initiate rendering view actions"() {
        given:
        def cu = newCu()

        def viewActions = preRender ? cu.preRenderViewActions : cu. renderingViewActions

        when:
        def counts = countActions(viewActions)

        then:
        counts.get(Action.INCREMENT_X) == 31 + 2
        counts.get(Action.INCREMENT_Y) == 1
        counts.get(Action.TRANSFER_TX_TO_X) == 1
        counts.get(Action.NO_OPERATION) == noops
        counts.getOrDefault(Action.TRANSFER_TY_TO_Y, 0) == tty
        counts.size() == size

        where:
        preRender || noops | tty | size
        false     || 306   | 0   | 4
        true      || 281   | 25  | 5
    }

    static <T> Map<T, Long> countActions(T[] actions) {
        Stream.of(actions).collect(groupingBy(Function.identity(), counting()))
    }

    def "should cycle through dots and scanlines"() {
        given:
        def cu = newCu(vs)
        oddFrame.set(inOdd)
        scanLineCounter.setValue(inSl)
        dotCounter.setValue(inDot)

        when:
        cu.nextDot()

        then:
        oddFrame.get() == outOdd
        scanLineCounter.getValue() == outSl
        dotCounter.getValue() == outDot

        where:
        vs   | inOdd | inSl | inDot || outOdd | outSl | outDot
        NTSC | true  |    0 |     0 || true   |     0 |      1
        NTSC | true  |    0 |   340 || true   |     1 |      0
        NTSC | true  |  261 |   340 || false  |     0 |      1 // skip 0,0 on even frames // TODO: wrong!
        NTSC | false |    0 |     0 || false  |     0 |      1
        NTSC | false |    0 |   340 || false  |     1 |      0
        NTSC | false |  261 |   340 || true   |     0 |      0

        PAL  | true  |    0 |     0 || true   |     0 |      1
        PAL  | true  |    0 |   340 || true   |     1 |      0
        PAL  | true  |  311 |   340 || false  |     0 |      0 // no skip on even frames
        PAL  | false |    0 |     0 || false  |     0 |      1
        PAL  | false |    0 |   340 || false  |     1 |      0
        PAL  | false |  311 |   340 || true   |     0 |      0

    }

    ControlUnit newCu() {
        newCu(NTSC)
    }

    ControlUnit newCu(VideoStandard vs) {
        new ControlUnit(
            config.videoStandard(vs).build(),
            cycleCounter,
            scanLineCounter,
            dotCounter,
            oddFrame,
            status,
            hBlank,
            vBlankInterruptEnabled,
            vBlankInterrupt,
            renderSprite,
            renderBackground,
            sprite0Hit,
            currentViewPort,
            tempViewPort,
            resetLock,
            bus,
            backgroundPatternTable,
            spritePatternTable,
            videoOut,
            paletteMemory
        )
    }
}
