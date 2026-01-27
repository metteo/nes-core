package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.NesMeta.ProgramMemory;
import net.novaware.nes.core.util.Quantity;
import org.jspecify.annotations.NonNull;

import static net.novaware.nes.core.file.NesMeta.Kind.NONE;
import static net.novaware.nes.core.file.NesMeta.Kind.PERSISTENT;
import static net.novaware.nes.core.file.NesMeta.Kind.UNKNOWN;
import static net.novaware.nes.core.file.NesMeta.Kind.VOLATILE;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;

/**
 * Resolves the actual size and persistence of PRG RAM (SRAM/Work RAM) by
 * adjudicating between three conflicting Modern iNES header fields.
 * <h3>The Implementation Strategy</h3>
 *
 * <ol>
 * <li><b>Priority 1 (Persistence):</b> If Byte 6 Bit 1 (Battery) is set,
 *     the system MUST provide at least 8 KB of RAM and persist it to disk.
 *     This bit is the most "official" and takes precedence over absence flags.
 * </li>
 * <li><b>Priority 2 (Specific Size):</b> If Byte 8 is non-zero, use it as
 *     the bank count (units of 8 KB). If Byte 8 is zero but the Battery
 *     bit is set, default to 1 bank (8 KB).
 * </li>
 * <li><b>Priority 3 (Absence Flag):</b> Byte 10 Bit 4 is used only as a
 *     "kill switch" for non-persistent Work RAM. If Byte 6 Bit 1 is NOT set
 *     and Byte 10 Bit 4 is set, the emulator should map 0 bytes to the
 *     $6000-$7FFF range to simulate "Open Bus" behavior.
 * </li>
 * </ol>
 *
 * <h3>The Reasoning</h3>
 * <ul>
 * <li><b>The Legacy Default:</b> Early iNES headers often left Byte 8 and 10
 *     as $00. Emulators must assume 8 KB of RAM for most mappers (like MMC1/3)
 *     if the battery bit is set, even if the "size" is technically zero.
 * </li>
 * <li><b>The "Open Bus" Problem:</b> Some games (e.g., <i>Low G Man</i>) do
 *     not have RAM hardware at $6000. If an emulator provides RAM there
 *     anyway, the game may fail to trigger its internal logic which expects
 *     to read floating bus values. Byte 10 Bit 4 was an unofficial extension
 *     to specifically disable the "default 8KB" assumption.
 * </li>
 * <li><b>Persistence vs. Absence:</b> Byte 6 is a <i>capability</i> flag
 *     (Battery), while Byte 10 is a <i>hardware</i> flag (RAM absence).
 *     A game can have RAM but no battery (Work RAM), but it effectively
 *     never has a battery with no RAM.
 * </li>
 * <li><b>Non-Volatile Storage:</b> While traditionally called the 'Battery Bit',
 *     this flag technically indicates any Non-Volatile storage (NVRAM, EEPROM,
 *     or Flash). If the mapper hardware dictates a serial protocol (e.g., EEPROM),
 *     this bit indicates that the state of that storage must be preserved
 *     between sessions.
 * </li>
 * </ul>
 */
public class ProgramMemoryResolver implements Resolver<ProgramMemory> {

    ProgramMemory resolve(Kind kind, Quantity size, Kind presence) {
        assertArgument(kind != null, "kind must not be null");
        assertArgument(size != null, "size must not be null");
        assertArgument(size.unit() == BANK_8KB, "size must be in 8KB units");
        assertArgument(size.amount() <= 4, "size should not exceed 4 8KB banks");
        assertArgument(presence != null, "presence must not be null");

        if (kind == PERSISTENT) { // we have a SaveRAM, just decide the size
            return memoryWithDefaultSize(PERSISTENT, size);
        } else if (kind == VOLATILE) { // we may have a WorkRAM default size
            if (presence == UNKNOWN) {
                return memoryWithDefaultSize(VOLATILE, size);
            } else if (presence == NONE) {
                if (size.amount() == 0) {
                    return new ProgramMemory(NONE, size);
                } else {
                    return new ProgramMemory(VOLATILE, size);
                }
            } else {
                throw new IllegalArgumentException("presence parameter can be only NONE or UNKNOWN, was: " + presence);
            }
        } else {
            throw new IllegalArgumentException("kind parameter can be only PERSISTENT or VOLATILE, was: " + kind);
        }
    }

    private @NonNull ProgramMemory memoryWithDefaultSize(Kind kind, Quantity size) {
        if (size.amount() > 0) {
            return new ProgramMemory(kind, size);
        } else {
            return new ProgramMemory(kind, new Quantity(1, BANK_8KB));
        }
    }
}
