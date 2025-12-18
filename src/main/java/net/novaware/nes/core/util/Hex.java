package net.novaware.nes.core.util;

import java.util.HexFormat;

// XXX: Test utility, should be in test source root but jpms got in a way
public class Hex {

    private static final HexFormat hexFormat = HexFormat.ofDelimiter(" ");

    public static byte[] b(CharSequence input) {
        return hexFormat.parseHex(input);
    }

    public static byte b(int input) {
        return (byte) input;
    }

    public static String s(byte[] input) {
        return hexFormat.formatHex(input);
    }

    public static String s(byte input) {
        return hexFormat.toHexDigits(input);
    }
}
