package net.novaware.nes.core.io.inject;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.io.memory.JoyDataDevice;
import net.novaware.nes.core.io.memory.JoyStrobeDevice;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.port.internal.JoypadPortImpl;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.JOY;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START;

@Module
public interface IoMemModule {

    @Provides
    @BoardScope
    @Named("JOY1")
    static JoyDataDevice provideJoy1(@Named("JOY1_DATA") ByteRegister joy1Data) {
        return new JoyDataDevice(
            "JOY1",
            IO_REGISTERS_START,
            joy1Data,
            new ByteRegister("stub"),
            new ByteRegister("stub")
        );
    }

    @Provides
    @BoardScope
    @Named("JOY2")
    static JoyDataDevice provideJoy2(@Named("JOY2_DATA") ByteRegister joy2Data) {
        return new JoyDataDevice(
            "JOY2",
            IO_REGISTERS_END,
            joy2Data,
            new ByteRegister("stub"),
            new ByteRegister("stub")
        );
    }

    @Provides
    @BoardScope
    @Named("JOY_STROBE")
    static JoyStrobeDevice provideJoyStrobe(
            @Named("JOY_STROBE") BooleanRegister strobeRegister,
            JoypadPortImpl joypad1Port
    ) {
        return new JoyStrobeDevice("JOY_STROBE", IO_REGISTERS_START, strobeRegister, joypad1Port::onStrobeChange);
    }


    @Provides
    @BoardScope
    @CpuVar(JOY)
    static MemoryDevice[] provideJoyDevices(
        @Named("JOY_STROBE") JoyStrobeDevice strobe,
        @Named("JOY1") JoyDataDevice joy1,
        @Named("JOY2") JoyDataDevice joy2
    ) {
        return new MemoryDevice[] {strobe, joy1, joy2};
    }
}
