package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.CpuVarName;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

import java.util.List;

/**
 * CPU Memory Registers
 */
public class CpuMemFile extends RegisterFile {

    private final ShortRegister memoryAddress;
    private final ByteRegister memoryData;

    public CpuMemFile(
        @CpuVar(CpuVarName.MA) ShortRegister memoryAddress,
        @CpuVar(CpuVarName.MD) ByteRegister memoryData
    ) {
        super("CPU.MEMS");

        dataRegisters = List.of(
            this.memoryData = memoryData


        );

        addressRegisters = List.of(
            this.memoryAddress = memoryAddress
        );
    }

    public AddressRegister getMemoryAddress() {
        return memoryAddress;
    }

    /** @see #getMemoryAddress() */
    public AddressRegister mar() {
        return memoryAddress;
    }

    public DataRegister getMemoryData() {
        return memoryData;
    }

    /** @see #getMemoryData() */
    public DataRegister mdr() {
        return memoryData;
    }
}
