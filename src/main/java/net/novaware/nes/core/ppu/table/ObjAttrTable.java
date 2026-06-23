package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.register.ObjAttrRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.ENTRY_SIZE;
import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.Masks.BIT_5;
import static net.novaware.nes.core.util.Masks.BIT_6;
import static net.novaware.nes.core.util.Masks.BIT_7;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: do the same with all the tables, move address generating
public class ObjAttrTable implements Table {

    private final String name;
    private final ObjAttrRegister cursor;
    private final ObjAttrMemory memory;

    public ObjAttrTable(
        String name,
        ObjAttrRegister cursor,
        ObjAttrMemory memory
    ) {
        this.name = name;
        this.cursor = cursor;
        this.memory = memory;
    }

    @Override
    public String getName() {
        return name;
    }

    private void verifyCursor() {
        // TODO: consider assert keyword or configurable assert (through compile time constant)
        assertState((cursor.getAsInt() & 0b11) == 0, "misaligned table access");
    }

    public int getRow() {
        verifyCursor();

        return cursor.getAsInt() / ENTRY_SIZE;
    }

    public void setRow(int row) {
        verifyCursor();

        cursor.setAsByte(row * ENTRY_SIZE);
    }

    public void nextRow() {
        verifyCursor();

        int currentAddr = cursor.getAsInt();

        cursor.setAsByte(currentAddr + ENTRY_SIZE);
    }

    public int size() {
        return memory.getCount();
    }

    public @Unsigned byte getY() {
        verifyCursor();

        return memory.read(cursor.get());
    }

    public int getYAsInt() {
        return sint(getY());
    }

    public @Unsigned byte getTile() {
        verifyCursor();

        int address = cursor.getAsInt() + 1;

        return memory.read(ubyte(address));
    }

    public int getTileAsInt() {
        return sint(getTile());
    }

    public @Unsigned byte getAttr() {
        verifyCursor();

        int address = cursor.getAsInt() + 2;

        return memory.read(ubyte(address));
    }

    public int getAttrAsInt() {
        return sint(getAttr());
    }

    public @Unsigned byte getX() {
        verifyCursor();

        int address = cursor.getAsInt() + 3;

        return memory.read(ubyte(address));
    }

    public int getXAsInt() {
        return sint(getX());
    }

    @Override
    public String toString() {
        return name + " (0:" + memory.getCount() + ")";
    }

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
