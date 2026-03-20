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
import net.novaware.nes.core.memory.DataBus;
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
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class CartridgeImpl implements Cartridge {

    private final NesFile nesFile;

    private Mapper mapper;

    @Owned private BankedMemory programData;    // ROM
    @Owned private MemoryDevice.ReadWrite programMemory;  // Work RAM / WRAM
    @Owned private MemoryDevice.ReadWrite programStorage; // Save RAM / SRAM // TODO: use nes file to store save ram

    @Owned private MemoryDevice.ReadWrite videoData;      // Video ROM / VROM
    @Owned private MemoryDevice.ReadWrite videoMemory;    // Video RAM / VRAM
    @Owned private MemoryDevice.ReadWrite videoStorage;   // Non-volatile Video RAM / NV-VRAM // TODO: store nvvram

    private final MemoryDevice.ReadWrite emptyDevice = new MemoryDevice.Empty();

    private MemoryDevice.ReadWrite currentProgramSegment;
    private @Unsigned short currentProgramAddress;

    private MemoryDevice.ReadWrite currentVideoSegment;
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
                "PRG-ROM",
                ushort(0x8000),
                ushort(0xFFFF),
            new Quantity(2, BANK_16KB),
            programDataQuantity
        );

        programData.preload(program);

        programData.configure(0, 0);
        programData.configure(1, programDataQuantity.amount() == 1 ? 0 : 1);

        // TODO: only if not disabled
        programMemory = new PhysicalMemory("WRAM", ushort(0x6000), ushort(0x7FFF), 8 * 1024);
        // TODO: only if battery
        programStorage = new PhysicalMemory("SRAM", ushort(0x6000), ushort(0x7FFF), 8 * 1024);

        videoData = new PhysicalMemory("CHR-ROM", ushort(0x2000), ushort(0x2FFF), UByteBuffer.of(nesFile.data().video()));
        videoMemory = new PhysicalMemory("VRAM", ushort(0x2000), ushort(0x2FFF), 8 * 1024);
        videoStorage = new PhysicalMemory("NVVRAM", ushort(0x2000), ushort(0x2FFF), 8 * 1024);

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
    public MemoryDevice.ReadWrite getCpuBusDevice() {
        return new CpuBusDevice();
    }

    @Override
    public MemoryDevice.ReadWrite getPpuBusDevice() {
        return new PpuBusDevice();
    }

    private class CpuBusDevice implements MemoryDevice.ReadWrite {

        @Override
        public void onAccess(@Unsigned short address) {
            int addrVal = sint(address);

            currentProgramAddress = address;

            if (0x6000 <= addrVal && addrVal <= 0x7FFF) {
                currentProgramSegment = programMemory;
            } else if (0x8000 <= addrVal && addrVal <= 0xFFFF) {
                currentProgramSegment = programData;
                // TODO: mirror data in case of 1x 16KB bank
            } else {
                currentProgramSegment = emptyDevice;
            }

            currentProgramSegment.onAccess(currentProgramAddress);
        }

        @Override
        public @Unsigned short getStartAddress() {
            return ushort(0x6000);
        }

        @Override
        public @Unsigned short getEndAddress() {
            return ushort(0xFFFF);
        }

        @Override
        public void onRead() {
            currentProgramSegment.onRead();
        }

        @Override
        public void onWrite() {
            currentProgramSegment.onWrite();
        }

        @Override
        public void onAttach(DataBus.Line dataLine) {
            programData.onAttach(dataLine);
            programMemory.onAttach(dataLine);
            programStorage.onAttach(dataLine);
        }

        @Override
        public void onDetach() {
            programData.onDetach();
            programMemory.onDetach();
            programStorage.onDetach();
        }
    }

    private class PpuBusDevice implements MemoryDevice.ReadWrite {

        @Override
        public void onAccess(@Unsigned short address) {
            int addrVal = sint(address);

            currentVideoAddress = address; // TODO: translate

            if (0x0000 <= addrVal && addrVal <= 0x1FFF) {
                currentVideoSegment = videoData;
            } else {
                currentVideoSegment = emptyDevice;
            }

            currentVideoSegment.onAccess(currentVideoAddress);
        }

        @Override
        public @Unsigned short getStartAddress() {
            return ushort(0x0000);
        }

        @Override
        public @Unsigned short getEndAddress() {
            return ushort(0x1FFF);
        }

        @Override
        public void onRead() {
            currentVideoSegment.onRead();
        }

        @Override
        public void onWrite() {
            currentVideoSegment.onWrite();
        }

        @Override
        public void onAttach(DataBus.Line dataLine) {
            videoData.onAttach(dataLine);
            videoMemory.onAttach(dataLine);
            videoStorage.onAttach(dataLine);
        }

        @Override
        public void onDetach() {
            videoData.onDetach();
            videoMemory.onDetach();
            videoStorage.onDetach();
        }
    }
}
