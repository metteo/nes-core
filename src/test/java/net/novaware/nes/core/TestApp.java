package net.novaware.nes.core;

import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.ImmutableCoreConfig;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.ppu.table.PatternTable;
import net.novaware.nes.core.ui.DefaultDisplayModel;
import net.novaware.nes.core.ui.TestUI;
import net.novaware.nes.core.util.Hex;

import javax.swing.*;
import java.net.URI;

import static net.novaware.nes.core.util.UTypes.ubyte;

public class TestApp {
    static void main(String... args) throws InterruptedException {
        CoreConfig config = ImmutableCoreConfig.builder()
                .setRecordCpuBus(false)
                .setRegion(Region.USA)
                .setPlatform(Platform.NES_FAMICOM)
                .setVideoStandard(VideoStandard.NTSC)
                .build();
        NesCore factory = NesCore.newNesCore(config);

        URI nestest = URI.create("file://" + args[0].replace(" ", "%20"));
        Cartridge testRom = factory.newCartridge(nestest);

        var board = factory.newBoard();
        board.getCartridgePort().connect(testRom);
        board.getDebugPort().connect(e -> {
            e.printStackTrace();
            board.powerOff();
        });

        final DefaultDisplayModel displayModel = new DefaultDisplayModel();
        board.getDisplayPort().connect(displayModel::setPixels);
        SwingUtilities.invokeLater(()-> TestUI.createAndShowGui(displayModel));

        board.powerOn();

//        Thread.sleep(4_000);
//        board.powerOff();

//        dumpPattern(factory.getPatternTable0());
//        dumpPattern(factory.getPatternTable1());
//        System.out.println(factory.getNameTable0().printBackground());
//        System.out.println(factory.getAttributeTable0().printAttributeBits(false));
//        System.out.println(factory.getPaletteMemory().printColors());
//        System.out.println(factory.getObjAttrMemory().printOam());

        //System.exit(0); // TODO: notify swing to shut down instead
    }

    // TODO: move to PatternTable
    static void dumpPattern(PatternTable patternMemory) {
        for (int i = 0x00; i < 0x100; i++) {
            String pattern = patternMemory.printPattern(i);
            System.out.println(Hex.s(ubyte(i)));
            System.out.println(pattern);
        }
    }
}
