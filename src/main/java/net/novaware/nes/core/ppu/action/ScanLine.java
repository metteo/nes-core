package net.novaware.nes.core.ppu.action;

public enum ScanLine { // TODO: maybe LineType as a name
    //            // NTSC
    RENDER_START, //     0
    RENDERING,    //   1 - 238
    RENDER_END,   //    239
    POST_RENDER,  //    240
    BLANK_START,  //    241
    BLANKING,     // 242 - 259
    BLANK_END,    //    260
    PRE_RENDER,   //    261
}
