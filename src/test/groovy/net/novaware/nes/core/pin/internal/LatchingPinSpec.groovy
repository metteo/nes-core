package net.novaware.nes.core.pin.internal

import net.novaware.nes.core.cpu.signal.Signal
import net.novaware.nes.core.cpu.signal.internal.LevelDetector
import net.novaware.nes.core.register.BooleanRegister
import spock.lang.Specification

class LatchingPinSpec extends Specification {

    def detector = new LevelDetector("test", Signal.LOW)
    def register = new BooleanRegister("spec")

    def "should construct an instance"() {
        when:
        def instance = new LatchingPin("verifier", detector, register::set)

        then:
        instance.getName() == "verifier"
        instance.toString() == "verifier"
    }

    def "should forward detected state to register with latching"() {
        given:
        def pin = new LatchingPin("a", detector, register::set)

        when:
        pin.set(Signal.LOW)

        then:
        register.get()

        and:
        pin.set(Signal.HIGH)

        then:
        register.get()

        and:
        register.set(false)
        pin.set(Signal.HIGH)

        then:
        !register.get()
    }
}
