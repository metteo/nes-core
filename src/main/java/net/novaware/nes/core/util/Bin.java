package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * Converts numbers into string representation of binary literal with '_'
 * separating nibbles and '0b' prefix.
 * Useful in spock smart assertions (mismatch shows differing bits)
 */
public class Bin {

    public static String s(@Unsigned byte b) {
        @SuppressWarnings("signedness") // it's an ubyte, no need to worry about sign of int
        String binary = Integer.toBinaryString(uint(b));
        String padded = String.format("%8s", binary).replace(' ', '0');
        int middle = padded.length() / 2;
        return "0b" + padded.substring(0 , middle) + "_" + padded.substring(middle);
    }

}
