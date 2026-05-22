package net.novaware.nes.core.util

import net.novaware.nes.core.memory.DataLine
import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.MemoryDevice
import org.checkerframework.checker.signedness.qual.Unsigned

import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ProbeUtil {

    static @Unsigned byte probeBus(MemoryBus bus, @Unsigned short address) {
        DataLine dataLine = new DataLine()

        bus.probe(address, dataLine)

        return dataLine.cycle()
    }

    static int probeBus(MemoryBus bus, int address) {
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
