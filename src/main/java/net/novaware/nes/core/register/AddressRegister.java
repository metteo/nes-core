package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

public abstract class AddressRegister extends Register {

    protected AddressRegister(String name) {
        super(name);
    }

    public abstract @Unsigned short get();
    public abstract @Unsigned byte high();
    public abstract @Unsigned byte low();

    public abstract void set(@Unsigned short address);
    public abstract AddressRegister high(@Unsigned byte hi);
    public abstract AddressRegister low(@Unsigned byte lo);

    public abstract int getAsInt();
    public abstract int highAsInt();
    public abstract int lowAsInt();

    public abstract void setAsShort(int address);
    public abstract AddressRegister highAsByte(int hi);
    public abstract AddressRegister lowAsByte(int lo);
}
