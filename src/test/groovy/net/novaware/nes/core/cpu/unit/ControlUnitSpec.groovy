package net.novaware.nes.core.cpu.unit

import static net.novaware.nes.core.cpu.instruction.Instruction.*
import static net.novaware.nes.core.memory.RecordingBus.Op
import static net.novaware.nes.core.memory.RecordingBus.OpType.ACCESS
import static net.novaware.nes.core.memory.RecordingBus.OpType.READ
import static net.novaware.nes.core.util.UTypes.ushort

// useful: bus.activity().forEach { println it.toTest() }
class ControlUnitSpec extends ControlUnitBaseSpec {

    // TODO: have separate tests for fetching, decoding and execution
    // to prevent permutations of instr group * addressing mode

    def "should construct an initialized instance" () {
        when:
        newControlUnit()

        then:
        InterruptLogic.RES_VECTOR == ushort(0xFFFC)
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

    def "should branch if plus"() {
        given:
        def cu = newControlUnit()

        ram(
            sourcePc  , Ox10,  // BPL
            sourcePc+1, offset // relative +/-
        )

        regs(
            pc: sourcePc,
            n: negative
        )

        bus.record()

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        bus.cycles() == cycles
        expectRegs(
            pc: targetPc
        )

        where:
        //                           | b + t + p = base cycles + taken + page crossing
        sourcePc | negative | offset | cycles    | targetPc
        0x0000   | false    | 5      | 2 + 1 + 0 | 0x0007
        0x0000   | true     | 5      | 2 + 0 + 0 | 0x0002

        0x00FC   | false    | 3      | 2 + 1 + 1 | 0x0101
        0x00FC   | true     | 3      | 2 + 0 + 0 | 0x00FE

        0x00FE   | false    | -3     | 2 + 1 + 1 | 0x00FD
        0x00FE   | true     | -3     | 2 + 0 + 0 | 0x0100
    }

    def "should branch if any without cycle checks"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, opcode,
            0x0001, 0x05
        )

        regs(
            pc: 0x0000,
            n:  neg,
            z:  zero,
            c:  carry,
            v:  ovf
        )

        when:
        cu.fetchOpcode()
        cu.fetchOperand()
        cu.decode()
        cu.execute()

        then:
        expectRegs pc: expectedPc

        where:
        opcode | neg   | zero  | carry | ovf   | expectedPc
        Ox10   | false | false | false | false | 0x0007 // BPL taken
        Ox10   | true  | false | false | false | 0x0002 // BPL not taken
        Ox30   | true  | false | false | false | 0x0007 // BMI taken
        Ox30   | false | false | false | false | 0x0002 // BMI not taken
        OxF0   | false | true  | false | false | 0x0007 // BEQ taken
        OxF0   | false | false | false | false | 0x0002 // BEQ not taken
        OxD0   | false | false | false | false | 0x0007 // BNE taken
        OxD0   | false | true  | false | false | 0x0002 // BNE not taken
        OxB0   | false | false | true  | false | 0x0007 // BCS taken
        OxB0   | false | false | false | false | 0x0002 // BCS not taken
        Ox90   | false | false | false | false | 0x0007 // BCC taken
        Ox90   | false | false | true  | false | 0x0002 // BCC not taken
        Ox70   | false | false | false | true  | 0x0007 // BVS taken
        Ox70   | false | false | false | false | 0x0002 // BVS not taken
        Ox50   | false | false | false | false | 0x0007 // BVC taken
        Ox50   | false | false | false | true  | 0x0002 // BVC not taken
    }

    def "should transfer between registers and update flags"() {
        given:
        def cu = newControlUnit()

        regs(a: 0x25)

        when:
        cu.transfer(registers.a(), registers.x()) // TAX

        then:
        expectRegs(x: 0x25, z: false, n: false)
    }
}
