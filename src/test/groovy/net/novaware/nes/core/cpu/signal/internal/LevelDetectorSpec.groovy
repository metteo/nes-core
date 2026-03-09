package net.novaware.nes.core.cpu.signal.internal


import spock.lang.Specification

import static net.novaware.nes.core.cpu.signal.Signal.HIGH
import static net.novaware.nes.core.cpu.signal.Signal.LOW

class LevelDetectorSpec extends Specification {

    def "should hold name and init inactive "() {
        given:
        def detector = new LevelDetector("a", LOW)

        expect:
        detector.getName() == "a"
        !detector.isActive()
    }

    def "should report active and inactive state"() {
        given:
        def detector = new LevelDetector("IRQ", LOW)

        when:
        detector.set(LOW)

        then:
        detector.isActive()

        and:
        detector.set(HIGH)

        then:
        !detector.isActive()

        and:
        detector.set(LOW)

        then:
        detector.isActive()
    }
}
