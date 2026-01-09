package net.novaware.nes.core.file;

public abstract class NesFileHandler {

    public static final int HEADER_SIZE = 16;

    public enum Version {
        // TODO: add field with ordering and methods to compare versions. somehow allow to derive one version from another?
        ARCHAIC_iNES,

        NES_0_7,

        MODERN_iNES,

        UNOFFICIAL_iNES, // flag 10: tv-system pal/ntsc/dual, prg-ram present/not, board conflicts: yes/no

        NES_2_0
    }


}
