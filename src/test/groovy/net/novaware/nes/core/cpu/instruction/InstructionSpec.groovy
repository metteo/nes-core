package net.novaware.nes.core.cpu.instruction

import spock.lang.Specification

class InstructionSpec extends Specification {

    def "should construct correctly with getters"() {
        given:
        Instruction adcImmediate = Instruction.Ox69

        expect:
        adcImmediate.group() == InstructionGroup.ADD_WITH_CARRY
        adcImmediate.addressingMode() == AddressingMode.IMMEDIATE
        adcImmediate.opcode() == (byte) 0x69
        adcImmediate.size() == 2
    }

    def "should contain correct amount of opcodes"() {
        expect:
        Instruction.values().length == 151
    }
}
