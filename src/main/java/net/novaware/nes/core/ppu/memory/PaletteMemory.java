package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.*;
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.BACKGROUND;
import static net.novaware.nes.core.ppu.unit.PaletteData.COLOR_TRANSPARENT;
import static net.novaware.nes.core.util.Asserts.assertNonNull;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: create Palette dictionary or sth with actual colors for displaying
//       (include emphasis in a way it's possible to compare all combinations
//       of a single color)

public class PaletteMemory implements MemoryDevice.ReadWrite, Nameable {

    private final String name;

    private final @Unsigned short startAddress;
    private final @Unsigned short endAddress;

    private final UByteBuffer palettes;
    private final int mask;

    private int position;

    private DataBus.Line dataLine = new OpenLine();

    public PaletteMemory(String name, @Unsigned short startAddress, @Unsigned short endAddress, int size) {
        this.name = name;

        this.startAddress = startAddress;
        this.endAddress = endAddress;

        final @Unsigned byte black = ubyte(0x0F);

        palettes = UByteBuffer.allocate(size)
                .order(LITTLE_ENDIAN)
                .fill(black);
        mask = palettes.capacity() - 1;

        // Poison mirrored, unreachable slots
        List.of(0x10, 0x14, 0x18, 0x1C)
                .forEach(i -> palettes.put(i, COLOR_TRANSPARENT));
    }

    @Override
    public String getName() {
        return name;
    }

    // region PPU Direct Access API

    /* package */ static int getColorPosition(Section section, int palette, int offset) {
        assert 0 <= palette && palette < 4 : "palette not in range";
        assert 0 <= offset && offset < 4 : "offset not in range";

        int sectionBit = offset == 0 ? 0 : section.bit;

        return sectionBit | (palette << 2) | offset;
    }

    public @Unsigned byte getColor(Section section, int palette, int offset) {
        assertNonNull(section, "section must not be null");

        int position = getColorPosition(section, palette, offset);

        return palettes.get(position);
    }

    /* package */ void setColor(Section section, int palette, int offset, @Unsigned byte color) {
        assertNonNull(section, "section must not be null");

        int position = getColorPosition(section, palette, offset);

        palettes.put(position, color);
    }

    // endregion
    // region CPU Bus Access API

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return endAddress;
    }

    @Override
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert sint(startAddress) <= sint(address) && sint(address) <= sint(endAddress);

        int position = getColorPosition(address);

        @Unsigned byte data = palettes.get(position);
        dataLine.data(data);
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        position = getColorPosition(address);

        palettes.position(position);
    }

    /* package */ int getColorPosition(@Unsigned short address) {
        int index = (sint(address) - sint(startAddress)) & mask;

        Section section = ((index & 0x10) != 0) ? FOREGROUND : BACKGROUND;
        int palette = (index & 0b1100) >> 2;
        int offset  =  index & 0b0011;

        return getColorPosition(section, palette, offset);
    }

    @Override
    public void onRead() {
        dataLine.data(palettes.get(position));
    }

    @Override
    public void onWrite() {
        final @Unsigned byte data = dataLine.data();
        palettes.put(position, data);
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }

    // endregion

    @Override
    public String toString() {
        return name + " (" + Hex.s(startAddress) + ":" + Hex.s(endAddress) + ")";
    }

    public String printColors() {
        StringBuilder colors = new StringBuilder();

        Section[] sections = Section.values();
        for (int s = 0; s < sections.length; s++) {
            Section section = sections[s];
            colors.append(section.name()).append("\t");

            for (int p = 0; p < 4; p++) {
                for (int o = 0; o < 4; o++) {
                    @Unsigned byte color = getColor(section, p, o);

                    colors.append(Hex.s(color)).append(" ");
                }
                colors.append("\t");
            }
            colors.append("\n");
        }

        return colors.toString();
    }

    public enum Section {
        BACKGROUND(0x00), // 0
        FOREGROUND(0x10), // 1 - sprite
        ;
        private final int bit;

        Section(int bit) {
            this.bit = bit;
        }

        public int bit() {
            return bit;
        }
    }
}
