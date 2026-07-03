package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.apu.inject.ApuModule;
import net.novaware.nes.core.board.Board;
import net.novaware.nes.core.board.inject.BoardModule;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.cart.internal.CartridgeImpl;
import net.novaware.nes.core.clock.inject.ClockModule;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.cpu.inject.CpuModule;
import net.novaware.nes.core.dma.inject.DmaModule;
import net.novaware.nes.core.file.ReaderMode;
import net.novaware.nes.core.file.ines.NesFileReader;
import net.novaware.nes.core.file.ines.NesFileReadingException;
import net.novaware.nes.core.io.inject.IoModule;
import net.novaware.nes.core.mx.NesCoreMXBeanImpl;
import net.novaware.nes.core.port.internal.PortModule;
import net.novaware.nes.core.ppu.inject.PpuModule;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.ppu.table.LayoutTable;
import net.novaware.nes.core.ppu.table.ObjAttrTables;
import net.novaware.nes.core.ppu.table.PatternTable;

import java.net.URI;

import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;

@BoardScope
@Component(modules = {
    DmaModule.class,
    ApuModule.class,
    IoModule.class,
    PpuModule.class,
    CpuModule.class,
    ClockModule.class,
    PortModule.class,
    BoardModule.class
})
public abstract class NesCore {

    // region NesCore creation

    public static NesCore newNesCore(CoreConfig config) {
        return DaggerNesCore.builder()
                .coreConfig(config)
                .build();
    }

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder coreConfig(CoreConfig config);

        public abstract NesCore build();
    }

    // endregion
    // region Factory methods

    public abstract Board newBoard();

    // TODO: do not expose these like that, create a port if needed.
    public abstract PaletteMemory getPaletteMemory();
    public abstract @PpuVar(PT0) PatternTable getPatternTable0();
    public abstract @PpuVar(PT1) PatternTable getPatternTable1();
    public abstract @PpuVar(LT0) LayoutTable getLayoutTable0();
    public abstract @PpuVar(AT0) AttributeTable getAttributeTable0();
    public abstract ObjAttrTables getObjAttrTables();
    public abstract NesCoreMXBeanImpl getMXBean();

    public Cartridge newCartridge(URI file) { // TODO: improve, for now only for nestest
        NesFileReader.Result result = new NesFileReader().read(file, ReaderMode.LENIENT);
        if (!result.problems().isEmpty() || result.nesFile() == null) {
            throw new NesFileReadingException(result.problems());
        }
        return new CartridgeImpl(result.nesFile());
    }

    // endregion
}
