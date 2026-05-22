package net.novaware.nes.core.register;

import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.memory.ControlBus;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

public class DelegatingRegister extends Register {

    private final ByteRegister nullByteRegister = new ByteRegister("NULL");
    private final MemoryBus nullMemoryBus = new EmptyMemoryBus();

    private final EmptyDelegate emptyDelegate = new EmptyDelegate();
    private final ByteDelegate byteDelegate = new ByteDelegate();
    private final ShortDelegate shortDelegate = new ShortDelegate();
    private final ByteRegisterDelegate byteRegisterDelegate = new ByteRegisterDelegate();
    private final MemoryDelegate memoryDelegate = new MemoryDelegate();

    private @Unsigned byte data;
    private @Unsigned short address;

    private DataRegister dataRegister = nullByteRegister;
    private MemoryBus memoryBus = nullMemoryBus; // TODO: maybe replace with MMU?
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
        memoryBus = nullMemoryBus;

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

    public void configureDataRegister(DataRegister dataRegister) {
        reset();

        this.dataRegister = dataRegister;
        this.delegate = byteRegisterDelegate;
    }

    public void configureMemory(MemoryBus memoryBus, @Unsigned short address) {
        reset();

        this.memoryBus = memoryBus;
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

    static class EmptyMemoryBus implements MemoryBus {

        @Override
        public BusOp currentOp() {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public void probe(@Unsigned short address, DataBus.Line dataLine) {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public ControlBus.Line access(@Unsigned short address) {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public DataBus.Read read() {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public DataBus.Write write() {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public @Unsigned byte data() {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public void data(@Unsigned byte data) {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public void attachCartridge(MemoryDevice.ReadWrite cartridge) {
            throw new UnsupportedOperationException("not implemented!");
        }

        @Override
        public void detachCartridge() {
            throw new UnsupportedOperationException("not implemented!");
        }

        @Override
        public void attachExpansion(MemoryDevice.ReadWrite expansion) {
            throw new UnsupportedOperationException("not implemented!");
        }

        @Override
        public void detachExpansion() {
            throw new UnsupportedOperationException("not implemented!");
        }
    }

    class MemoryDelegate extends EmptyDelegate {

        @Override
        public @Unsigned byte getData() {
            return memoryBus.access(address).read().data();
        }

        @Override
        public void setData(@Unsigned byte data) {
            memoryBus.access(address).write().data(data);
        }

        @Override
        public @Unsigned short getAddress() {
            return address;
        }
    }

    // TODO: implement toString delegating to delegate. Use probe instead of read to not affect bus activity
}
