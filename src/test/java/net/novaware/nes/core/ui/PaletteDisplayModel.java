package net.novaware.nes.core.ui;

import java.awt.*;

import static net.novaware.nes.core.util.UTypes.ubyte;

public class PaletteDisplayModel extends DefaultDisplayModel {
    @Override
    public Color getColor(int y, int x) {
        return new Color(paletteData.getColor(ubyte(x / 4 & 0x3F))); // FIXME: lots of objects!
    }
}
