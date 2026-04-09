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

// visible bank size & count
// hidden bank size & count

// if visible bank size sum > hidden = mirror
// if visible bank size sum < hidden = switch

// for program: 16KB (standard), 8KB (e.g. MMC3,...), 32KB (AxROM)
// for video: 8KB (whole tileset), 4KB (bg and fg), 1-2KB (animation every frame, MMC3)

// Special cases:
// power of 2 mirroring
// fixed vs swappable banks
// mirroring modes: v, h, 1 screen, 4 screen
// write protection: throw error or ignore writes

// TODO: initial configuration
// for many to one (mirroring), set all visible banks to the hidden one
// for 1 to 1 (direct), set up the pairs
// for 1 to many (switching) set first to first, second to last (for program)

/**
 * Usage stages:
 * 1. Instantiate
 * 2. Set Visible / Hidden bank quantities
 * 3. Allocate / Preload HiddenBanks
 * 4. Configure Visible banks
 */
public class BankedMemory implements MemoryDevice.ReadWrite, Nameable {

    private final String name;
    private final @Unsigned short startAddress;
    private final Quantity bankSize;

    private int bankIndex;
    private int bankAddress;

    private UByteBuffer[] visibleBanks; // refs
    private UByteBuffer[] hiddenBanks;  // data

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

        visibleBanks = new UByteBuffer[0];
        hiddenBanks = new UByteBuffer[0];
    }

    @Override
    public String getName() {
        return name;
    }

    public BankedMemory setHiddenBanks(Quantity hiddenBanks) {
        assertArgument(hiddenBanks.unit() == bankSize.unit(), "hidden banks unit different from bank size");

        this.hiddenBanks = new UByteBuffer[hiddenBanks.amount()];
        return this;
    }

    public BankedMemory allocateHiddenBanks() {
        for (int i = 0; i < this.hiddenBanks.length; i++) {
            this.hiddenBanks[i] = UByteBuffer.allocate(bankSize.toBytes());
        }

        return this;
    }

    public void preloadHiddenBanks(ByteBuffer data) {
        // TODO: assert hidden banks sum size == data.size()

        int divider = hiddenBanks.length;
        int bankSize = this.bankSize.toBytes();

        for (int i = 0; i < divider; i++) {
            hiddenBanks[i] = UByteBuffer.of(data
                    .slice(i * bankSize, bankSize)
                    .order(LITTLE_ENDIAN));
        }
    }

    public BankedMemory setVisibleBanks(Quantity visibleBanks) {
        assertArgument(visibleBanks.unit() == bankSize.unit(), "visible banks unit different from bank size");

        this.visibleBanks = new UByteBuffer[visibleBanks.amount()];
        return this;
    }

    // TODO: change to a chain call pointVisible(1).toHidden(0) or similar
    public BankedMemory configureVisibleBank(int visibleBank, int hiddenBank) {
        assertArgument(0 <= visibleBank && visibleBank < visibleBanks.length, "visible bank out of range");
        assertArgument(hiddenBank >= 0, "hidden bank out of range");

        this.visibleBanks[visibleBank] = this.hiddenBanks[hiddenBank % hiddenBanks.length];

        return this;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return ushort(sint(startAddress) + (visibleBanks.length * bankSize.toBytes()) - 1);
    }

    @Override
    public void onAccess(@Unsigned short address) {
        int addrVal = sint(address);
        int visibleAddress = addrVal - sint(startAddress);

        // TODO: slow in hot code, change to shifting / masking
        bankIndex = visibleAddress / bankSize.toBytes();
        bankAddress = visibleAddress % bankSize.toBytes();

        // Assuming bankSize is a power of two, TODO: don't assume, assert in constructor!
        // TODO: maybe make as fields
        int shift = Integer.numberOfTrailingZeros(bankSize.toBytes());
        int mask = bankSize.toBytes() - 1;

        int bankIndex2 = visibleAddress >> shift;
        int bankAddress2 = visibleAddress & mask;

        assertArgument(bankIndex == bankIndex2, "shift produced wrong value");
        assertArgument(bankAddress == bankAddress2, "mask produced wrong value");

        visibleBanks[bankIndex].position(bankAddress);
    }

    public @Unsigned byte readByte() {
        return visibleBanks[bankIndex].get(bankAddress);
    }

    public void writeByte(@Unsigned byte data) {
        visibleBanks[bankIndex].put(bankAddress, data);
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
