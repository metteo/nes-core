package net.novaware.nes.core.cart.internal;

import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.mapper.Mapper;
import net.novaware.nes.core.mapper.NROM;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PagedMemory;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.memory.ReservedMemory;
import net.novaware.nes.core.ppu.memory.PpuMemMap;
import net.novaware.nes.core.util.Quantity;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC_DUAL;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_MIRROR_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.UNUSED_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.UNUSED_START;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Asserts.assertNonNull;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_1KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;
import static net.novaware.nes.core.util.UTypes.ushort;

// TODO: use nes file to store save ram and nvvram

public class CartridgeImpl implements Cartridge {

    private final NesFile nesFile;

    // TODO: mapper is a MemoryDevice listening on some / all address ranges and reconfiguring banks, etc?
    private Mapper mapper;

    private final MemoryDevice.ReadWrite emptyDevice = new MemoryDevice.Empty();

    private final PagedMemory cpuBusDevice = new PagedMemory("CART.CPU", CpuMemMap.MEMORY_SIZE, emptyDevice);
    private final PagedMemory ppuBusDevice = new PagedMemory("CART.PPU", PpuMemMap.MEMORY_SIZE, emptyDevice);

    private @Nullable BankedMemory ppuVideoMemory;

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

        BankedMemory programData = new BankedMemory("PRG-ROM", ushort(0x8000), new Quantity(1, BANK_16KB))
            .setVirtualBanks(new Quantity(2, BANK_16KB))
            .setPhysicalBanks(programDataQuantity);

        programData.preloadPhysicalBanks(program);

        programData.mapVirtualToPhysical(0, 0);
        programData.mapVirtualToPhysical(1, programDataQuantity.amount() == 1 ? 0 : 1);
        cpuBusDevice.attach(programData);

        // TODO: only if not disabled
        PhysicalMemory programMemory = new PhysicalMemory("WRAM", ushort(0x6000), ushort(0x7FFF), 8 * 1024);
        cpuBusDevice.attach(programMemory);

        // TODO: only if battery
        PhysicalMemory programStorage = new PhysicalMemory("SRAM", ushort(0x6000), ushort(0x7FFF), 8 * 1024);

        BankedMemory videoData = new BankedMemory("CHR-ROM", ushort(0x0000), new Quantity(1, BANK_8KB))
            .setVirtualBanks(new Quantity(1, BANK_8KB))
            .setPhysicalBanks(new Quantity(1, BANK_8KB));

        videoData.preloadPhysicalBanks(nesFile.data().video());
        videoData.mapVirtualToPhysical(0, 0);

        // TODO: banked memory but only if 4 screen
        PhysicalMemory videoMemory = new PhysicalMemory("VRAM", ushort(0x2000), ushort(0x2FFF), 2 * 1024);
        PhysicalMemory videoStorage = new PhysicalMemory("NVVRAM", ushort(0x2000), ushort(0x2FFF), 2 * 1024);

        ppuBusDevice.attach(videoData);

        ReservedMemory unusedPpu = new ReservedMemory("Unused PPU", UNUSED_START, UNUSED_END);
        ppuBusDevice.attach(unusedPpu);

        ReservedMemory privatePpu = new ReservedMemory("Private PPU", PALETTE_RAM_START, PALETTE_RAM_MIRROR_END);
        ppuBusDevice.attach(privatePpu);
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
        return cpuBusDevice;
    }

    @Override
    public void setIrqDetector(Detector irqDetector) {}

    @Override
    public MemoryDevice.ReadWrite getPpuBusDevice(
        BankedMemory ppuVideoMemory
    ) {
        this.ppuVideoMemory = ppuVideoMemory;

        configurePpuVideoMemory();

        return ppuBusDevice;
    }

    private void configurePpuVideoMemory() {
        final BankedMemory ppuVideoMemory = assertNonNull(this.ppuVideoMemory, "ppuVideoMemory should not be null");

        ppuVideoMemory
            .setVirtualBanks(new Quantity(4, BANK_1KB));

        switch(nesFile.meta().videoData().layout()) {
            case STANDARD_VERTICAL, ALTERNATIVE_VERTICAL -> ppuVideoMemory
                .mapVirtualToPhysical(0, 0)
                .mapVirtualToPhysical(1, 0)
                .mapVirtualToPhysical(2, 1)
                .mapVirtualToPhysical(3, 1);
            case STANDARD_HORIZONTAL, ALTERNATIVE_HORIZONTAL -> ppuVideoMemory
                .mapVirtualToPhysical(0, 0)
                .mapVirtualToPhysical(1, 1)
                .mapVirtualToPhysical(2, 0)
                .mapVirtualToPhysical(3, 1);
        }

        ppuBusDevice.attach(ppuVideoMemory);
    }

    @Override
    public void disconnect() {
        if (ppuVideoMemory != null) {
            ppuBusDevice.detach(ppuVideoMemory);
            ppuVideoMemory = null;
        }

        // TODO: disconnect from irq
        // TODO: maybe onDetach cpu and ppu bus devices
    }
}
