package net.novaware.nes.core.ppu.table;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Masks.BIT_5;
import static net.novaware.nes.core.util.Masks.BIT_6;
import static net.novaware.nes.core.util.Masks.BIT_7;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class ObjAttr {

    public static boolean asFlipV(@Unsigned byte attr) {
        return (sint(attr) & BIT_7) != 0;
    }

    public static boolean asFlipH(@Unsigned byte attr) {
        return (sint(attr) & BIT_6) != 0;
    }

    public static boolean asHidden(@Unsigned byte attr) {
        return (sint(attr) & BIT_5) != 0;
    }

    public static @Unsigned byte asUnused(@Unsigned byte attr) {
        return ubyte(sint(attr) >> 2 & 0b111);
    }

    public static @Unsigned byte asPalette(@Unsigned byte attr) {
        return ubyte(sint(attr) & 0b11);
    }
}
