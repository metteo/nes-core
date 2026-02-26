package net.novaware.nes.core.port.internal;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.port.DebugPort;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

@BoardScope
public class DebugPortImpl implements DebugPort {

    private final CpuRegisters registers;

    private @Nullable Receiver receiver;

    @Inject
    public DebugPortImpl(
        CpuRegisters registers
    ) {
        this.registers = registers;
    }

    @Override
    public void connect(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void disconnect() {
        this.receiver = null;
    }

    @Override
    public void setProgramCounter(@Unsigned short address) {
        registers.getProgramCounter().set(address);
    }

    public void onException(Exception exception) {
        if (receiver != null) {
            receiver.onException(exception);
        }
    }
}
