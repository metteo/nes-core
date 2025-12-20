package net.novaware.nes.core.cpu.instruction

import net.novaware.nes.core.cpu.instruction.Addressing
import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.cpu.instruction.InstructionGroup
import spock.lang.Specification

class InstructionSpec extends Specification {

    def "should construct correctly with getters"() {
        given:
        Instruction adcImmediate = Instruction.Ox69

        expect:
        adcImmediate.group() == InstructionGroup.ADD_WITH_CARRY
        adcImmediate.addressing() == Addressing.IMMEDIATE
        adcImmediate.opcode() == (byte) 0x69
        adcImmediate.size() == 2
    }
}
