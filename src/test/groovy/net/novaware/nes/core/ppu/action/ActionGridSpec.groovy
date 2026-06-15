package net.novaware.nes.core.ppu.action

import net.novaware.nes.core.config.VideoStandard
import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

class ActionGridSpec extends Specification {

    def frameCounter = PpuRegModule.provideFrameCounter()
    def lineCounter = PpuRegModule.provideLineCounter()
    def dotCounter = PpuRegModule.provideDotCounter()

    def "should construct an instance"() {
        when:
        def instance = new ActionGrid(vs, frameCounter, lineCounter, dotCounter)

        then:
        instance != null

        where:
        vs << [VideoStandard.NTSC, VideoStandard.PAL]
    }
}
