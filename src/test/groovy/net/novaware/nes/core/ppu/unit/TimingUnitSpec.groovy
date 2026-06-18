package net.novaware.nes.core.ppu.unit

import net.novaware.nes.core.config.VideoStandard
import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.config.VideoStandard.NTSC
import static net.novaware.nes.core.config.VideoStandard.PAL

class TimingUnitSpec extends Specification {

    def frameCounter = PpuRegModule.provideFrameCounter()
    def frameToggle = PpuRegModule.provideFrameToggle()
    def lineCounter = PpuRegModule.provideLineCounter()
    def dotCounter = PpuRegModule.provideDotCounter()

    def renderSprite = PpuRegModule.provideRenderSprite()
    def renderBackground = PpuRegModule.provideRenderBackground()

    TimingUnit newActionGrid(VideoStandard vs) {
        def ag = new TimingUnit(vs, frameCounter, frameToggle, lineCounter, dotCounter, renderSprite, renderBackground)
        ag.initialize()
        ag
    }

    def "should construct an instance"() {
        when:
        def instance = newActionGrid(vs)

        then:
        instance != null

        where:
        vs << [NTSC, PAL]
    }

    def "should cycle through dots and lines"() {
        given:
        frameToggle.set(inFt)
        lineCounter.setValue(inLi)
        dotCounter.setValue(inDot)

        renderSprite.set(inRs)
        renderBackground.set(inRb)

        def grid = newActionGrid(vs)

        when:
        grid.increment()

        then:
        frameCounter.getValue() == (nextFr ? 1 : 0)
        frameToggle.get() == outFt
        lineCounter.getValue() == outLi
        dotCounter.getValue() == outDot

        renderSprite.get() == inRs
        renderBackground.get() == inRb

        where:
        vs   | inRs | inRb | inFt  | inLi | inDot || nextFr | outFt  | outLi | outDot | comment
        //                                                                            | "NTSC, FT=n"
        NTSC | true | true | false |    0 |     0 || false  | false  |     0 |      1 | "next dot"
        NTSC | true | true | false |    0 |   340 || false  | false  |     1 |      0 | "next line"
        NTSC | true | true | false |  261 |   339 || false  | false  |   261 |    340 | "last dot, no skip"
        NTSC | true | true | false |  261 |   340 || true   | true   |     0 |      0 | "next frame"
        //                                                                            | "NTSC, FT=y"
        NTSC | true | true | true  |    0 |     0 || false  | true   |     0 |      1 | "next dot"
        NTSC | true | true | true  |    0 |   340 || false  | true   |     1 |      0 | "next line"
        NTSC | false| false| true  |  261 |   339 || false  | true   |   261 |    340 | "no skip on render=n"
        NTSC | true | true | true  |  261 |   339 || true   | false  |     0 |      0 | "skip    on render=y"
        //                                                                            | "PAL, FT=n"
        PAL  | true | true | false |    0 |     0 || false  | false  |     0 |      1 | "next dot"
        PAL  | true | true | false |    0 |   340 || false  | false  |     1 |      0 | "next line"
        PAL  | true | true | false |  311 |   339 || false  | false  |   311 |    340 | "last dot"
        PAL  | true | true | false |  311 |   340 || true   | true   |     0 |      0 | "next frame"
        //                                                                            | "PAL, FT=y"
        PAL  | true | true | true  |    0 |     0 || false  | true   |     0 |      1 | "next dot"
        PAL  | true | true | true  |    0 |   340 || false  | true   |     1 |      0 | "next line"
        PAL  | true | true | true  |  311 |   339 || false  | true   |   311 |    340 | "last dot"
        PAL  | true | true | true  |  311 |   340 || true   | false  |     0 |      0 | "next frame"
    }

    def "should print dots and lines"() {
        given:
        renderBackground.set(true)

        def grid = newActionGrid(NTSC)

        def output = ""
        boolean shouldPrint = false

        when:
        for(int i = 0; i < 89341.5 * 60.0988; i++) { // 1 second worth of dots for NTSC
            output = frameCounter.getValue() +
                (frameToggle.get() ? "*" : "") + "," +
                lineCounter.getValue() + "," +
                dotCounter.getValue()

            int dot = dotCounter.getValue()
            if (shouldPrint & ((0 <= dot && dot <= 1) || (339 <= dot && dot <= 340))) {
                println(output)
            }

            grid.increment()
        }

        then:
        output == "60,25,301" // that is the 0.0988 fraction of the frame
    }
}
