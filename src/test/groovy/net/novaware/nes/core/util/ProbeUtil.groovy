package net.novaware.nes.core.util

import net.novaware.nes.core.cpu.memory.CpuBus
import net.novaware.nes.core.memory.DataLine
import net.novaware.nes.core.memory.MemoryDevice
import net.novaware.nes.core.ppu.memory.PpuBus
import net.novaware.nes.core.test.TestBus
import org.checkerframework.checker.signedness.qual.Unsigned

import static net.novaware.nes.core.util.UTypes.*

class ProbeUtil {

    static @Unsigned byte probeBus(Object bus, @Unsigned short address) {
        DataLine dataLine = new DataLine()

        switch(bus) {
            case CpuBus: bus.probe(address, dataLine); break
            case PpuBus: bus.probe(address, dataLine); break
            case TestBus: bus.probe(address, dataLine); break
        }

        return dataLine.cycle()
    }

    static int probeBus(Object bus, int address) {
        return ubyte(probeBus(bus, ushort(address)))
    }

    static @Unsigned byte probeDevice(MemoryDevice device, @Unsigned short address) {
        DataLine dataLine = new DataLine()

        device.probe(address, dataLine)

        return dataLine.cycle()
    }

    static int probeDevice(MemoryDevice device, int address) {
        return sint(probeDevice(device, ushort(address)))
    }
}
