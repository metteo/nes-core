package net.novaware.nes.core.register

import net.novaware.nes.core.TestNesCore
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class DelegatingRegisterSpec extends Specification {

    def "should throw on set when set as empty" () {
        given:
        def register = new DelegatingRegister("DOP")
        register.configureEmpty()

        when:
        register.setData(ubyte(0x12))

        then:
        thrown(IllegalStateException)
    }

    def "should throw on get when set as empty" () {
        given:
        def register = new DelegatingRegister("DOP")
        register.configureEmpty()

        when:
        register.getData()

        then:
        thrown(IllegalStateException)
    }

    def "should work with byte data"() {
        given:
        def register = new DelegatingRegister("DOP")


        when:
        register.configureData(ubyte(0x11))

        then:
        register.getData() == ubyte(0x11)

        and:
        register.setData(ubyte(0x12))

        then:
        register.getData() == ubyte(0x12)
    }

    def "should work with ByteRegister"() {
        given:
        def accumulator = new ByteRegister("A")
        accumulator.setAsByte(0x34)

        def register = new DelegatingRegister("DOP")
        register.configureDataRegister(accumulator)

        when:
        def prevValue = register.getData()

        then:
        prevValue == ubyte(0x34)

        and:
        register.setData(ubyte(0x12))

        then:
        accumulator.get() == ubyte(0x12)
        register.getData() == ubyte(0x12)
    }

    def "should work with memory address"() {
        given:
        def cpuBus = TestNesCore.newTestNesCore().newCpuBus()
        cpuBus.access(ushort(0x0012)).write().data(ubyte(0x34))
        def register = new DelegatingRegister("DOP")
        register.configureMemory(cpuBus, ushort(0x0012))

        when:
        def prevValue = register.getData()

        then:
        prevValue == ubyte(0x34)

        and:
        register.setData(ubyte(0x56))

        then:
        cpuBus.access(ushort(0x0012)).read().data() == ubyte(0x56)
        register.getData() == ubyte(0x56)
    }
}
