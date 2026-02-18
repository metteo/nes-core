package net.novaware.nes.core.cart.internal;

import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.mapper.Mapper;
import net.novaware.nes.core.mapper.NROM;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import net.novaware.nes.core.util.uml.Owned;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC_DUAL;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.UnsignedTypes.sint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class CartridgeImpl implements Cartridge {

    private final NesFile nesFile;

    private Mapper mapper;

    @Owned private BankedMemory programData;    // ROM
    @Owned private MemoryDevice programMemory;  // Work RAM / WRAM
    @Owned private MemoryDevice programStorage; // Save RAM / SRAM // TODO: use nes file to store save ram

    @Owned private MemoryDevice videoData;      // Video ROM / VROM
    @Owned private MemoryDevice videoMemory;    // Video RAM / VRAM
    @Owned private MemoryDevice videoStorage;   // Non-volatile Video RAM / NV-VRAM // TODO: store nvvram

    private MemoryDevice currentProgramSegment;
    private @Unsigned short currentProgramAddress;

    private MemoryDevice currentVideoSegment;
    private @Unsigned short currentVideoAddress;

    public CartridgeImpl(NesFile nesFile) {
        this.nesFile = nesFile;

        var meta = this.nesFile.meta();

        assertArgument(nesFile.meta().mapper() == 0, "only mapper 0 supported for now");
        assertArgument(meta.system() == NesMeta.System.NES, "only NES is supported");
        assertArgument(meta.videoStandard() == NTSC || meta.videoStandard() == NTSC_DUAL, "only NTSC supported");

        ByteBuffer trainer = nesFile.data().trainer(); // TODO: copy into WRAM: 0x7000 to 0x71FF
        ByteBuffer program = nesFile.data().program();

        Quantity programDataQuantity = nesFile.meta().programData();
        this.mapper = new NROM(programDataQuantity.amount());

        programData = new BankedMemory(
                ushort(0x8000),
            new Quantity(2, BANK_16KB),
            programDataQuantity
        );

        programData.preload(program);

        programData.configure(0, 0);
        programData.configure(1, programDataQuantity.amount() == 1 ? 0 : 1);

        // TODO: only if not disabled
        programMemory = new PhysicalMemory(8 * 1024, 0x6000);
        // TODO: only if battery
        programStorage = new PhysicalMemory(8 * 1024, 0x6000);

        videoData = new PhysicalMemory(UByteBuffer.of(nesFile.data().video()));
        videoMemory = new PhysicalMemory(8 * 1024, 0);
        videoStorage = new PhysicalMemory(8 * 1024, 0);

        currentProgramSegment = programData;
        currentVideoSegment = videoData;

        // TODO: adjust to mirroring mode / layout
        // TODO: adjust to mapper
        //
    }

    @Override
    public Config getConfig() {
        return new Config() {

            @Override
            public Platform getPlatform() {
                return Platform.NES_FAMICOM;
            }

            @Override
            public VideoStandard getVideoStandard() {
                return VideoStandard.NTSC;
            }

            @Override
            public Region getRegion() {
                return Region.USA;
            }
        };
    }

    @Override
    public MemoryDevice getProgram() {
        return new ProgramMemoryDevice();
    }

    @Override
    public MemoryDevice getVideo() {
        return new VideoMemoryDevice();
    }

    private class ProgramMemoryDevice implements MemoryDevice {

        @Override
        public void specify(@Unsigned short address) { // NOTE: cartridge can listen to all specify calls on address bus
            int addrVal = sint(address);

            if (0x6000 <= addrVal && addrVal <= 0x7FFF) {
                currentProgramSegment = programMemory;
                currentProgramAddress = address;
            } else if (0x8000 <= addrVal && addrVal <= 0xFFFF) {
                currentProgramSegment = programData;
                currentProgramAddress = address;
                // TODO: mirror data in case of 1x 16KB bank
            }

            currentProgramSegment.specify(currentProgramAddress);
        }

        @Override
        public @Unsigned byte readByte() {
            return currentProgramSegment.readByte();
        }

        @Override
        public void writeByte(@Unsigned byte data) {
            currentProgramSegment.writeByte(data);
        }
    }

    private class VideoMemoryDevice implements MemoryDevice {

        @Override
        public void specify(@Unsigned short address) {
            int addrVal = sint(address);

            if (0x0000 <= addrVal && addrVal <= 0x1FFF) {
                currentVideoSegment = videoData;
                currentVideoAddress = address; // TODO: translate
            }

            currentVideoSegment.specify(currentVideoAddress);
        }

        @Override
        public @Unsigned byte readByte() {
            return currentVideoSegment.readByte();
        }

        @Override
        public void writeByte(@Unsigned byte data) {
            currentVideoSegment.writeByte(data);
        }
    }
}
