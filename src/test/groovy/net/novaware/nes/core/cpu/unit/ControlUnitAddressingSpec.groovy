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

        registers.currentInstruction.set(Ox0A.opcode())
        registers.currentOperand.set(USHORT_0)

        regs a: 0x28

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == InstructionGroup.SHIFT_LEFT.ordinal()
        registers.decodedOperand.getData() == ubyte(0x28)
    }

    def "should decode absolute mode" () {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox0E.opcode())
        registers.currentOperand.setAsShort(0x1234)

        ram 0x1234, 0x42

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == InstructionGroup.SHIFT_LEFT.ordinal()
        registers.decodedOperand.getData() == ubyte(0x42)
    }

    def "should decode absolute x / y indexed mode"() {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(instr.opcode())
        registers.currentOperand.setAsShort(0x2345)

        regs x: x, y: y
        ram (
            (0x2345 + x + y), 0x13
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == cycles
        registers.decodedInstruction.getAsInt() == instr.group().ordinal()
        registers.decodedOperand.getData() == ubyte(0x13)

        where:
        instr | x    | y    | cycles
        Ox1E  | 0xBC | 0    | 1 // oops
        Ox1E  | 0x01 | 0    | 0
        Ox19  | 0    | 0xCD | 1 // oops
        Ox19  | 0    | 0x02 | 0
    }

    def "should decode immediate mode" () {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Ox09.opcode())
        registers.currentOperand.setAsShort(0x78)

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        registers.decodedOperand.getData() == ubyte(0x78)
    }

    def "should decode absolute indirect mode"() {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox6C.opcode())
        registers.currentOperand.setAsShort(operand)

        ram(
            0x0300, 0x34, // bb

            0x03FF, 0x56, // aa
            0x0400, 0x78, // cc
            0x0401, 0x90  // dd
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 2
        registers.decodedInstruction.getAsInt() == InstructionGroup.JUMP_TO_LOCATION.ordinal()
        registers.decodedOperand.getAddress() == ushort(result)

        where:
        operand | result
        0x03FF  | 0x3456 // buggy cpu
        0x0400  | 0x9078 // no page crossing
    }

    def "should decode pre indexed indirect x mode" () {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox01.opcode())
        registers.currentOperand.setAsShort(0x20)

        regs x: 0x04
        ram(
            0x0024, 0x78, // Lo
            0x0025, 0x56, // Hi
            0x5678, 0x42  // Target
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 2
        registers.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        registers.decodedOperand.getData() == ubyte(0x42)
    }

    def "should decode pre indexed indirect x mode with zero page wrap"() {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox01.opcode())
        registers.currentOperand.setAsShort(0xFF)

        regs x: 0x01
        ram(
            0x0000, 0x78, // (0xFF + 0x01) & 0xFF == 0x00
            0x0001, 0x56,
            0x5678, 0x99
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 2
        registers.decodedOperand.getData() == ubyte(0x99)
    }

    def "should decode post indexed indirect y mode" () {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox11.opcode())
        registers.currentOperand.setAsShort(address)

        regs y: 0x02
        ram(
            address        , 0x80,
            address+1      , 0x40,
            (0x4080 + 0x02), 0x77
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == cycles
        registers.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        registers.decodedOperand.getData() == ubyte(0x77)

        where:
        address | cycles
        0x00FE  | 2
        0x00FF  | 3
    }

    def "should decode zero page mode"() {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(Instruction.Ox05.opcode())
        registers.currentOperand.setAsShort(0x42)

        ram 0x0042, 0x13

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == InstructionGroup.BITWISE_OR.ordinal()
        registers.decodedOperand.getData() == ubyte(0x13)
    }

    def "should decode indexed zero page x / y mode"() {
        given:
        def cu = newControlUnit()

        registers.currentInstruction.set(instr.opcode())
        registers.currentOperand.setAsShort(0x80)

        regs x: x, y: y
        ram(
            ((0x0080 + x + y) & 0xFF), 0x55
        )

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == instr.group().ordinal()
        registers.decodedOperand.getData() == ubyte(0x55)

        where:
        instr | x    | y
        Ox16  | 0x0F | 0    // ASL $80,X
        Ox96  | 0    | 0x02 // STX $80,Y
    }

    def "should decode relative mode"() {
        given:
        def cu = newControlUnit()

        regs pc: 0x0025
        registers.currentInstruction.set(OxD0.opcode())
        registers.currentOperand.setAsShort(operand)

        bus.record()

        when:
        cu.decode()

        then:
        bus.cycles() == 0
        registers.decodedInstruction.getAsInt() == BRANCH_IF_ZERO_CLR.ordinal()
        registers.decodedOperand.getAddress() == ushort(0x0025 + operand)

        where:
        operand << [-2, 2]
    }
}
