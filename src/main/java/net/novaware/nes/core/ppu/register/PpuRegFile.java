package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

import java.util.List;

import static net.novaware.nes.core.ppu.inject.PpuVarName.CB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CP;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EG;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.HB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org</a>
 */
@BoardScope
public class PpuRegFile extends RegisterFile {

    // TODO: switch to private + getters when PPU impl is more advanced
    public final CycleCounter cycleCounter;
    public final PpuStatusRegister status;
    public final BooleanRegister hBlank;
    public final ViewPortRegister currentViewPort;
    public final ViewPortRegister tempViewPort;
    public final BooleanRegister secondWrite;

    public final BooleanRegister vBlankInterruptEnabled;
    public final BooleanRegister masterSlaveSelect;
    public final BooleanRegister spriteSize;
    public final ShortRegister backgroundPatternTable;
    public final ShortRegister spritePatternTable;
    public final ByteRegister vramAddressIncrement;

    public final BooleanRegister emphasizeRed;
    public final BooleanRegister emphasizeGreen;
    public final BooleanRegister emphasizeBlue;
    public final BooleanRegister renderSprite;
    public final BooleanRegister renderBackground;
    public final BooleanRegister maskSprite;
    public final BooleanRegister maskBackground;
    public final BooleanRegister greyscale;

    private final ByteRegister oamAddress;

    @Inject
    public PpuRegFile(
        @PpuVar(CC) CycleCounter cycleCounter,
        @PpuVar(PS) PpuStatusRegister status,
        @PpuVar(HB) BooleanRegister hBlank,
        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T)  ViewPortRegister tempViewPort,
        @PpuVar(W)  BooleanRegister secondWrite,

        @PpuVar(CV) BooleanRegister vBlankInterruptEnabled,
        @PpuVar(CP) BooleanRegister masterSlaveSelect,
        @PpuVar(CH) BooleanRegister spriteSize,
        @PpuVar(CB) ShortRegister backgroundPatternTable,
        @PpuVar(CS) ShortRegister spritePatternTable,
        @PpuVar(CI) ByteRegister vramAddressIncrement,

        @PpuVar(ER) BooleanRegister emphasizeRed,
        @PpuVar(EG) BooleanRegister emphasizeGreen,
        @PpuVar(EB) BooleanRegister emphasizeBlue,
        @PpuVar(RS) BooleanRegister renderSprite,
        @PpuVar(RB) BooleanRegister renderBackground,
        @PpuVar(MS) BooleanRegister maskSprite,
        @PpuVar(MB) BooleanRegister maskBackground,
        @PpuVar(GS) BooleanRegister greyscale,

        @PpuVar(OAM) ByteRegister oamAddress
    ) {
        super("PPU.REGS");

        this.cycleCounter = cycleCounter;
        this.status = status;
        this.hBlank = hBlank;
        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;
        this.secondWrite = secondWrite;

        this.vBlankInterruptEnabled = vBlankInterruptEnabled;
        this.masterSlaveSelect = masterSlaveSelect;
        this.spriteSize = spriteSize;

        addressRegisters = List.of(
            this.backgroundPatternTable = backgroundPatternTable,
            this.spritePatternTable = spritePatternTable
        );
        dataRegisters = List.of(
            this.vramAddressIncrement = vramAddressIncrement,
            this.oamAddress = oamAddress
        );
        this.emphasizeRed = emphasizeRed;
        this.emphasizeGreen = emphasizeGreen;
        this.emphasizeBlue = emphasizeBlue;
        this.renderSprite = renderSprite;
        this.renderBackground = renderBackground;
        this.maskSprite = maskSprite;
        this.maskBackground = maskBackground;
        this.greyscale = greyscale;
    }
}
