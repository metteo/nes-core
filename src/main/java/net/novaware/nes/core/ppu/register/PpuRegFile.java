package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;
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
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DR;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EG;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.HB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OF;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;
import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org</a>
 */
@BoardScope
public class PpuRegFile extends RegisterFile {

    // TODO: switch to private + getters when PPU impl is more advanced
    public final IntegerCounter cycleCounter;
    public final IntegerCounter scanLineCounter;
    public final IntegerCounter dotCounter;

    public final PpuStatusRegister status;
    public final BooleanRegister hBlank;

    public final ViewPortRegister currentViewPort;
    public final ViewPortRegister tempViewPort;
    public final BooleanRegister secondWrite;
    public final ByteRegister dataReadBuffer;

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

    public final ByteRegister oamAddress;

    public final BooleanRegister oddFrame;

    @Inject
    public PpuRegFile(
        @PpuVar(CC) IntegerCounter cycleCounter,
        @PpuVar(SC) IntegerCounter scanLineCounter,
        @PpuVar(DC) IntegerCounter dotCounter,

        @PpuVar(PS) PpuStatusRegister status,
        @PpuVar(HB) BooleanRegister hBlank,
        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T)  ViewPortRegister tempViewPort,
        @PpuVar(W)  BooleanRegister secondWrite,
        @PpuVar(DR) ByteRegister dataReadBuffer,

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

        @PpuVar(OAM) ByteRegister oamAddress,

        @PpuVar(OF) BooleanRegister oddFrame
    ) {
        super("PPU.REGS");

        this.cycleCounter = cycleCounter;
        this.scanLineCounter = scanLineCounter;
        this.dotCounter = dotCounter;
        this.status = status;

        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;

        addressRegisters = List.of(
            this.backgroundPatternTable = backgroundPatternTable,
            this.spritePatternTable = spritePatternTable
        );
        dataRegisters = List.of(
            this.dataReadBuffer = dataReadBuffer,
            this.vramAddressIncrement = vramAddressIncrement,
            this.oamAddress = oamAddress
        );
        booleanRegisters = List.of(
            this.secondWrite = secondWrite,
            this.hBlank = hBlank,

            this.vBlankInterruptEnabled = vBlankInterruptEnabled,
            this.masterSlaveSelect = masterSlaveSelect,
            this.spriteSize = spriteSize,

            this.emphasizeRed = emphasizeRed,
            this.emphasizeGreen = emphasizeGreen,
            this.emphasizeBlue = emphasizeBlue,
            this.renderSprite = renderSprite,
            this.renderBackground = renderBackground,
            this.maskSprite = maskSprite,
            this.maskBackground = maskBackground,
            this.greyscale = greyscale,

            this.oddFrame = oddFrame
        );
    }

    public void resetControl() {
        vBlankInterruptEnabled.set(false);
        masterSlaveSelect.set(false);
        spriteSize.set(false);
        backgroundPatternTable.set(USHORT_0);
        spritePatternTable.set(USHORT_0);
        vramAddressIncrement.set(ubyte(1));
    }

    public void resetMask() {
        emphasizeRed.set(false);
        emphasizeGreen.set(false);
        emphasizeBlue.set(false);
        renderSprite.set(false);
        renderBackground.set(false);
        maskSprite.set(false);
        maskBackground.set(false);
        greyscale.set(false);
    }
}
