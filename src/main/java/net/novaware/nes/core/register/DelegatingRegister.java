package net.novaware.nes.core.register;

import net.novaware.nes.core.cpu.memory.CpuBus;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

public class DelegatingRegister extends Register {

    private final ByteRegister nullByteRegister = new ByteRegister("NULL");

    private final EmptyDelegate emptyDelegate = new EmptyDelegate();
    private final ByteDelegate byteDelegate = new ByteDelegate();
    private final ShortDelegate shortDelegate = new ShortDelegate();
    private final ByteRegisterDelegate byteRegisterDelegate = new ByteRegisterDelegate();
    private final MemoryDelegate memoryDelegate = new MemoryDelegate();

    private @Unsigned byte data;
    private @Unsigned short address;

    private ByteRegister dataRegister = nullByteRegister;
    private @Nullable CpuBus cpuBus; // TODO: maybe replace with MMU?
    private Delegate delegate = emptyDelegate;

    public DelegatingRegister(String name) {
        super(name);
    }

    /**
     * Removes old data before configuring for new data.
     */
    private void reset() {
        data = UBYTE_0;
        address = USHORT_0;

        dataRegister = nullByteRegister;

        delegate = emptyDelegate;
    }

    public void configureEmpty() {
        reset();
    }

    public DelegatingRegister configureData(@Unsigned byte data) {
        reset();

        this.data = data;
        this.delegate = byteDelegate;
        return this;
    }

    public DelegatingRegister configureAddress(@Unsigned short address) {
        reset();

        this.address = address;
        this.delegate = shortDelegate;
        return this;
    }

    public void configureDataRegister(ByteRegister dataRegister) {
        reset();

        this.dataRegister = dataRegister;
        this.delegate = byteRegisterDelegate;
    }

    public void configureMemory(CpuBus cpuBus, @Unsigned short address) {
        reset();

        this.cpuBus = cpuBus;
        this.address = address;
        this.delegate = memoryDelegate;
    }

    public @Unsigned byte getData() {
        return delegate.getData();
    }

    public void setData(@Unsigned byte data) {
        delegate.setData(data);
    }

    public @Unsigned short getAddress() {
        return delegate.getAddress();
    }

    public void setAddress(@Unsigned short address) {
        delegate.setAddress(address);
    }

    // FIXME: virtual calls all over. Consider simplifying during performance optimization
    interface Delegate {
        @Unsigned byte getData();
        void setData(@Unsigned byte data);

        @Unsigned short getAddress();

        void setAddress(@Unsigned short address);
    }

    static class EmptyDelegate implements Delegate {

        @Override
        public @Unsigned byte getData() {
            throw new IllegalStateException("Empty delegate called");
        }

        @Override
        public void setData(@Unsigned byte data) {
            throw new IllegalStateException("Empty delegate called");
        }

        @Override
        public @Unsigned short getAddress() {
            throw new IllegalStateException("Empty delegate called");
        }

        @Override
        public void setAddress(@Unsigned short address) {
            throw new IllegalStateException("Empty delegate called");
        }
    }

    class ByteDelegate extends EmptyDelegate {

        @Override
        public @Unsigned byte getData() {
            return data;
        }

        @Override
        public void setData(@Unsigned byte d) {
            data = d;
        }
    }

    class ByteRegisterDelegate extends EmptyDelegate {

        @Override
        public @Unsigned byte getData() {
            return dataRegister.get();
        }

        @Override
        public void setData(@Unsigned byte data) {
            dataRegister.set(data);
        }
    }

    class ShortDelegate extends EmptyDelegate {

        @Override
        public @Unsigned short getAddress() {
            return address;
        }

        @Override
        public void setAddress(@Unsigned short a) {
            address = a;
        }
    }

    class MemoryDelegate extends EmptyDelegate {

        @Override
        @SuppressWarnings("argument") // @Nullable cpuBus
        public @Unsigned byte getData() {
            requireNonNull(cpuBus);

            return cpuBus.access(address).read().data();
        }

        @Override
        @SuppressWarnings("argument") // @Nullable cpuBus
        public void setData(@Unsigned byte data) {
            requireNonNull(cpuBus);

            cpuBus.access(address).write().data(data);
        }

        @Override
        public @Unsigned short getAddress() {
            return address;
        }
    }

    // TODO: implement toString delegating to delegate. Use probe instead of read to not affect bus activity
}
