package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.cpu.instruction.InstructionGroup

import static net.novaware.nes.core.cpu.instruction.Instruction.*
import static net.novaware.nes.core.cpu.instruction.InstructionGroup.BRANCH_IF_ZERO_CLR
import static net.novaware.nes.core.util.UTypes.*

class ControlUnitAddressingSpec extends ControlUnitBaseSpec {

    def "should decode accumulator mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox0A.opcode())
        insRegs.currentOperand.set(USHORT_0)

        regs a: 0x28

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.SHIFT_LEFT.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x28)
    }

    def "should decode absolute mode" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox0E.opcode())
        insRegs.currentOperand.setAsShort(0x1234)

        ram 0x1234, 0x42

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.SHIFT_LEFT.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x42)
    }

    def "should decode absolute x / y indexed mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(instr.opcode())
        insRegs.currentOperand.setAsShort(0x2345)

        regs x: x, y: y
        ram (
            (0x2345 + x + y), 0x13
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == cycles
        insRegs.decodedInstruction.getAsInt() == instr.group().ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x13)

        where:
        instr | x    | y    | cycles
        Ox1E  | 0xBC | 0    | 1 // write oops
        Ox1E  | 0x01 | 0    | 1 // write oops
        Ox19  | 0    | 0xCD | 1 // page cross oops
        Ox19  | 0    | 0x02 | 0
    }

    def "should decode immediate mode" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox09.opcode())
        insRegs.currentOperand.setAsShort(0x78)

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x78)
    }

    def "should decode absolute indirect mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Instruction.Ox6C.opcode())
        insRegs.currentOperand.setAsShort(operand)

        ram(
            0x0300, 0x34, // bb

            0x03FF, 0x56, // aa
            0x0400, 0x78, // cc
            0x0401, 0x90  // dd
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 2
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.JUMP_TO_LOCATION.ordinal()
        insRegs.decodedOperand.getAddress() == ushort(result)

        where:
        operand | result
        0x03FF  | 0x3456 // buggy cpu
        0x0400  | 0x9078 // no page crossing
    }

    def "should decode pre indexed indirect x mode" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Instruction.Ox01.opcode())
        insRegs.currentOperand.setAsShort(0x20)

        regs x: 0x04
        ram(
            0x0024, 0x12, // Lo
            0x0025, 0x60, // Hi
            0x6012, 0x42  // Target
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 3
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x42)
    }

    // TODO: this doesn't correctly test wrapping. it should be 0x00FF, 0x0000
    def "should decode pre indexed indirect x mode with zero page wrap"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Instruction.Ox01.opcode())
        insRegs.currentOperand.setAsShort(0xFF)

        regs x: 0x01
        ram(
            0x0000, 0x12, // (0xFF + 0x01) & 0xFF == 0x00
            0x0001, 0x60,
            0x6012, 0x99
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 3
        insRegs.decodedOperand.getData() == ubyte(0x99)
    }

    def "should decode post indexed indirect y mode" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Instruction.Ox11.opcode())
        insRegs.currentOperand.setAsShort(address)

        regs y: y
        ram(
            address        , 0xFD,
            (address+1)%256, 0x60,
            (0x60FD + y), 0x77
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == cycles
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x77)

        where:
        address | y | cycles | comment
        0x00FE  | 1 | 2      | "no wrap"
        0x00FF  | 2 | 2      | "zero page wrap"
        0x00AB  | 3 | 3      | "mem page wrap"
    }

    def "should decode zero page mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Instruction.Ox05.opcode())
        insRegs.currentOperand.setAsShort(0x42)

        ram 0x0042, 0x13

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x13)
    }

    def "should decode indexed zero page x / y mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(instr.opcode())
        insRegs.currentOperand.setAsShort(0x80)

        regs x: x, y: y
        ram(
            ((0x0080 + x + y) & 0xFF), 0x55
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 1 // memory read without adding index register
        insRegs.decodedInstruction.getAsInt() == instr.group().ordinal()
        insRegs.decodedOperand.getData() == ubyte(0x55)

        where:
        instr | x    | y
        Ox16  | 0x0F | 0    // ASL $80,X
        Ox96  | 0    | 0x02 // STX $80,Y
    }

    def "should decode relative mode"() {
        given:
        def cu = newControlUnit()

        regs pc: 0x0025
        insRegs.currentInstruction.set(OxD0.opcode())
        insRegs.currentOperand.setAsShort(operand)

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getAsInt() == BRANCH_IF_ZERO_CLR.ordinal()
        insRegs.decodedOperand.getAddress() == ushort(0x0025 + operand)

        where:
        operand << [-2, 2]
    }
}
