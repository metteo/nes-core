package net.novaware.nes.core.util

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.memory.RecordingBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class RegsAndRamBaseSpec extends Specification {

    CpuRegisters registers = new CpuRegisters()
    RecordingBus bus = new RecordingBus()

    def regs(Map args) {
        // 16-bit registers
        if (args.pc != null) registers.programCounter.set(ushort(args.pc as int))

        // 8-bit general purpose
        if (args.a != null)  registers.accumulator.set(ubyte(args.a as int))
        if (args.x != null)  registers.indexX.set(ubyte(args.x as int))
        if (args.y != null)  registers.indexY.set(ubyte(args.y as int))
        if (args.sp != null)  registers.stackPointer.set(ubyte(args.sp as int))

        // Status Flags (P register)
        // We can handle individual flags for better test readability
        if (args.c != null) registers.status.setCarry(args.c as boolean)
        if (args.z != null) registers.status.setZero(args.z as boolean)
        if (args.i != null) registers.status.setIrqDisabled(args.i as boolean)
        if (args.d != null) registers.status.setDecimal(args.d as boolean)
        if (args.v != null) registers.status.setOverflow(args.v as boolean)
        if (args.n != null) registers.status.setNegative(args.n as boolean)
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

            bus.specifyThen(ushort(address)).writeByte(ubyte(value))
        }
    }

    def expectRegs(Map args) {
        if (args.pc != null) assert registers.programCounter.get() == ushort(args.pc as int)
        if (args.a != null)  assert registers.accumulator.get() == ubyte(args.a as int)
        if (args.x != null)  assert registers.indexX.get() == ubyte(args.x as int)
        if (args.y != null)  assert registers.indexY.get() == ubyte(args.y as int)
        if (args.sp != null) assert registers.stackPointer.get() == ubyte(args.sp as int)

        // Status flags as booleans
        if (args.c != null) assert registers.status.getCarry() == args.c
        if (args.z != null) assert registers.status.isZero() == args.z
        if (args.i != null) assert registers.status.isIrqDisabled() == args.i
        if (args.d != null) assert registers.status.isDecimal() == args.d
        if (args.v != null) assert registers.status.isOverflow() == args.v
        if (args.n != null) assert registers.status.isNegative() == args.n

        return true
    }

    def expectRam(Object... addrData) { // TODO: improve, maybe do a string based assertion?
        addrData.collate(2).each { pair ->
            def (addr, expected) = pair

            assert bus.specifyThen(ushort(addr as int)).readByte() == ubyte(expected as int)
        }
        return true
    }
}
