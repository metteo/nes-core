package net.novaware.nes.core.cpu.register


import spock.lang.Specification
import spock.lang.Subject

import static net.novaware.nes.core.TestBoardFactory.newTestBoardFactory

class CpuRegFileSpec extends Specification {

    @Subject
    CpuRegFile registers = newTestBoardFactory().newCpuRegisters()

    def "all data registers are initialized and reachable"() {
        expect:
        registers.dataRegisters.size() == 4
        registers.a().getName() == "A"
        registers.x().getName() == "X"
        registers.y().getName() == "Y"
        registers.sp().getName() == "S"
    }

    def "all address registers are initialized and reachable"() {
        expect:
        registers.addressRegisters.size() == 1
        registers.pc().getName() == "PC"
    }

    def "shorthand methods return the same instance as full getters"() {
        expect:
        registers.pc() == registers.getProgramCounter()
        registers.a() == registers.getAccumulator()
        registers.x() == registers.getIndexX()
        registers.y() == registers.getIndexY()
        registers.sp() == registers.getStackPointer()
        registers.status() == registers.getStatus()
    }

    def "special registers are initialized correctly"() {
        expect:
        registers.status().getName() == "P"
    }
}
