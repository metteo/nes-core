package net.novaware.nes.core.register;

import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.util.UnsignedTypes;
import org.checkerframework.checker.signedness.qual.Unsigned;

public class DelegatingRegister extends Register {

    private final ByteRegister nullByteRegister = new ByteRegister("NULL");
    private final MemoryBus nullMemoryBus = new EmptyMemoryBus();

    private final ByteDelegate byteDelegate = new ByteDelegate();
    private final ByteRegisterDelegate byteRegisterDelegate = new ByteRegisterDelegate();
    private final EmptyDelegate emptyDelegate = new EmptyDelegate();
    private final MemoryDelegate memoryDelegate = new MemoryDelegate();

    private @Unsigned byte data;
    private @Unsigned short address;

    private DataRegister byteRegister = nullByteRegister;
    private MemoryBus memoryBus = nullMemoryBus;
    private Delegate delegate = emptyDelegate;

    public DelegatingRegister(String name) {
        super(name);
    }

    /**
     * Removes old data before configuring for new data.
     */
    private void reset() {
        data = UnsignedTypes.UBYTE_0;
        address = UnsignedTypes.USHORT_0;

        byteRegister = nullByteRegister;
        memoryBus = nullMemoryBus;

        delegate = emptyDelegate;
    }

    public void configureEmpty() {
        reset();
    }

    public void configureByte() {
        reset();

        this.delegate = byteDelegate;
    }

    public void configureByteRegister(DataRegister byteRegister) {
        reset();

        this.delegate = byteRegisterDelegate;
        this.byteRegister = byteRegister;
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

    interface Delegate {
        @Unsigned byte getData();
        void setData(@Unsigned byte data);

        default @Unsigned short getAddress() {
            throw new UnsupportedOperationException("Not implemented");
        }

        default void setAddress(@Unsigned short address) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    class ByteDelegate implements Delegate {

        @Override
        public @Unsigned byte getData() {
            return data;
        }

        @Override
        public void setData(@Unsigned byte data) {
            DelegatingRegister.this.data = data;
        }
    }

    class ByteRegisterDelegate implements Delegate {

        @Override
        public @Unsigned byte getData() {
            return byteRegister.get();
        }

        @Override
        public void setData(@Unsigned byte data) {
            byteRegister.set(data);
        }
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
    }

    static class EmptyMemoryBus implements MemoryBus {

        @Override
        public void specify(@Unsigned short address) {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public DataBus specifyAnd(@Unsigned short address) {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public @Unsigned byte readByte() {
            throw new IllegalStateException("Empty memory bus called");
        }

        @Override
        public void writeByte(@Unsigned byte data) {
            throw new IllegalStateException("Empty memory bus called");
        }
    }

    class MemoryDelegate implements Delegate {

        @Override
        public @Unsigned byte getData() {
            return memoryBus.specifyAnd(address).readByte();
        }

        @Override
        public void setData(@Unsigned byte data) {
            memoryBus.specifyAnd(address).writeByte(data);
        }

        @Override
        public @Unsigned short getAddress() {
            return address;
        }
    }
}
