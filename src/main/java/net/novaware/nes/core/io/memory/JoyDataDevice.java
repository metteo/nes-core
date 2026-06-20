package net.novaware.nes.core.io.memory;

import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.Bin;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Masks.BIT_7;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class JoyDataDevice implements MemoryDevice.ReadOnly, Nameable {

    private final String name;

    private final @Unsigned short address;

    private final ByteRegister primary;
    private final ByteRegister expansion;
    private final ByteRegister microphone;

    private DataBus.Line dataLine = new OpenLine();

    public JoyDataDevice(
        String name,
        @Unsigned short address,
        ByteRegister primary,
        ByteRegister expansion,
        ByteRegister microphone
    ) {
        this.name = name;
        this.address = address;
        this.primary = primary;
        this.expansion = expansion;
        this.microphone = microphone;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return address;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return address;
    }

    @Override
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert this.address == address : "only direct access is supported";

        dataLine.data(doRead());
    }

    @Override
    public void onAccess(@Unsigned short address) {
        assert this.address == address : "only direct access is supported";
    }

    private @Unsigned byte doRead() {
        int primaryByte = primary.getAsInt();
        int result = (BIT_7 & primaryByte) >> 7;

        return ubyte(result);
    }

    private void shiftPrimary() {
        // TODO: consider making a dedicated register class
        int primaryShifted = (primary.getAsInt() << 1) | 0b1; // TODO: make configurable per input device?
        primary.setAsByte(primaryShifted);
    }

    @Override
    public void onRead() {
        byte result = doRead();

        shiftPrimary();

        dataLine.data(result);
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (0x" + Hex.s(address) + "): " + Bin.s(doRead());
    }
}
