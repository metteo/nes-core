package net.novaware.nes.core.ppu.action;

import static net.novaware.nes.core.ppu.action.ActionCategory.BUS;
import static net.novaware.nes.core.ppu.action.ActionCategory.DRAW;
import static net.novaware.nes.core.ppu.action.ActionCategory.MISC;
import static net.novaware.nes.core.ppu.action.ActionCategory.OAM;
import static net.novaware.nes.core.ppu.action.ActionCategory.REGS;

/**
 * @see gemini: NES Timelines
 */
public enum Action {

    // region Bus

    ACCESS_NAME_TABLE_ADDRESS  ("NTA", BUS),
    READ_NAME_TABLE_DATA       ("NTD", BUS),

    ACCESS_ATTR_TABLE_ADDRESS  ("ATA", BUS),
    READ_ATTR_TABLE_DATA       ("ATD", BUS),

    // TODO: dot 0 skipped on even frames
    // TODO: dot 0 of every scanline except see above
    ACCESS_BG_LO_BITS_ADDRESS  ("BLA", BUS),
    READ_BG_LO_BITS_DATA       ("BLD", BUS),

    ACCESS_BG_HI_BITS_ADDRESS  ("BHA", BUS),
    READ_BG_HI_BITS_DATA       ("BHD", BUS),

    UNUSED_NAME_TABLE_ADDRESS  ("NTAU", BUS),
    UNUSED_NAME_TABLE_DATA     ("NTAU", BUS),
                                    // TODO: use unused and ignored NT fetches to do extended sec oam sprite fetching
    IGNORED_NAME_TABLE_ADDRESS ("NTAI", BUS),
    IGNORED_NAME_TABLE_DATA    ("NTAI", BUS),

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

    // endregion
    // region Regs

    INCREMENT_X     ("INX", REGS),
    INCREMENT_Y     ("INY", REGS),

    TRANSFER_T_TO_X ("TTX", REGS),
    TRANSFER_T_TO_Y ("TTY", REGS),

    SET_VBLANK      ("SEV", REGS),
    CLR_STATUS      ("CLS", REGS),

    // endregion
    // region Misc

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
}
