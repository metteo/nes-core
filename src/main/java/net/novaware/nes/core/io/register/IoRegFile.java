package net.novaware.nes.core.io.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

@BoardScope
public class IoRegFile extends RegisterFile {

    // FIXME: these registers are asymmetrical, writes do different things than reads

    private ByteRegister joyStrobe = new ByteRegister("JOY_STROBE"); // W 0x4016
    private ByteRegister joy1Data  = new ByteRegister("JOY1_DATA");   // R 0x4016
    private ByteRegister joy2Data  = new ByteRegister("JOY2_DATA");   // R 0x4017

    @Inject
    protected IoRegFile() {
        super("IO_REGS");

        dataRegisters = List.of(joyStrobe, joy1Data, joy2Data);
    }

    public ByteRegister getJoy1Data() {
        return joy1Data;
    }

    public ByteRegister getJoy2Data() {
        return joy2Data;
    }
}
