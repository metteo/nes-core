package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;

public class RecordingDevice implements MemoryDevice.ReadWrite {

    private DataBus.Line dataLine = new DataBus.OpenCircuit();

    private @Unsigned short addressLatch;
    private @Unsigned byte dataLatch;

    @Override
    public @Unsigned short getStartAddress() {
        return USHORT_0;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return USHORT_MAX_VALUE;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        this.addressLatch = address;
    }

    @Override
    public void onRead() {
        dataLatch = dataLine.data(); // listen without altering
    }

    @Override
    public void onWrite() {
        dataLatch = dataLine.data();
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new DataBus.OpenCircuit();
    }
}
