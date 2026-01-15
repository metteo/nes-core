package net.novaware.nes.core.file;

import java.util.Map;

/**
 * Sections of Data passed through MessageDigest / hashing functions
 * Useful for metadata lookup in XML header DB / online or data verification
 * If there are multiple hash values for the same "title" then use a separate instance of {@link NesFile}
 * <p>
 *
 * @param file    of a whole file
 *                The process of hashing may use different combinations of header / data / footer to have higher
 *                chance of matching with meta db.
 * @param trainer hash
 * @param program hash
 * @param video   hash
 * @param misc    hash of PlayChoice-10 payload (inst and prom) or other
 */
public record NesHash(
        Map<Algorithm, String> file,

        Map<Algorithm, String> trainer,
        Map<Algorithm, String> program,
        Map<Algorithm, String> video,
        Map<Algorithm, String> misc
) {

    public static NesHash empty() {
        return new NesHash(Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    /**
     * Common message digest / integrity algorithms used to identify NES files
     */
    public enum Algorithm {
        SUM16, // sum of all bytes modulo 16 in hex
        CRC32, // in hex
        MD5,
        SHA1,
        SHA256
    }
}
