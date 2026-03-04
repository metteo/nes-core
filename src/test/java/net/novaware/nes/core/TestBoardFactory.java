package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.clock.ClockModule;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.ImmutableCoreConfig;
import net.novaware.nes.core.cpu.inject.CpuModule;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.MemoryModule;
import net.novaware.nes.core.cpu.inject.RegisterModule;
import net.novaware.nes.core.cpu.register.CpuInsFile;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.cpu.unit.ControlFlow;
import net.novaware.nes.core.cpu.unit.ControlUnit;
import net.novaware.nes.core.cpu.unit.InterruptLogic;
import net.novaware.nes.core.cpu.unit.LoadStore;
import net.novaware.nes.core.cpu.unit.MemoryMgmt;
import net.novaware.nes.core.cpu.unit.StackEngine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.port.internal.PortModule;
import net.novaware.nes.core.register.DelegatingRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;

@BoardScope
@Component(modules = {
    CpuModule.class,
    RegisterModule.class,
    MemoryModule.class,
    ClockModule.class,
    PortModule.class
    // TODO: allow changing between RecordingBus and SystemBus
})
public abstract class TestBoardFactory {

    public static TestBoardFactory newTestBoardFactory() {
        return newTestBoardFactory(ImmutableCoreConfig.builder()
                .setCpuBusType(MemoryBus.Type.RECORDING)
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

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder coreConfig(CoreConfig config);

        public abstract TestBoardFactory build();
    }
}
