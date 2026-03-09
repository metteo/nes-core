package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.Quantity.Unit;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;

public class BankedMemory implements MemoryDevice {

    private final @Unsigned short startAddress;
    private int bankIndex;
    private int bankAddress;

    private enum Mode {
        MIRRORING, SWITCHING
    }

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

    private Quantity bankSize;
    private UByteBuffer[] visibleBanks; // refs

    private UByteBuffer[] hiddenBanks;  // data

    private Mode mode;

    public BankedMemory(
        @Unsigned short offset,
        Quantity visibleBanks, Quantity hiddenBanks
    ) {
        this.startAddress = offset;
        this.bankSize = new Quantity(1, visibleBanks.unit());

        assertArgument(visibleBanks.unit() != Unit.BYTES, "bytes not accepted as unit");
        assertArgument(hiddenBanks.unit() != Unit.BYTES, "bytes not accepted as unit");
        assertArgument(visibleBanks.unit() == hiddenBanks.unit(), "bank sizes must be the same");

        this.visibleBanks = new UByteBuffer[visibleBanks.amount()];

        this.hiddenBanks = new UByteBuffer[hiddenBanks.amount()];

        // TODO: create method to allocate instead of doing it always. preload doesn't need this
        for (int i = 0; i < this.hiddenBanks.length; i++) {
            this.hiddenBanks[i] = UByteBuffer.allocate(new Quantity(1, hiddenBanks.unit()).toBytes()); // TODO: offset would have to be dynamic in this case?
        }

        mode = Mode.MIRRORING;
        // TODO: initial configuration
        // for many to one (mirroring), set all visible banks to the hidden one
        // for 1 to 1 (direct), set up the pairs
        // for 1 to many (switching) set first to first, second to last (for program)
    }

    // TODO: expose method for copying over / setting data in the banks

    // TODO: change to a chain call pointVisible(1).toHidden(0) or similar
    public void configure(int visibleBank, int hiddenBank) {
        assertArgument(0 <= visibleBank && visibleBank < visibleBanks.length, "visible bank out of range");
        assertArgument(hiddenBank >= 0, "hidden bank out of range");

        this.visibleBanks[visibleBank] = this.hiddenBanks[hiddenBank % hiddenBanks.length];
    }

    public void preload(ByteBuffer data) {
        // TODO: assert hidden banks sum size == data.size()

        int divider = hiddenBanks.length;
        int bankSize = this.bankSize.toBytes();

        for (int i = 0; i < divider; i++) {
            hiddenBanks[i] = UByteBuffer.of(data
                    .slice(i * bankSize, bankSize)
                    .order(LITTLE_ENDIAN)
            );
        }
    }

    @Override
    public void specify(@Unsigned short address) {
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

    @Override
    public @Unsigned byte readByte() {
        return visibleBanks[bankIndex].get(bankAddress);
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        visibleBanks[bankIndex].put(bankAddress, data);
    }
}
