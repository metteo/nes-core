package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.apu.inject.ApuModule;
import net.novaware.nes.core.clock.ClockModule;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.ImmutableCoreConfig;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.inject.CpuMemModule;
import net.novaware.nes.core.cpu.inject.CpuModule;
import net.novaware.nes.core.cpu.inject.CpuRegModule;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuInsFile;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.cpu.unit.ControlFlow;
import net.novaware.nes.core.cpu.unit.ControlUnit;
import net.novaware.nes.core.cpu.unit.InterruptLogic;
import net.novaware.nes.core.cpu.unit.LoadStore;
import net.novaware.nes.core.cpu.unit.MemoryMgmt;
import net.novaware.nes.core.cpu.unit.StackEngine;
import net.novaware.nes.core.dma.inject.DmaModule;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.RecordingDevice;
import net.novaware.nes.core.port.internal.PortModule;
import net.novaware.nes.core.ppu.Ppu;
import net.novaware.nes.core.ppu.inject.PpuModule;
import net.novaware.nes.core.register.DelegatingRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;

@BoardScope
@Component(modules = {
    DmaModule.class,
    ApuModule.class, // TODO: do we want it here? RecordingBus doesn't need it
    PpuModule.class,  // TODO: do we want it here? RecordingBus doesn't need it
    CpuModule.class,
    CpuRegModule.class,
    CpuMemModule.class,
    ClockModule.class,
    PortModule.class
})
public abstract class TestBoardFactory { // TODO: consider TestSubjectFactory name

    public static TestBoardFactory newTestBoardFactory() {
        return newTestBoardFactory(ImmutableCoreConfig.builder()
                .setRecordCpuBus(true)
                .build()
        );
    }

    public static TestBoardFactory newTestBoardFactory(CoreConfig config) {
        return DaggerTestBoardFactory.builder()
                .coreConfig(config)
                .build();
    }

    public abstract CpuRegFile newCpuRegisters();

    public abstract LoadStore newLoadStore();

    @CpuVar(BUS)
    public abstract MemoryBus newCpuBus();

    @CpuVar(DO)
    public abstract DelegatingRegister newDecodedOperand();

    public abstract MemoryMgmt newMemoryMgmt();
    public abstract StackEngine newStackEngine();

    public abstract ControlUnit newControlUnit();

    public abstract CpuInsFile newExtRegisters();

    public abstract InterruptLogic newInterruptLogic();

    public abstract ArithmeticLogic newArithmeticLogic();

    public abstract ControlFlow newControlFlow();

    public abstract Cpu newCpu();

    public abstract Ppu newPpu();

    public abstract RecordingDevice newRecordingDevice();

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder coreConfig(CoreConfig config);

        public abstract TestBoardFactory build();
    }
}
