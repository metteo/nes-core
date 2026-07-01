package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Nameable;

import static net.novaware.nes.core.util.Masks.BIT_3;

public class PatternTables extends MemBusTable implements Tables, Nameable {

    public static final int MEM_ROW_COUNT = 2;

    public PatternTables(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    // TODO: make instance method for getting / probing a line

    public static int getAddress(Pattern.Size size, int memRow, int cell, int plane, int line) {
        return switch(size) {
            case SINGLE -> getSingleAddress(memRow, cell, plane, line);
            case DOUBLE -> getDoubleAddress(memRow, cell, plane, line);
            case UNKNOWN -> throw new IllegalArgumentException(size + " pattern size");
        };
    }

    public static int getAddress(Pattern.Size size, int memRow, int row, int col, int plane, int line) {
        return switch(size) {
            case SINGLE -> getSingleAddress(memRow, row, col, plane, line);
            case DOUBLE -> getDoubleAddress(memRow, row, col, plane, line);
            case UNKNOWN -> throw new IllegalArgumentException(size + " pattern size");
        };
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getSingleAddress(int memRow, int cell, int plane, int line) {
        // soft asserts for performance
        assert 0 <= memRow && memRow < MEM_ROW_COUNT : "memRow out of range";
        assert 0 <= cell && cell <= 0xFF : "cell out of range";
        assert 0 <= plane && plane <= 0b1 : "plane out of range";
        assert 0 <= line && line <= 0x7 : "line out of range";

        int memRowShift = memRow << 12;
        int cellShift = cell << 4;
        int planeShift = plane << 3;

        int address = memRowShift | cellShift | planeShift | line;

        return address;
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getSingleAddress(int memRow, int row, int col, int plane, int line) {
        // soft asserts for performance
        assert 0 <= row && row <= 0xF : "row out of range";
        assert 0 <= col && col <= 0xF : "col out of range";

        int rowShift = row << 4;
        int cell = rowShift | col;

        return getSingleAddress(memRow, cell, plane, line);
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getDoubleAddress(int memRow, int cell, int plane, int line) {
        // soft asserts for performance
        assert 0 <= memRow && memRow < MEM_ROW_COUNT : "memRow out of range";
        assert 0 <= cell && cell <= 0x7F : "cell out of range";
        assert 0 <= plane && plane <= 0b1 : "plane out of range";
        assert 0 <= line && line <= 0xF : "line out of range";

        int half = (line & BIT_3) >> 3; // top or bottom

        int memRowShift = memRow << 12;
        int cellShift = cell << 5;
        int halfShift = half << 4;
        int planeShift = plane << 3;
        int lineMask = line & 0b111;

        int address = memRowShift | cellShift | halfShift | planeShift | lineMask;

        return address;
    }

    // TODO: prevent mistakes from wrong order of params
    public static int getDoubleAddress(int memRow, int row, int col, int plane, int line) {
        // soft asserts for performance
        assert 0 <= row && row <= 0xF : "row out of range";
        assert 0 <= col && col <= 0x7 : "col out of range";

        int rowShift = row << 3;
        int cell = rowShift | col;

        return getDoubleAddress(memRow, cell, plane, line);
    }
}
