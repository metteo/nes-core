package net.novaware.nes.core.util;

public class Blocks {
    public static char toChar(int val) {
        char c = switch(val) {
            case 0b11 -> '█';
            case 0b10 -> '▓';
            case 0b01 -> '░';
            case 0b00 -> ' ';
            default   -> '▒'; // error
        };
        return c;
    }
}
