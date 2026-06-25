package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.register.ViewPortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class NameTables implements Tables {

    public static @Unsigned short getNameTableAddress(ViewPortRegister viewPort) {
        // TODO: consider using vram segment register here
        return ushort(0x2000 | (sint(viewPort.get()) & 0xFFF));
    }
}
