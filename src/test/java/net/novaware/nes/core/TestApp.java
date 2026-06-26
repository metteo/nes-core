package net.novaware.nes.core;

import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.ImmutableCoreConfig;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.mx.NesCoreMXUtil;
import net.novaware.nes.core.ui.DefaultDisplayModel;
import net.novaware.nes.core.ui.TestUI;

import javax.swing.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static net.novaware.nes.core.util.UTypes.ubyte;

public class TestApp {
    static void main(String... args) throws Exception {
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

        NesCoreMXUtil.register(factory.getMXBean());

        board.getCartridgePort().connect(testRom);
        board.getDebugPort().connect(e -> {
            e.printStackTrace();
            board.powerOff();
        });

        final AtomicInteger keyState = new AtomicInteger();
        final DefaultDisplayModel displayModel = new DefaultDisplayModel();
        board.getDisplayPort().connect(pixels -> displayModel.setPixels(pixels));
        // FIXME: swing generates lots of garbage
        SwingUtilities.invokeLater(()-> TestUI.createAndShowGui(displayModel, keyState));

        board.getJoypad1Port().connect(() -> ubyte(keyState.get()));

        board.powerOn();

//        board.powerOff();

//        while (true) {
//        Thread.sleep(5_000);

//        factory.getPatternTable0().dump();
//        factory.getPatternTable1().dump();
//        System.out.println(factory.getNameTable0().printBackground());
//        System.out.println(factory.getAttributeTable0().printAttributeBits(false));
//        System.out.println(factory.getPaletteMemory().printColors());
//        System.out.println(factory.getObjAttrTables().print());
//        }

        //System.exit(0); // TODO: notify swing to shut down instead
    }
}
