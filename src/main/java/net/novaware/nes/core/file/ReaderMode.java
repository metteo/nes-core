package net.novaware.nes.core.file;

public enum ReaderMode {
    /**
     * Throws an exception for any deviation from the iNES / NES 2.0 / other specification.
     */
    STRICT,

    /**
     * Attempts to parse the file, logging warnings for minor issues
     * (e.g., truncated CHR) and only throwing exceptions for major,
     * blocking errors (e.g., corrupt header).
     */
    LENIENT
}
