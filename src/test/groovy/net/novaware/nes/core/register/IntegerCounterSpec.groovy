package net.novaware.nes.core.register

import spock.lang.Specification

class IntegerCounterSpec extends Specification {

    def "should construct and hold a name and initial value"() {
        given:
        def instance = new IntegerCounter("test")

        expect:
        instance.getName() == "test"
        instance.getValue() == 0
        !instance.isPositive()
    }

    def "should allow pre-setting a value"() {
        given:
        def counter = new IntegerCounter("pre-set")

        when:
        counter.setValue(42)

        then:
        counter.getValue() == 42
        counter.isPositive()
    }

    def "should increment and decrement unconditionally"() {
        given:
        def counter = new IntegerCounter("unconditional")

        when:
        counter.increment()

        then:
        counter.getValue() == 1
        counter.isPositive()

        and:
        counter.decrement()

        then:
        counter.getValue() == 0
        !counter.isPositive()
    }

    def "should increment and decrement conditionally"() {
        given:
        def counter = new IntegerCounter("conditional")

        when:
        counter.maybeIncrement(true)

        then:
        counter.getValue() == 1
        counter.isPositive()

        and:
        counter.maybeIncrement(false)

        then:
        counter.getValue() == 1
        counter.isPositive()

        and:
        counter.maybeDecrement(true)

        then:
        counter.getValue() == 0
        !counter.isPositive()

        and:
        counter.maybeDecrement(false)

        then:
        counter.getValue() == 0
        !counter.isPositive()

        and:
        counter.maybeDecrement(true)

        then:
        counter.getValue() == -1
        !counter.isPositive()
    }

    def "should reset to zero"() {
        given:
        def counter = new IntegerCounter("reset")

        when:
        counter.setValue(42)
        counter.reset()

        then:
        counter.getValue() == 0
        !counter.isPositive()
    }

    def "should decrement by an amount"() {
        given:
        def counter = new IntegerCounter("decrement")

        when:
        counter.setValue(42)
        counter.decrementBy(10)

        then:
        counter.getValue() == 32
        counter.isPositive()
    }
}
