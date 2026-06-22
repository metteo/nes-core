package net.novaware.nes.core.util;

public final class Nums {

    public static boolean powOfTwo(int num) {
        return (num & (num - 1)) == 0;
    }
}
