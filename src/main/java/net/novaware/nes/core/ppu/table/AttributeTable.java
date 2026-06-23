package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Masks.BIT_1;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_attribute_tables">Attribute Tables on nesdev.org</a>
 */
public class AttributeTable extends MemBusTable {

    public static final int ROW_COUNT = 8;
    public static final int COL_COUNT = 8;

    public static final int SUBROW_COUNT = 2;
    public static final int SUBCOL_COUNT = 2;

    public AttributeTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public String printAttributeBytes() {
        int start = segment.getStartAsInt();

        StringBuilder builder = new StringBuilder();

        for (int y = 0; y < ROW_COUNT; y++) {
            for (int x = 0; x < COL_COUNT; x++) {
                int address = start + y * COL_COUNT + x;

                @Unsigned byte data = bus.access(ushort(address)).read().data();

                builder.append(Hex.s(data)).append(" ");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public String printAttributeBits(boolean useDigits) {
        int start = segment.getStartAsInt();

        StringBuilder pattern = new StringBuilder();

        for (int doubleY = 0; doubleY < 2 * ROW_COUNT; doubleY++) {
            for (int doubleX = 0; doubleX < 2 * COL_COUNT; doubleX++) {
                // ignore remainder on purpose
                int y = doubleY / 2;
                int x = doubleX / 2;

                int address = start + y * COL_COUNT + x;

                @Unsigned byte dataByte = bus.access(ushort(address)).read().data();

                int subY = doubleY - 2 * y; // TODO: is this faster than remainder? nope :(
                int subX = doubleX - 2 * x;

                int data = sint(dataByte);

                /**
                    int topLeft = data & 0b11;
                    int topRight = (data >> 2) & 0b11;
                    int bottomLeft = (data >> 4) & 0b11;
                    int bottomRight = (data >> 6) & 0b11;
                */
                int subVal = (data >> (subY * 4 + subX * 2)) & 0b11;

                char c = switch(subVal) {
                    case 0b11 -> '█';
                    case 0b10 -> '▓';
                    case 0b01 -> '▒';
                    case 0b00 -> '░';
                    default   -> '▞';
                };

                if (useDigits) {
                    pattern.append(subVal).append(" ");
                } else {
                    pattern.append(c);
                }
            }
            pattern.append("\n");
        }

        return pattern.toString();
    }

    public @Unsigned byte getAttribute(ViewPortRegister viewPort) {
        int start = segment.getStartAsInt();
        int y = (viewPort.getCoarseY() & 0b11100) << 1;
        int x = viewPort.getCoarseX() >> 2;

        @Unsigned short address = ushort(start | y | x );
        @Unsigned byte data = bus.access(address).read().data();

        return data;
    }

    public @Unsigned byte getSubAttribute(ViewPortRegister viewPort) {
        @Unsigned byte attribute = getAttribute(viewPort);

        return getSubAttribute(attribute, viewPort);
    }

    public static @Unsigned byte getSubAttribute(@Unsigned byte attribute, ViewPortRegister viewPort) {
        int y = (viewPort.getCoarseY() & BIT_1) >> 1;
        int x = (viewPort.getCoarseX() & BIT_1) >> 1;

        int shift = getSubAttributeShift(y, x);
        int mask = 0b11 << shift;

        int subAttribute = (sint(attribute) & mask) >> shift;

        return ubyte(subAttribute);
    }

    public static int getSubAttributeShift(int y, int x) {
        assert 0 <= y && y <= 1 : "y out of bounds";
        assert 0 <= x && x <= 1 : "x out of bounds";

        return x * 2 + y * 4;
    }

    public static int getSubAttributeMask(int y, int x) {
        assert 0 <= y && y <= 1 : "y out of bounds";
        assert 0 <= x && x <= 1 : "x out of bounds";

        return 0b11 << getSubAttributeShift(y, x);
    }


}
