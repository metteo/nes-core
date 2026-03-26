package net.novaware.nes.core.cpu.register

import net.novaware.nes.core.cpu.instruction.AddressingMode
import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.cpu.instruction.InstructionGroup
import spock.lang.Specification

class InstructionRegisterSpec extends Specification {

    def "should construct and hold the name"() {
        given:
        def ir = new InstructionRegister("DI")

        expect:
        ir.getName() == "DI"
        ir.group == InstructionGroup.UNKNOWN
        ir.addressing == AddressingMode.UNKNOWN
    }
    def "should set instruction group and addressing mode"() {
        given:
        def ir = new InstructionRegister("DI")
        def instruction = Instruction.OxA9 // LDA Immediate

        when:
        ir.set(instruction)

        then:
        ir.getGroup() == InstructionGroup.LOAD_A_WITH_MEMORY
        ir.getAddressing() == AddressingMode.IMMEDIATE
    }

    def "should provide string representation"() {
        given:
        def ir = new InstructionRegister("DI")
        ir.set(Instruction.Ox00) // BRK Implied/Immediate

        expect:
        ir.toString() == "DI: FORCE_BREAK IMMEDIATE"
    }
    
    def "should handle unknown instruction"() {
        given:
        def ir = new InstructionRegister("DI")
        def instruction = Instruction.OxUK

        when:
        ir.set(instruction)

        then:
        ir.getGroup() == InstructionGroup.UNKNOWN
        ir.getAddressing() == AddressingMode.UNKNOWN
    }
}
