package net.novaware.nes.core.register

import spock.lang.Specification

class DoubleCounterSpec extends Specification {

    def "should construct and hold a name and initial value"() {
        given:
        def instance = new DoubleCounter("test")

        expect:
        instance.getName() == "test"
        instance.getValue() == 0d
        !instance.isPositive()
    }

    def "should allow pre-setting a value"() {
        given:
        def counter = new DoubleCounter("pre-set")

        when:
        counter.setValue(42.01d)

        then:
        counter.getValue() == 42.01d
        counter.isPositive()
    }

    def "should increment and decrement unconditionally"() {
        given:
        def counter = new DoubleCounter("unconditional")

        when:
        counter.increment()

        then:
        counter.getValue() == 1d
        counter.isPositive()

        and:
        counter.decrement()

        then:
        counter.getValue() == 0d
        !counter.isPositive()
    }
}
