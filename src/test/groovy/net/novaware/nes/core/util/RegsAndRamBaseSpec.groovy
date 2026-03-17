package net.novaware.nes.core.util

import net.novaware.nes.core.TestBoardFactory
import net.novaware.nes.core.config.ImmutableCoreConfig
import net.novaware.nes.core.cpu.instruction.Instruction
import net.novaware.nes.core.cpu.register.CpuInsFile
import net.novaware.nes.core.cpu.register.CpuRegFile
import net.novaware.nes.core.memory.MemoryBus
import spock.lang.Specification

import static UTypes.ubyte
import static UTypes.ushort

class RegsAndRamBaseSpec<T extends MemoryBus> extends Specification {

    def factory = TestBoardFactory.newTestBoardFactory(
        ImmutableCoreConfig.builder()
            .setCpuBusType(getCpuBusType())
            .build()
    )

    CpuRegFile registers = factory.newCpuRegisters()
    CpuInsFile insRegs = factory.newExtRegisters()

    T bus = factory.newCpuBus() as T

    MemoryBus.Type getCpuBusType() {
        return MemoryBus.Type.RECORDING
    }

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
        if (args.b != null) registers.status.setBorrow(args.b as boolean) // NOTE: NOT break
        if (args.z != null) registers.status.setZero(args.z as boolean)
        if (args.i != null) registers.status.setIrqDisabled(args.i as boolean)
        if (args.d != null) registers.status.setDecimal(args.d as boolean)
        if (args.v != null) registers.status.setOverflow(args.v as boolean)
        if (args.n != null) registers.status.setNegative(args.n as boolean)

        // Instruction registers
        if (args.do != null) insRegs.dor().configureData(ubyte(args.do as int))

        checkKeys(args.keySet())

        return true
    }

    static def checkKeys(Set keys) {
        def uncheckedKeys = new HashSet(keys)
        uncheckedKeys
                .removeAll(["pc", "a", "x", "y", "sp", "c", "b", "z", "i", "d", "v", "n", "do"])

        if (!uncheckedKeys.isEmpty()) {
            throw new AssertionError("There are unchecked keys: ${uncheckedKeys.join(", ")}")
        }
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
        if (args.b != null) assert registers.status.getBorrow() == args.b
        if (args.z != null) assert registers.status.isZero() == args.z
        if (args.i != null) assert registers.status.isIrqDisabled() == args.i
        if (args.d != null) assert registers.status.isDecimal() == args.d
        if (args.v != null) assert registers.status.isOverflow() == args.v
        if (args.n != null) assert registers.status.isNegative() == args.n

        if (args.do != null) assert insRegs.dor().getData() == ubyte(args.do as int) // TODO: what about address?

        checkKeys(args.keySet())

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
