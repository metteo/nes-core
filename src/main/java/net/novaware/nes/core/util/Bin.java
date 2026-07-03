package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Converts numbers into string representation of binary literal with '_'
 * separating nibbles and '0b' prefix.
 * Useful in spock smart assertions (mismatch shows differing bits)
 */
public class Bin {

    public static String s(@Unsigned byte b) {
        String padded = toBinaryString(sint(b), 8);

        return "0b" + insertNibbleSeparator(padded);
    }

    private static Stream<String> splitMiddleToStream(String s) {
        int index = s.length() / 2;
        return Stream.of(s.substring(0 , index), s.substring(index));
    }

    private static String insertNibbleSeparator(String s) {
        assertArgument(s.length() == 8, "length == 8 required");

        return splitMiddleToStream(s)
                .collect(joining("_"));
    }

    private static String insertNibbleSeparators(String s) {
        assertArgument(s.length() == 16, "length == 16 required");

        return splitMiddleToStream(s)
                .flatMap(Bin::splitMiddleToStream)
                .collect(joining("_"));
    }

    public static String s(@Unsigned short s) {
        String padded = toBinaryString(sint(s), 16);

        return "0b" + insertNibbleSeparators(padded);
    }

    @SuppressWarnings("signedness")
    private static String toBinaryString(int i, int padding) {
        String binary = Integer.toBinaryString(i);
        @SuppressWarnings("format.string") // NOTE: not a compile time constant. Consider FormatString from checker
        String padded = String.format("%" + padding + "s", binary).replace(' ', '0');
        return padded;
    }

    public static String s(int i, int padding) {
        return "0b" + toBinaryString(i, padding);
    }
}
