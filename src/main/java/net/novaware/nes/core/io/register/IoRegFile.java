package net.novaware.nes.core.io.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

@BoardScope
public class IoRegFile extends RegisterFile {

    // FIXME: these registers are asymmetrical, writes do different things than reads

    private ByteRegister oamDma; // 0x4014
    private ByteRegister sndChn = new ByteRegister("SNDCHN"); // TODO: use dedicate status / control register
    private ByteRegister joy1 = new ByteRegister("JOY1");
    private ByteRegister joy2 = new ByteRegister("JOY2"); // 0x4017

    @Inject
    protected IoRegFile(
        @DmaVar(OAM) ByteRegister oamDma
    ) {
        super("IO_REGS");

        this.oamDma = oamDma;

        dataRegisters = List.of(oamDma, sndChn, joy1, joy2);
    }

    public ByteRegister[] getCpuRegisters() {
        return new ByteRegister[]{ oamDma, sndChn, joy1, joy2 };
    }
}
