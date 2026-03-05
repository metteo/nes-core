package net.novaware.nes.core.cpu.signal.internal


import spock.lang.Specification

import static net.novaware.nes.core.cpu.signal.Signal.HIGH
import static net.novaware.nes.core.cpu.signal.Signal.LOW

class EdgeDetectorSpec extends Specification {

    def "should hold name and init inactive "() {
        given:
        def detector = new EdgeDetector("a", LOW)

        expect:
        detector.getName() == "a"
        !detector.isActive()
    }

    def "should report active state"() {
        given:
        def detector = new EdgeDetector("IRQ", LOW)

        when:
        detector.set(LOW)

        then:
        detector.isActive()

        and:
        detector.set(LOW)

        then:
        !detector.isActive()

        and:
        detector.set(HIGH)

        then:
        !detector.isActive()

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
