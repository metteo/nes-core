package net.novaware.nes.core.cpu.unit


import net.novaware.nes.core.cpu.instruction.InstructionGroup

import static net.novaware.nes.core.cpu.instruction.Instruction.*
import static net.novaware.nes.core.cpu.instruction.InstructionGroup.BRANCH_IF_ZERO_CLR
import static net.novaware.nes.core.cpu.instruction.InstructionGroup.PUSH_STATUS_TO_SP
import static net.novaware.nes.core.memory.BusOp.*
import static net.novaware.nes.core.memory.RecordingDevice.Op
import static net.novaware.nes.core.util.UTypes.*

class ControlUnitAddressingSpec extends ControlUnitBaseSpec {

    def "should decode implied mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox08.opcode())
        insRegs.currentOperand.set(USHORT_0)

        rec.record()

        when:
        cu.decode()

        def cycles = rec.cycles()
        def group = insRegs.decodedInstruction.getGroup()

        insRegs.decodedOperand.getData()

        then:
        cycles == 0
        group == PUSH_STATUS_TO_SP

        def e = thrown(IllegalStateException)
        e.message == "Empty delegate called"
    }

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
        insRegs.decodedInstruction.getGroup() == InstructionGroup.SHIFT_LEFT
        insRegs.decodedOperand.getData() == ubyte(0x28)
    }

    def "should decode immediate mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox09.opcode())
        insRegs.currentOperand.setAsShort(0x78)

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getGroup() == InstructionGroup.BITWISE_OR
        insRegs.decodedOperand.getData() == ubyte(0x78)
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
        insRegs.decodedInstruction.getGroup() == BRANCH_IF_ZERO_CLR
        insRegs.decodedOperand.getAddress() == ushort(0x0025 + operand)

        where:
        operand << [-2, 2]
    }

    def "should decode zero page mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox05.opcode())
        insRegs.currentOperand.setAsShort(0x42)

        ram 0x0042, 0x13

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 0
        insRegs.decodedInstruction.getGroup() == InstructionGroup.BITWISE_OR
        insRegs.decodedOperand.getData() == ubyte(0x13)
    }

    def "should decode zero page x / y mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(instr.opcode())
        insRegs.currentOperand.setAsShort(base)

        regs x: x, y: y
        ram(
            base, 0x44,
            ((base + x + y) & 0xFF), 0x55
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 1
        rec.activity() == [
            // memory read without adding index register
            new Op(ADDRESS_ACCESS, base, 0x00),
            new Op(CONTROL_READ,   base, 0x00),
            new Op(DATA_READ,      base, 0x44),
        ]

        insRegs.decodedInstruction.getGroup() == instr.group()
        insRegs.decodedOperand.getData() == ubyte(0x55)

        where:
        instr | base   | x    | y
        Ox16  | 0x0080 | 0x0F | 0    // ASL $80,X
        Ox96  | 0x0080 | 0    | 0x02 // STX $80,Y
        Ox16  | 0x00FF | 0x03 | 0    // ASL $FF,X stay within zero page
        Ox96  | 0x00FF | 0    | 0x04 // STX $FF,Y stay within zero page
    }

    def "should decode zero page x indirect mode" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox01.opcode())
        insRegs.currentOperand.setAsShort(0x20)

        regs x: 0x04
        ram(
            0x0020, 0x34,
            0x0024, 0x12, // Lo
            0x0025, 0x60, // Hi
            0x6012, 0x42  // Target
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 3
        rec.activity() == [
            new Op(ADDRESS_ACCESS, 0x0020, 0x00), // sum cycle
            new Op(CONTROL_READ,   0x0020, 0x00),
            new Op(DATA_READ,      0x0020, 0x34),
            new Op(ADDRESS_ACCESS, 0x0024, 0x00), // low byte
            new Op(CONTROL_READ,   0x0024, 0x00),
            new Op(DATA_READ,      0x0024, 0x12),
            new Op(ADDRESS_ACCESS, 0x0025, 0x00), // hi byte
            new Op(CONTROL_READ,   0x0025, 0x00),
            new Op(DATA_READ,      0x0025, 0x60)
        ]
        insRegs.decodedInstruction.getGroup() == InstructionGroup.BITWISE_OR
        insRegs.decodedOperand.getData() == ubyte(0x42)
    }

    def "should decode zero page x indirect mode with zero page wrap"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox01.opcode())
        insRegs.currentOperand.setAsShort(base)

        def basex = (base + x) & 0xFF
        def basex1 = (base + x + 1) & 0xFF

        regs x: x
        ram(
            basex , 0x12, // lo
            basex1, 0x60, // hi
            0x6012, 0x99, // target
            base,   0x44  // sum cycle
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == 3
        rec.activity() == [
            new Op(ADDRESS_ACCESS, base, 0x00), // sum cycle
            new Op(CONTROL_READ,   base, 0x00),
            new Op(DATA_READ,      base, 0x44),
            new Op(ADDRESS_ACCESS, basex, 0x00), // lo byte
            new Op(CONTROL_READ,   basex, 0x00),
            new Op(DATA_READ,      basex, 0x12),
            new Op(ADDRESS_ACCESS, basex1, 0x00), // hi byte
            new Op(CONTROL_READ,   basex1, 0x00),
            new Op(DATA_READ,      basex1, 0x60)
        ]
        insRegs.decodedOperand.getData() == ubyte(0x99)

        where:
        base   | x    | comment
        0x00FE | 0x01 | "zero page wrap within address"
        0x00FF | 0x02 | "zero page wrap on sum"
    }

    def "should decode zero page y indirect mode - read" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox11.opcode())
        insRegs.currentOperand.setAsShort(address)

        regs y: y
        ram(
            address,         0xFD,
            (address+1)%256, 0x60,
            (0x60FD + y),    0x77
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == cycles // TODO: figure out how to assert the cycles
        insRegs.decodedInstruction.getGroup() == InstructionGroup.BITWISE_OR
        insRegs.decodedOperand.getData() == ubyte(0x77)

        where:
        address | y | cycles | comment
        0x00FE  | 1 | 2      | "no wrap"
        0x00FF  | 2 | 2      | "zero page wrap"
        0x00AB  | 3 | 3      | "mem page wrap"
    }

    def "should decode zero page y indirect mode - write" () {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox91.opcode())
        insRegs.currentOperand.setAsShort(address)

        regs y: y
        ram(
            address,         0xFD,
            (address+1)%256, 0x60,
            (0x60FD + y),    0x77
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == cycles
        insRegs.decodedInstruction.getGroup() == InstructionGroup.STORE_A_IN_MEMORY
        insRegs.decodedOperand.getData() == ubyte(0x77)

        where:
        address | y | cycles | comment
        0x00FE  | 1 | 3      | "no wrap"
        0x00FF  | 2 | 3      | "zero page wrap"
        0x00AB  | 3 | 3      | "mem page wrap"
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
        insRegs.decodedInstruction.getGroup() == InstructionGroup.SHIFT_LEFT
        insRegs.decodedOperand.getData() == ubyte(0x42)
    }

    def "should decode absolute x / y mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(instr.opcode())
        insRegs.currentOperand.setAsShort(0x1245)

        regs x: x, y: y
        ram (
            (0x1245 + x + y), 0x13
        )

        rec.record()

        when:
        cu.decode()

        then:
        rec.cycles() == cycles
        insRegs.decodedInstruction.getGroup() == instr.group()
        insRegs.decodedOperand.getData() == ubyte(0x13)

        where:
        instr | x    | y    | cycles
        Ox1D  | 0xBC | 0    | 1 // x read  / page cross oops
        Ox1D  | 0x01 | 0    | 0 // x read  / no oops
        Ox1E  | 0xBC | 0    | 1 // x write / write oops
        Ox1E  | 0x01 | 0    | 1 // x write / write oops
        Ox19  | 0    | 0xCD | 1 // y read  / page cross oops
        Ox19  | 0    | 0x02 | 0 // y read  / no oops
        Ox99  | 0    | 0xBC | 1 // y write / write oops
        Ox99  | 0    | 0x01 | 1 // y write / write oops
    }

    def "should decode absolute indirect mode"() {
        given:
        def cu = newControlUnit()

        insRegs.currentInstruction.set(Ox6C.opcode())
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
        insRegs.decodedInstruction.getGroup() == InstructionGroup.JUMP_TO_LOCATION
        insRegs.decodedOperand.getAddress() == ushort(result)

        where:
        operand | result
        0x03FF  | 0x3456 // buggy cpu
        0x0400  | 0x9078 // no page crossing
    }
}
