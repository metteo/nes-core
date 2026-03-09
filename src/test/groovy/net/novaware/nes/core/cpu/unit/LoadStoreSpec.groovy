package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.TestBoardFactory
import net.novaware.nes.core.cpu.register.CpuRegFile
import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.register.DelegatingRegister
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class LoadStoreSpec extends Specification {

    def factory = TestBoardFactory.newTestBoardFactory()
    CpuRegFile regs = factory.newCpuRegisters()
    MemoryBus bus = factory.newCpuBus()
    LoadStore loadStore = factory.newLoadStore()
    DelegatingRegister decodedOperand = factory.newDecodedOperand()

    def "should load register with memory"() {
        given:
        decodedOperand.configureMemory(bus, ushort(0x1234))
        decodedOperand.setData(ubyte(data))

        when:
        loadStore.load(regs.a())

        then:
        regs.a().getAsInt() == data
        regs.status().zero == zero
        regs.status().negative == negative

        where:
        data || zero  | negative
        0xFE || false | true
        0x00 || true  | false
        0x56 || false | false
    }

    def "should store register in memory"() {
        given:
        regs.a().set(ubyte(0x56))
        decodedOperand.configureMemory(bus, ushort(0x1234))

        when:
        loadStore.store(regs.a())

        then:
        bus.specifyThen(ushort(0x1234)).readByte() == ubyte(0x56)
        // doesn't affect the flags
    }
}
