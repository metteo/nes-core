package net.novaware.nes.core.util;

import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.BiConsumer
import java.util.function.Supplier

import static net.novaware.nes.core.util.Asserts.assertArgument
import static net.novaware.nes.core.util.Asserts.assertState

class AssertsSpec extends Specification {

    def "should not throw exception if assertion is met" () {
        when:
        assertArgument(true, "1")
        assertArgument(true, {-> "2"})
        assertState(true, "3")
        assertState(true, {-> "4"})

        then:
        noExceptionThrown()
    }

    def "should throw IAE with message provided directly" () {
        when:
        assertArgument(false, "1")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "1"
    }

    def "should throw IAE with message Supplied" () {
        when:
        assertArgument(false, {-> "1"})

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "1"
    }

    def "should throw ISE with message provided directly" () {
        when:
        assertState(false, "1")

        then:
        def e = thrown(IllegalStateException)
        e.message == "1"
    }

    def "should throw ISE with message Supplied" () {
        when:
        assertState(false, {-> "1"})

        then:
        def e = thrown(IllegalStateException)
        e.message == "1"
    }

    @Unroll
    def "should throw NPE early if string message is null" () {
        when:
        ((BiConsumer<Boolean, String>)method).accept(false, null)

        then:
        thrown(NullPointerException)

        where:
        method                   | _
        Asserts.&assertArgument  | _
        Asserts.&assertState     | _
    }

    @Unroll
    def "should throw NPE early if supplied message is null" () {
        when:
        ((BiConsumer<Boolean, Supplier<String>>)method).accept(false, msg)

        then:
        thrown(NullPointerException)

        where:
        method                   | msg
        Asserts.&assertArgument  | null
        Asserts.&assertArgument  | {-> null}
        Asserts.&assertState     | null
        Asserts.&assertState     | {-> null}
    }
}
