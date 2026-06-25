package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Nameable;

import static net.novaware.nes.core.util.Masks.BIT_3;

public class PatternTables extends MemBusTable implements Tables, Nameable {

    public PatternTables(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public static int getAddress(Pattern.Size size, int table, int cell, int plane, int sliver) {
        return switch(size) {
            case SINGLE -> getSingleAddress(table, cell, plane, sliver);
            case DOUBLE -> getDoubleAddress(table, cell, plane, sliver);
            case UNKNOWN -> throw new IllegalArgumentException(size + " pattern size");
        };
    }

    public static int getAddress(Pattern.Size size, int table, int row, int col, int plane, int sliver) {
        return switch(size) {
            case SINGLE -> getSingleAddress(table, row, col, plane, sliver);
            case DOUBLE -> getDoubleAddress(table, row, col, plane, sliver);
            case UNKNOWN -> throw new IllegalArgumentException(size + " pattern size");
        };
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getSingleAddress(int table, int cell, int plane, int sliver) {
        // soft asserts for performance
        assert 0 <= table && table <= 0b1 : "table out of range";
        assert 0 <= cell && cell <= 0xFF : "cell out of range";
        assert 0 <= plane && plane <= 0b1 : "plane out of range";
        assert 0 <= sliver && sliver <= 0x7 : "sliver out of range";

        int tableShift = table << 12;
        int cellShift = cell << 4;
        int planeShift = plane << 3;

        int address = tableShift | cellShift | planeShift | sliver;

        return address;
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getSingleAddress(int table, int row, int col, int plane, int sliver) {
        // soft asserts for performance
        assert 0 <= row && row <= 0xF : "row out of range";
        assert 0 <= col && col <= 0xF : "col out of range";

        int rowShift = row << 4;
        int cell = rowShift | col;

        return getSingleAddress(table, cell, plane, sliver);
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getDoubleAddress(int table, int cell, int plane, int sliver) {
        // soft asserts for performance
        assert 0 <= table && table <= 0b1 : "table out of range";
        assert 0 <= cell && cell <= 0xFE && ((cell & 0b1) == 0) : "cell out of range";
        assert 0 <= plane && plane <= 0b1 : "plane out of range";
        assert 0 <= sliver && sliver <= 0xF : "sliver out of range";

        int half = (sliver & BIT_3) >> 3;

        int tableShift = table << 12;
        int cellShift = cell << 4;
        int halfShift = half << 4;
        int planeShift = plane << 3;
        int sliverMask = sliver & 0b111;

        int address = tableShift | cellShift | halfShift | planeShift | sliverMask;

        return address;
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getDoubleAddress(int table, int row, int col, int plane, int sliver) {
        // soft asserts for performance
        assert 0 <= row && row <= 0xF : "row out of range";
        assert 0 <= col && col <= 0x7 : "col out of range";

        int rowShift = row << 4;
        int colShift = col << 1;
        int cell = rowShift | colShift;

        return getDoubleAddress(table, cell, plane, sliver);
    }
}
