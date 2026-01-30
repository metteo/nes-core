package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;

public class CpuRegisterFile {

    public ShortRegister programCounter = new ShortRegister("PC");

    public ShortRegister memoryAddress = new ShortRegister("MAR");
    public ShortRegister memoryData = new ShortRegister("MDR"); // FIXME: should hold longest instruction

    public ShortRegister currentInstruction = new ShortRegister("CIR"); // FIXME: should hold longest instruction
    public ShortRegister decodedInstruction = new ShortRegister("DIR"); // FIXME: should hold longest instruction

    public ByteRegister accumulator = new ByteRegister("A");
    public ByteRegister indexX = new ByteRegister("X");
    public ByteRegister indexY = new ByteRegister("Y");
    public ByteRegister statusFlags = new ByteRegister("SF");

    public ByteRegister stackPointer = new ByteRegister("SP");

    // quasi pipeline
    // IF -> ID -> EX
    //             IF -> ID -> EX

}
