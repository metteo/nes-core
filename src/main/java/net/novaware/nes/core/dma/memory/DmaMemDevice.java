package net.novaware.nes.core.dma.memory;

import dagger.Lazy;
import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.dma.Dma;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.OAM_DMA_REGISTER;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;

@BoardScope
public class DmaMemDevice implements MemoryDevice.WriteOnly, Nameable {

    @Used
    private final ByteRegister oamDma;

    @Used
    private final Lazy<Dma> dma; // top level chip so lazy injected to prevent stack overflow during construction

    private DataBus.Line dataLine = new OpenLine();

    @Inject
    public DmaMemDevice(
        @DmaVar(OAM) ByteRegister oamDma,
        Lazy<Dma> dma
    ) {
        this.oamDma = oamDma;
        this.dma = dma;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return OAM_DMA_REGISTER;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return OAM_DMA_REGISTER;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        assertArgument(sint(address) == 0x4014, "OAM DMA responds only to 0x4014"); // TODO: Refer to memory map or accept as param?
    }

    public void writeByte(@Unsigned byte data) {
        oamDma.set(data);

        dma.get().triggerDma(); // TODO: this should happen when master clock calls dma.cycle()
    }

    @Override
    public void onWrite() {
        writeByte(dataLine.data());
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
    public String toString() {
        return getName() + " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }

    @Override
    public String getName() {
        return "DMA";
    }
}
