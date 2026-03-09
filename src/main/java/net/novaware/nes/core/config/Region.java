package net.novaware.nes.core.config;

// affects:
// CIC lockout,
// Audio Pin mixing,
// Arcade Palette selection
public enum Region {
    USA,
    JAPAN,
    EUROPE,
    CHINA,
    BRAZIL, // @see VideoStandard#PAL_M
    WORLD,
    UNKNOWN
}
