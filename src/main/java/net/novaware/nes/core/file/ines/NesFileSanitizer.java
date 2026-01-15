package net.novaware.nes.core.file.ines;

/**
 * Applies implied defaults and / or cleans up the garbage data
 */
public class NesFileSanitizer extends NesFileHandler {
    // TODO: default prg-ram to 8kb if byte8 is 0 in modern nes
    // int amount = Math.max(byte8, 1); // Value 0 infers 8 KB for compatibility, VOLATILE
    // TODO: default chr-ram to 8kb if there is no chr-rom in modern nes
    // TODO: allow removal of the info block from the end of the header
    // TODO: allow removal of the title from the footer
    // TODO: check the reader / writer and add more defaults
}
