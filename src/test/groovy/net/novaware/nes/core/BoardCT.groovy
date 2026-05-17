package net.novaware.nes.core


import net.novaware.nes.core.config.ImmutableCoreConfig
import net.novaware.nes.core.config.Platform
import net.novaware.nes.core.config.Region
import net.novaware.nes.core.config.VideoStandard
import spock.lang.Specification

class BoardCT extends Specification {

    def "should construct the Board"() {
        given:
        BoardFactory factory = BoardFactory.newBoardFactory(ImmutableCoreConfig.builder()
                .setRecordCpuBus(true)
                .setRegion(Region.USA)
                .setPlatform(Platform.NES_FAMICOM)
                .setVideoStandard(VideoStandard.NTSC)
                .build()
        )

        when:
        def board = factory.newBoard()

        then:
        board != null // no errors
    }
}
