package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.HexFormat;

public class Hex {

    private static final HexFormat hexFormat = HexFormat.ofDelimiter(" ");

    public static byte[] b(CharSequence input) {
        return hexFormat.parseHex(input);
    }

    public static byte b(int input) {
        return (byte) input;
    }

    @SuppressWarnings("signedness")
    public static String s(@Unsigned byte[] input) {
        return hexFormat.formatHex(input);
    }

    public static String s(byte input) {
        return hexFormat.toHexDigits(input);
    }
}
