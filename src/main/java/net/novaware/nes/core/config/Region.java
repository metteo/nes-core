package net.novaware.nes.core.config;

// affects:
// CIC lockout,
// Audio Pin mixing,
// Arcade Palette selection
public enum Region { // TODO: region of the game or the console?
    USA,
    JAPAN,
    EUROPE,    // PAL
    CHINA,
    BRAZIL,    // PAL M
    ARGENTINA, // PAL N
    RUSSIA,    // DENDY
    POLAND,    // DENDY
    WORLD,
    UNKNOWN
}
