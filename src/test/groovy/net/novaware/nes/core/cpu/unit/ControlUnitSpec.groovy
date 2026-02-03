package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.memory.SystemBus
import net.novaware.nes.core.register.CycleCounter
import spock.lang.Specification

import static net.novaware.nes.core.cpu.instruction.Instruction.*
import static net.novaware.nes.core.util.Bin.s
import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class ControlUnitSpec extends Specification {

    // TODO: have separate tests for fetching, decoding and execution
    // to limit permutations of instr group * addressing mode

    CpuRegisters registers = new CpuRegisters()
    CycleCounter cpuCycleCounter = new CycleCounter("CPUCC")
    SystemBus systemBus = new SystemBus(cpuCycleCounter)
    AddressGen addressGen = new AddressGen(systemBus)

    def "should construct an instance" () {
        when:
        newControlUnit()

        then:
        ControlUnit.RESET_VECTOR == ushort(0xFFFC)
        cpuCycleCounter.getValue() == 6
    }

    ControlUnit newControlUnit() {
        def cu = new ControlUnit(
                registers,
                cpuCycleCounter,
                systemBus,
                addressGen,
                new ArithmeticLogic(registers)
        )
        cu.initialize()
        cu.powerOn()
        cu.reset()
        cu
    }

    def regs(Map args) {
        // 16-bit registers
        if (args.pc != null) registers.programCounter.set(ushort(args.pc as int))

        // 8-bit general purpose
        if (args.a != null)  registers.accumulator.set(ubyte(args.a as int))
        if (args.x != null)  registers.indexX.set(ubyte(args.x as int))
        if (args.y != null)  registers.indexY.set(ubyte(args.y as int))
        if (args.s != null)  registers.stackPointer.set(ubyte(args.s as int))

        // Status Flags (P register)
        // We can handle individual flags for better test readability
        if (args.c != null) registers.status.setCarry(args.c as boolean)
        if (args.z != null) registers.status.setZero(args.z as boolean)
        if (args.i != null) registers.status.setIrqDisabled(args.i as boolean)
        if (args.d != null) registers.status.setDecimal(args.d as boolean)
        if (args.b != null) registers.status.setB(args.b as boolean)
        if (args.v != null) registers.status.setOverflow(args.v as boolean)
        if (args.n != null) registers.status.setNegative(args.n as boolean)

        // Support setting the whole status byte at once if needed
        // if (args.p != null) registers.status.set(ubyte(args.p as int))
    }

    def ram(Object... addr_data) {
        assert addr_data.size() % 2 == 0 : "ram must be in pairs [address, value]. current size: ${addr_data.size()}"

        addr_data.collate(2).each { pair ->
            def (addr, data) = pair

            int address = switch(addr) {
                case Number -> addr.intValue()
                default     -> throw new IllegalArgumentException("Unknown addr type: ${addr.class}")
            }

            int value = switch (data) {
                case Instruction -> data.opcode() // Your enum method
                case Number      -> data.intValue()
                default          -> throw new IllegalArgumentException("Unknown data type: ${data.class}")
            }

            systemBus.specifyAnd(ushort(address)).writeByte(ubyte(value))
        }
    }

    def "should jump to start of rom" () {
        given:
        ram(
            0xFFFC, 0x00,
            0xFFFD, 0x80
        )

        when:
        def cu = newControlUnit()

        then:
        registers.programCounter.get() == ushort(0x8000)
    }

    def "should calculate bitwise or"() {
        given:
        ram(
            0x0000, Ox0D,
            0x0001, 0x54,
            0x0002, 0x06,

            0x0654, 0b1010_0110
        )

        when:
        def cu = newControlUnit()
        cpuCycleCounter.mark()

        regs a: 0b0101_1001

        cu.fetch()
        // TODO: check what was fetched
        cu.decode()
        // TODO: check what was decoded
        cu.execute()

        then:
        cpuCycleCounter.diff() == 4

        registers.accumulator.get() == ubyte(0b1111_1111)
        //                            0bNV1B_DIZC
        s(registers.status.get()) == "0b1010_0100"
    }

    def "should do nothing on NOP" () {
        given:
        def cu = newControlUnit()

        systemBus.specifyAnd(ushort(0x0000)).writeByte(ubyte(0xEA))
        registers.programCounter.setAsShort(0x0000)

        cpuCycleCounter.mark()

        when:
        cu.fetch()
        cu.decode()
        cu.execute()

        then:
        cpuCycleCounter.diff() == 2
    }

    def "should calculate bitwise and immediate"() {
        given:
        def cu = newControlUnit()

        ram(
            0x0000, Ox29,
            0x0001, 0x0F
        )

        regs pc: 0x0000, a: 0x0F

        cpuCycleCounter.mark()

        when:
        cu.fetch()
        cu.decode()
        cu.execute()

        then:
        cpuCycleCounter.diff() == 2
        registers.accumulator.get() == ubyte(0x0F)
        !registers.status.isZero()
        !registers.status.isNegative()
    }

    def "should calculate bitwise and zero page"() {
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

        cpuCycleCounter.mark()

        when:
        cu.fetch()
        cu.decode()
        cu.execute()

        then:
        cpuCycleCounter.diff() == 3
        registers.accumulator.get() == ubyte(0xF0)
        !registers.status.isZero()
        registers.status.isNegative()
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

        cpuCycleCounter.mark()

        when:
        cu.fetch()
        cu.decode()
        cu.execute()

        then:
        cpuCycleCounter.diff() == 2
        registers.accumulator.get() == ubyte(0b00101011)
        !registers.status.isZero()
        !registers.status.isNegative()
        registers.status.getCarry()
    }

    def "should rotate left zero page memory"() {
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

        cpuCycleCounter.mark()

        when:
        cu.fetch()
        cu.decode()
        cu.execute()

        then:
        cpuCycleCounter.diff() == 5
        expectRam 0x0005, 0b1110_0000
        expectRegs(
            z: false,
            n: true,
            c: true
        )
    }

    def expectRegs(Map args) {
        if (args.pc != null) assert registers.programCounter.get() == ushort(args.pc as int)
        if (args.a != null)  assert registers.accumulator.get() == ubyte(args.a as int)

        // Status flags as booleans
        if (args.z != null) assert registers.status.isZero() == args.z
        if (args.n != null) assert registers.status.isNegative() == args.n
        if (args.c != null) assert registers.status.getCarry() == args.c
        // ... repeat for v, i, d
        return true
    }

    def expectRam(Object... addrData) { // TODO: improve, maybe do a string based assertion?
        addrData.collate(2).each { pair ->
            def (addr, expected) = pair

            assert systemBus.specifyAnd(ushort(addr as int)).readByte() == ubyte(expected as int)
        }
        return true
    }
}
