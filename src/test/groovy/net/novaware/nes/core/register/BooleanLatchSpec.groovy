package net.novaware.nes.core.register

import net.novaware.nes.core.cpu.register.StatusRegister
import spock.lang.Specification

class BooleanLatchSpec extends Specification {

    def "should hold name"() {
        given:
        def latch = new BooleanLatch("I", null)

        expect:
        latch.getName() == "I"
    }

    def "should hold value until requested"() {
        given:
        def status = new StatusRegister()
        status.setIrqDisabled(true)
        def latch = new BooleanLatch("I", status::setIrqDisabled)

        when:
        latch.delayedSet(false)

        then:
        status.isIrqDisabled()

        and:
        latch.commit()

        then:
        !status.isIrqDisabled()

        and:
        status.setIrqDisabled(true)
        latch.commit()

        then:
        status.isIrqDisabled() // there is no pending value
    }

    def "should not forward value if reset first"() {
        given:
        def status = new StatusRegister()
        status.setIrqDisabled(true)
        def latch = new BooleanLatch("I", status::setIrqDisabled)

        when:
        latch.delayedSet(false)
        latch.reset()
        latch.commit()

        then:
        status.isIrqDisabled()
    }


}
