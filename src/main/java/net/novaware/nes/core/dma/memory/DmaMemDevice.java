package net.novaware.nes.core.dma.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.dma.Dma;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;

public class DmaMemDevice implements MemoryDevice {

    @Used
    private final ByteRegister oamDma;

    @Used
    private final Dma dma;

    @Inject
    public DmaMemDevice(
        @DmaVar(OAM) ByteRegister oamDma,
        Dma dma
    ) {
        this.oamDma = oamDma;
        this.dma = dma;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return 0;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return 0;
    }

    @Override
    public void specify(@Unsigned short address) {
        assertArgument(sint(address) == 0x4014, "OAM DMA responds only to 0x4014"); // TODO: Refer to memory map or accept as param?
    }

    @Override
    public @Unsigned byte readByte() {
        return 0; // TODO: open bus
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        oamDma.set(data);

        dma.triggerDma();
    }
}
