package net.novaware.nes.core.mapper;

import static net.novaware.nes.core.util.Asserts.assertArgument;

/**
 * @see <a href="https://www.nesdev.org/wiki/NROM">NROM on nesdev.org</a>
 */
public class NROM implements Mapper {

    public static final int NUMBER = 0;

    /**
     * Number of program data banks
     */
    private final int banks;

    public NROM(int banks) {
        assertArgument(1 <= banks && banks <= 2, "invalid number of banks");

        this.banks = banks;
    }

    @Override
    public int getNumber() {
        return NUMBER;
    }
}
