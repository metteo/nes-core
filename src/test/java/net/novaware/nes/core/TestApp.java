package net.novaware.nes.core;

import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.ImmutableCoreConfig;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;

import java.net.URI;

public class TestApp {
    static void main(String... args) throws InterruptedException {
        CoreConfig config = ImmutableCoreConfig.builder()
                .setRecordCpuBus(false)
                .setRegion(Region.USA)
                .setPlatform(Platform.NES_FAMICOM)
                .setVideoStandard(VideoStandard.NTSC)
                .build();
        NesCore factory = NesCore.newNesCore(config);

        URI nestest = URI.create("file://" + args[0]);
        Cartridge testRom = factory.newCartridge(nestest);

        var board = factory.newBoard();
        board.getCartridgePort().connect(testRom);
        board.getDebugPort().connect(e -> {
            e.printStackTrace();
            board.powerOff();
        });

        board.powerOn();

        Thread.sleep(4_000);
        board.powerOff();
    }
}
