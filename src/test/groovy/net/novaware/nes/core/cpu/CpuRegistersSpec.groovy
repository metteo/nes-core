package net.novaware.nes.core.cpu

import spock.lang.Specification
import spock.lang.Subject

class CpuRegistersSpec extends Specification {

    @Subject
    CpuRegisters registers = new CpuRegisters()

    def "all data registers are initialized and reachable"() {
        expect:
        registers.dataRegisters.size() == 6
        registers.a().getName() == "A"
        registers.x().getName() == "X"
        registers.y().getName() == "Y"
        registers.mdr().getName() == "MDR"
        registers.cir().getName() == "CIR"
        registers.dir().getName() == "DIR"
    }

    def "all address registers are initialized and reachable"() {
        expect:
        registers.addressRegisters.size() == 5
        registers.pc().getName() == "PC"
        registers.mar().getName() == "MAR"
        registers.cor().getName() == "COR"
        registers.sp().getName() == "S"
        registers.ss().getName() == "SS"
    }

    def "shorthand methods return the same instance as full getters"() {
        expect:
        registers.pc() == registers.getProgramCounter()
        registers.mar() == registers.getMemoryAddress()
        registers.mdr() == registers.getMemoryData()
        registers.cir() == registers.getCurrentInstruction()
        registers.cor() == registers.getCurrentOperand()
        registers.dir() == registers.getDecodedInstruction()
        registers.dor() == registers.getDecodedOperand()
        registers.a() == registers.getAccumulator()
        registers.x() == registers.getIndexX()
        registers.y() == registers.getIndexY()
        registers.sp() == registers.getStackPointer()
        registers.ss() == registers.getStackSegment()
        registers.status() == registers.getStatus()
    }

    def "special registers are initialized correctly"() {
        expect:
        registers.dor().getName() == "DOR"
        registers.status().getName() == "P"
    }
}
