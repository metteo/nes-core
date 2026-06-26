package net.novaware.nes.core.clock

import net.novaware.nes.core.clock.inject.ClockModule
import net.novaware.nes.core.config.ImmutableCoreConfig
import net.novaware.nes.core.config.Platform
import net.novaware.nes.core.config.Region
import net.novaware.nes.core.config.VideoStandard
import net.novaware.nes.core.mx.NesCoreRecorder
import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class MasterClockSpec extends Specification {

    def coreConfig = ImmutableCoreConfig.builder()
            .setRecordCpuBus(false)
            .setRegion(Region.USA)
            .setPlatform(Platform.NES_FAMICOM)
            .setVideoStandard(VideoStandard.NTSC)
            .build()

    def frameToggle = PpuRegModule.provideFrameToggle()

    def random = new Random()

    def cpu = new ClockReceiver() {
        @Override
        int cycle() {
            int cycles = random.nextInt(2, 8)
            return cycles
        }
    }

    def ppu = new ClockReceiver() {
        @Override
        int cycle() {
            return 1
        }
    }

    def apu = new ClockReceiver() {
        @Override
        int cycle() {
            return 1
        }
    }

    def dma = new ClockReceiver() {
        @Override
        int cycle() {
            return 1
        }
    }

    def videoEncoder = new ClockReceiver() {
        @Override
        int cycle() {
            return 1
        }
    }

    def executor = ClockModule.provideClockExecutor()
    def mxBean = Mock(NesCoreRecorder)

    private MasterClock newMasterClock() {
        new MasterClock(coreConfig, frameToggle, cpu, ppu, apu, dma, executor, videoEncoder, mxBean)
    }

    def "should calculate cycles budget for a frame"() {
        given:
        def masterClock = newMasterClock()

        frameToggle.set(oddFrameVal)

        when:
        masterClock.calculateFrameBudget()

        then:
        masterClock.ppuClockBudget.getValue() == ppuCycles
        masterClock.cpuClockBudget.getValue() == 357368
        masterClock.apuClockBudget.getValue() == 357368
        masterClock.dmaClockBudget.getValue() == 357368

        where:
        oddFrameVal | ppuCycles
        true        | 357364
        false       | 357368
    }

    def "should calculate second budget"() {
        given:
        def masterClock = newMasterClock()

        when:
        masterClock.calculateSecondBudget()

        then:
        assertThat(masterClock.frameBudget.getValue(), closeTo(60.098d, 0.001d))
        masterClock.frameDuration == 16639356L
    }

    def "should run for a second"() {
        given:
        def masterClock = newMasterClock()
        masterClock.timeTicking = true
        masterClock.timeRestricted = true
        masterClock.timeBudget.setValue(1)

        when:
        masterClock.run()

        then:
        masterClock.timeBudget.getValue() == 0
        masterClock.timeCounter.getValue() == 1
        masterClock.frameCounter.getValue() == 60


    }
}
