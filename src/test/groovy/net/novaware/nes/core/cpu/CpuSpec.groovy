package net.novaware.nes.core.cpu

import net.novaware.nes.core.cpu.register.CpuRegFile
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector
import net.novaware.nes.core.cpu.signal.internal.LevelDetector
import net.novaware.nes.core.cpu.unit.*
import spock.lang.Specification

import static net.novaware.nes.core.cpu.signal.Signal.HIGH
import static net.novaware.nes.core.cpu.signal.Signal.LOW

class CpuSpec extends Specification {

    CpuRegFile registers = null

    ControlUnit controlUnit = Mock()
    AddressGen addressGen = Mock()
    ArithmeticLogic alu = Mock()
    InstructionDecoder decoder = Mock()
    InterruptLogic interrupts = Mock()
    LoadStore loadStore = Mock()
    MemoryMgmt mmu = Mock()
    PowerMgmt powerMgmt = Mock()
    PrefetchUnit prefetch = Mock()
    StackEngine stackEngine = Mock()
    DiagnosticUnit diagnostics = Mock()
    LevelDetector irq = Mock()
    EdgeDetector nmi = Mock()
    LevelDetector res = Mock()
    LevelDetector s0h = Mock()
    LevelDetector rdy = Mock()
    EdgeDetector so = Mock()

    Cpu instance = new Cpu(
        registers,
        controlUnit,
        addressGen,
        alu,
        decoder,
        interrupts,
        loadStore,
        mmu,
        powerMgmt,
        prefetch,
        stackEngine,
        diagnostics,
        irq, nmi, s0h, res, rdy, so
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
        1 * mmu.initialize()
        1 * powerMgmt.initialize()
        1 * prefetch.initialize()
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
        1 * mmu.reset()
        1 * powerMgmt.reset()
        1 * prefetch.reset()
        1 * stackEngine.reset()
    }

    def "should properly react to irq signal"() {
        when:
        instance.interruptRequest(signal)

        then:
        1 * irq.set(signal)

        where:
        signal << [LOW, HIGH]
    }

    def "should properly react to nmi signal"() {
        when:
        instance.nonMaskableInterrupt(signal)

        then:
        1 * nmi.set(signal)

        where:
        signal << [LOW, HIGH]
    }

    def "should properly react to s0h signal"() {
        when:
        instance.s0h(signal)

        then:
        1 * s0h.set(signal)

        where:
        signal << [LOW, HIGH]
    }

    def "should properly react to res signal"() {
        when:
        instance.reset(signal)

        then:
        1 * res.set(signal)

        where:
        signal << [LOW, HIGH]
    }

    def "should properly react to rdy signal"() {
        when:
        instance.rdy(signal)

        then:
        1 * rdy.set(signal)

        where:
        signal << [LOW, HIGH]
    }

    def "should properly react to so signal"() {
        when:
        instance.so(signal)

        then:
        1 * so.set(signal)

        where:
        signal << [LOW, HIGH]
    }
}
