package net.novaware.nes.core.apu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_STATUS_REGISTER;

@BoardScope
public class ApuMemDevice implements MemoryDevice.ReadWrite, Nameable {

    // NOTE: we need 3 devices: Channel registers, Status register and Frame Counter

    @Inject
    public ApuMemDevice() {

    }

    @Override
    public void onRead() {

    }

    @Override
    public void onWrite() {

    }

    @Override
    public void onAttach(DataBus.Line dataLine) {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public @Unsigned short getStartAddress() {
        return APU_STATUS_REGISTER;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return APU_STATUS_REGISTER;
    }

    @Override
    public void onAccess(@Unsigned short address) {

    }

    @Override
    public String getName() {
        return "APU_STATUS";
    }

    @Override
    public String toString() {
        return getName() + " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
