package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.RecordingBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class LoadStoreSpec extends Specification {

    CpuRegisters regs = new CpuRegisters()
    MemoryBus bus = new RecordingBus()
    LoadStore loadStore = new LoadStore(regs)

    def "should load register with memory"() {
        given:
        regs.dor().configureMemory(bus, ushort(0x1234))
        regs.dor().setData(ubyte(data))

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
        regs.dor().configureMemory(bus, ushort(0x1234))

        when:
        loadStore.store(regs.a())

        then:
        bus.specifyThen(ushort(0x1234)).readByte() == ubyte(0x56)
        // doesn't affect the flags
    }
}
