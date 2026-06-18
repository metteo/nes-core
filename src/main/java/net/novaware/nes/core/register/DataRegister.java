package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

// TODO: consider removing Data / Address Register abstract classes to improve devirtualization
public abstract class DataRegister extends Register {

    protected DataRegister(String name) {
        super(name);
    }

    public abstract @Unsigned byte get();
    public abstract void set(@Unsigned byte data);

    public abstract int getAsInt();
    public abstract void setAsByte(int data);
}
