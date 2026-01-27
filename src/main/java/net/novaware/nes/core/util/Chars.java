package net.novaware.nes.core.util;

public class Chars {

    /**
     * Limited charset to prevent strange symbols
     */
    public static boolean isPrintable(int c) {
        return 32 <= c && c < 127;
    }
}
