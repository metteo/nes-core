package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Usage stages:
 * 1. Instantiate
 * 2. Set Physical bank quantity
 * 3. Allocate / Preload Physical Banks
 * 4. Set Virtual bank quantity
 * 5. Map Virtual banks to Physical banks
 */
public class BankedMemory implements MemoryDevice.ReadWrite, Nameable {

    private final String name;
    private final @Unsigned short startAddress;
    private final Quantity bankSize;

    private UByteBuffer[] physicalBanks;  // data
    private UByteBuffer[] virtualBanks;   // refs

    private int bankIndex;
    private int bankAddress;

    private DataBus.Line dataLine = new OpenLine();

    public BankedMemory(
        String name,
        @Unsigned short startAddress,
        Quantity bankSize
    ) {
        assertArgument(bankSize.unit() != Quantity.Unit.BYTES, "bytes not accepted as bank size unit");
        assertArgument(bankSize.amount() == 1, "non one bank size");

        this.name = name;
        this.startAddress = startAddress;
        this.bankSize = bankSize;

        virtualBanks = new UByteBuffer[0];
        physicalBanks = new UByteBuffer[0];
    }

    @Override
    public String getName() {
        return name;
    }

    public BankedMemory setPhysicalBanks(Quantity physicalBanks) {
        assertArgument(physicalBanks.unit() == bankSize.unit(), "physical banks unit different from bank size");

        this.physicalBanks = new UByteBuffer[physicalBanks.amount()];
        return this;
    }

    public Quantity getPhysicalBankCount() {
        return new Quantity(physicalBanks.length, bankSize.unit());
    }

    public BankedMemory allocatePhysicalBanks() {
        for (int i = 0; i < this.physicalBanks.length; i++) {
            this.physicalBanks[i] = UByteBuffer.allocate(bankSize.toBytes());
        }

        return this;
    }

    public void preloadPhysicalBanks(ByteBuffer data) {
        assertArgument(data.capacity() == getPhysicalBankCount().toBytes(), "mismatch between data and physical banks");

        int divider = physicalBanks.length;
        int bankSize = this.bankSize.toBytes();

        for (int i = 0; i < divider; i++) {
            physicalBanks[i] = UByteBuffer.of(data
                    .slice(i * bankSize, bankSize)
                    .order(LITTLE_ENDIAN));
        }
    }

    public BankedMemory setVirtualBanks(Quantity virtualBanks) {
        assertArgument(virtualBanks.unit() == bankSize.unit(), "virtual banks unit different from bank size");

        this.virtualBanks = new UByteBuffer[virtualBanks.amount()];
        return this;
    }

    public Quantity getVirtualBankCount() {
        return new Quantity(virtualBanks.length, bankSize.unit());
    }

    public BankedMemory mapVirtualToPhysical(int virtualBank, int physicalBank) {
        assertArgument(0 <= virtualBank && virtualBank < virtualBanks.length, "virtual bank out of range");
        assertArgument(physicalBank >= 0, "physical bank out of range");

        this.virtualBanks[virtualBank] = this.physicalBanks[physicalBank % physicalBanks.length];

        return this;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return ushort(sint(startAddress) + (virtualBanks.length * bankSize.toBytes()) - 1);
    }

    @Override
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert sint(getStartAddress()) <= sint(address) && sint(address) <= sint(getEndAddress());

        // TODO: refactor this and onAccess to share fast bank index/address resolution
        int virtualAddress = sint(address) - sint(startAddress);
        int bankIndex = virtualAddress / bankSize.toBytes();
        int bankAddress = virtualAddress % bankSize.toBytes();

        @Unsigned byte data = virtualBanks[bankIndex].get(bankAddress);
        dataLine.data(data);
    }

    @Override
    public void onAccess(@Unsigned short address) {
        int virtualAddress = sint(address) - sint(startAddress);

        // TODO: slow in hot code, change to shifting / masking
        bankIndex = virtualAddress / bankSize.toBytes();
        bankAddress = virtualAddress % bankSize.toBytes();

        // Assuming bankSize is a power of two, TODO: don't assume, assert in constructor!
        // TODO: maybe make as fields
        int shift = Integer.numberOfTrailingZeros(bankSize.toBytes());
        int mask = bankSize.toBytes() - 1;

        int bankIndex2 = virtualAddress >> shift;
        int bankAddress2 = virtualAddress & mask;

        assertArgument(bankIndex == bankIndex2, "shift produced wrong value");
        assertArgument(bankAddress == bankAddress2, "mask produced wrong value");

        virtualBanks[bankIndex].position(bankAddress);
    }

    public @Unsigned byte readByte() {
        return virtualBanks[bankIndex].get(bankAddress);
    }

    public void writeByte(@Unsigned byte data) {
        virtualBanks[bankIndex].put(bankAddress, data);
    }

    @Override
    public void onRead() {
        dataLine.data(readByte());
    }

    @Override
    public void onWrite() {             // TODO: block writes so it behaves like ROM
        writeByte(dataLine.data());
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }

    @Override
    public String toString() {
        return name + " (" + Hex.s(startAddress) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
