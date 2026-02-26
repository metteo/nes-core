package net.novaware.nes.core.cpu.instruction

import spock.lang.Specification

import java.util.stream.Stream

import static net.novaware.nes.core.util.UTypes.sint

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
        Instruction.values().length == 153

        and: "only OxUK should use 0xFF pseudo opcode"
        Stream.of(Instruction.values())
                .map(Instruction::opcode)
                .filter(o -> sint(o) == 0xFF)
                .count() == 1
    }

    def "should cross check instruction sizes with addressing mode sizes"() {
        expect:
        for (Instruction i : Instruction.values()) {
            assert i.size() == i.addressingMode().size() + 1
        }
    }
}
