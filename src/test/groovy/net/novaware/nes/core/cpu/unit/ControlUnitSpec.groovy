package net.novaware.nes.core.cpu.unit

import static net.novaware.nes.core.cpu.instruction.Instruction.*
import static net.novaware.nes.core.memory.RecordingBus.Op
import static net.novaware.nes.core.memory.RecordingBus.OpType.ACCESS
import static net.novaware.nes.core.memory.RecordingBus.OpType.READ
import static net.novaware.nes.core.util.UnsignedTypes.ushort

// useful: bus.activity().forEach { println it.toTest() }
class ControlUnitSpec extends ControlUnitBaseSpec {

    // TODO: have separate tests for fetching, decoding and execution
    // to prevent permutations of instr group * addressing mode

    def "should construct an initialized instance" () {
        when:
        newControlUnit()

        then:
        ControlUnit.RESET_VECTOR == ushort(0xFFFC)
        bus.cycles() == 6
        expectRegs(
            sp: 0x01FD
        )
    }

    def "should reset properly"() {
        given:
        ram(
            0xFFFC, 0x00,
            0xFFFD, 0x80
        )
        def cu = newControlUnit()
        
        when:
        cu.reset()

        then:
        bus.cycles() == 6
        expectRegs(
            pc: 0x8001,
            sp: 0x01FA
        )
    }

    def "should jump to start of rom" () {
        given:
        ram(
            0xFFFC, 0x00,
            0xFFFD, 0x80
        )

        when:
        newControlUnit()

        then:
        expectRegs pc: 0x8001 // pc+1 because of priming fetchOpcode
    }

    def "should bitwise or absolute"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, Ox0D,
            0x0001, 0x54,
            0x0002, 0x06,

            0x0654, 0b1010_0110
        )

        regs pc: 0x0000, a: 0b0101_1001

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 4
        expectRegs(
            a: 0b1111_1111,
            z: false,
            n: true
        )

        bus.activity() == [
            new Op(ACCESS, 0x0000, 0x00), // opcode
            new Op(READ,   0x0000, 0x0D),

            new Op(ACCESS, 0x0001, 0x00), // absolute
            new Op(READ,   0x0001, 0x54),
            new Op(ACCESS, 0x0002, 0x00),
            new Op(READ,   0x0002, 0x06),

            new Op(ACCESS, 0x0654, 0x00), // operand
            new Op(READ,   0x0654, 0xA6)
	    ]
    }

    def "should do no operation" () {
        given:
        def cu = newControlUnit()

        ram 0x0000, OxEA
        regs pc: 0x0000

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 2
        bus.activity() == [
            new Op(ACCESS, 0x0000, 0x00), // opcode
            new Op(READ,   0x0000, 0xEA),

            new Op(ACCESS, 0x0001, 0x00), // required 2nd read
            new Op(READ,   0x0001, 0x00),
        ]
    }

    def "should bitwise and immediate"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, Ox29,
            0x0001, 0x0F
        )

        regs pc: 0x0000, a: 0x0F

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 2
        expectRegs(
            a: 0x0F,
            z: false,
            n: false
        )
    }

    def "should bitwise and zero page"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, Ox25,
            0x0001, 0x05,
            0x0005, 0xF0
        )

        regs(
            pc: 0x0000,
            a: 0xF0
        )

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 3
        expectRegs(
            a: 0xF0,
            z: false,
            n: true
        )
    }

    def "should rotate left accumulator"() {
        given:
        def cu = newControlUnit()

        ram(0x0000, Ox2A)

        regs(
            pc: 0x0000,
            a: 0b1001_0101,
            c: true
        )

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 2
        expectRegs(
            a: 0b00101011,
            z: false,
            n: false,
            c: true
        )
    }

    def "should rotate left zero page"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, Ox26,
            0x0001, 0x05,
            0x0005, 0xF0
        )

        regs(
            pc: 0x0000,
            c: false
        )

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == 5
        expectRam 0x0005, 0b1110_0000
        expectRegs(
            z: false,
            n: true,
            c: true
        )
    }
}
