package net.novaware.nes.core.cpu

import net.novaware.nes.core.cpu.unit.*
import spock.lang.Specification

class CpuSpec extends Specification {

    CpuRegisters registers = new CpuRegisters()

    ControlUnit controlUnit = Mock()
    AddressGen addressGen = Mock()
    ArithmeticLogic alu = Mock()
    InstructionDecoder decoder = Mock()
    InterruptLogic interrupts = Mock()
    LoadStore loadStore = Mock()
    PowerMgmt powerMgmt = Mock()
    MemoryMgmt mmu = Mock()
    StackEngine stackEngine = Mock()

    Cpu instance = new Cpu(
            registers,
            controlUnit,
            addressGen,
            alu,
            decoder,
            interrupts,
            loadStore,
            powerMgmt,
            mmu,
            stackEngine
    )

    def "should properly initialize units" () {
        when:
        instance.initialize()

        then:
        1 * controlUnit.initialize()
        1 * addressGen.initialize()
        1 * alu.initialize()
        1 * decoder.initialize()
        1 * interrupts.initialize()
        1 * loadStore.initialize()
        1 * powerMgmt.initialize()
        1 * mmu.initialize()
        1 * stackEngine.initialize()
    }

    def "should properly reset units" () {
        when:
        instance.reset()

        then:
        1 * controlUnit.reset()
        1 * addressGen.reset()
        1 * alu.reset()
        1 * decoder.reset()
        1 * interrupts.reset()
        1 * loadStore.reset()
        1 * powerMgmt.reset()
        1 * mmu.reset()
        1 * stackEngine.reset()
    }
}
