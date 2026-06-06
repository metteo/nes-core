package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.ppu.register.ExtRegister;

public class RenderUnit {

    // background shift regs
    private short tileHighPlane;
    private short tileLowPlane;
    private byte attribute;

    // sprite shift regs, 8 / 16
    private byte spriteHighPlane;
    private byte spriteLowPlane;
    private byte spriteAttribute;
    private byte spriteCountDown;

    // ext input / output
    private ExtRegister ext = new ExtRegister();

}
