package net.novaware.nes.core.io.register;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

@BoardScope
public class IoRegFile extends RegisterFile {

    private final BooleanRegister joyStrobe;
    private final ByteRegister joy1Data;
    private final ByteRegister joy2Data;

    @Inject
    protected IoRegFile(
        @Named("JOY_STROBE") BooleanRegister joyStrobe,
        @Named("JOY1_DATA") ByteRegister joy1Data,
        @Named("JOY2_DATA") ByteRegister joy2Data
    ) {
        super("IO_REGS");

        booleanRegisters = List.of(
            this.joyStrobe = joyStrobe
        );
        dataRegisters = List.of(
            this.joy1Data = joy1Data,
            this.joy2Data = joy2Data
        );
    }
}
