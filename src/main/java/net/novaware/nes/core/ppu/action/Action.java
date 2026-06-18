package net.novaware.nes.core.ppu.action;

import java.util.stream.Stream;

import static net.novaware.nes.core.ppu.action.ActionCategory.BUS;
import static net.novaware.nes.core.ppu.action.ActionCategory.DRAW;
import static net.novaware.nes.core.ppu.action.ActionCategory.FLAG;
import static net.novaware.nes.core.ppu.action.ActionCategory.MISC;
import static net.novaware.nes.core.ppu.action.ActionCategory.OAM;
import static net.novaware.nes.core.ppu.action.ActionCategory.VIEW;

/**
 * @see gemini: NES Timelines
 */
public enum Action {

    // region Bus

    ACCESS_NAME_TABLE_ADDRESS  ("NTA", BUS),
    READ_NAME_TABLE_DATA       ("NTD", BUS),

    ACCESS_ATTR_TABLE_ADDRESS  ("ATA", BUS),
    READ_ATTR_TABLE_DATA       ("ATD", BUS),

    ACCESS_BG_LO_BITS_ADDRESS  ("BLA", BUS),
    READ_BG_LO_BITS_DATA       ("BLD", BUS),

    ACCESS_BG_HI_BITS_ADDRESS  ("BHA", BUS),
    READ_BG_HI_BITS_DATA       ("BHD", BUS),

    UNUSED_NAME_TABLE_DATA     ("NDU", BUS), // TODO: use unused and ignored NT fetches to do extended sec oam sprite fetching
    IGNORED_NAME_TABLE_DATA    ("NDI", BUS),

    ACCESS_SP_LO_BITS_ADDRESS  ("SLA", BUS),
    READ_SP_LO_BITS_DATA       ("SLD", BUS),

    ACCESS_SP_HI_BITS_ADDRESS  ("SHA", BUS),
    READ_SP_HI_BITS_DATA       ("SHD", BUS),

    // endregion
    // region OAM

    CLR_SECONDARY_OAM ("CSO", OAM),
    EVAL_PRIMARY_OAM  ("EPO", OAM),

    // endregion
    // region Draw

    RENDER ("RDR", DRAW),
    CLEAR  ("CLR", DRAW),

    // endregion
    // region View

    INCREMENT_X      ("INX", VIEW),
    INCREMENT_Y      ("INY", VIEW),

    TRANSFER_TX_TO_X ("TTX", VIEW), // h
    TRANSFER_TY_TO_Y ("TTY", VIEW), // v

    // endregion
    // region Flag

    SET_VBLANK ("SEV", FLAG),
    SET_HBLANK ("SEH", FLAG),
    CLR_HBLANK ("CLH", FLAG),
    CLR_STATUS ("CLS", FLAG),

    // endregion
    // region Misc

    SHIFT ("ASL", MISC),

    NO_OPERATION ("NOP", MISC),

    // endregion

    UNKNOWN ("???", ActionCategory.UNKNOWN),
    ;
    private final String mnemonic;
    private final ActionCategory category;

    Action(String mnemonic, ActionCategory category) {
        this.mnemonic = mnemonic;
        this.category = category;
    }

    public String getMnemonic() { return mnemonic; }
    public ActionCategory getCategory() { return category; }

    public static Stream<Action> stream() {
        return Stream.of(values());
    }
}
