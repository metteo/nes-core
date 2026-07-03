package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.CB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CP;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;

@BoardScope
public class PpuCtrlRegister {

    public final BooleanRegister vBlankInterruptEnabled;
    public final BooleanRegister masterSlaveSelect;
    public final BooleanRegister spriteSize;
    public final ShortRegister backgroundPatternTable;
    public final ShortRegister spritePatternTable;
    public final ByteRegister vramAddressIncrement;
    public final ViewPortRegister tempViewPort; // only LT part

    @Inject
    public PpuCtrlRegister(
        @PpuVar(CV) BooleanRegister vBlankInterruptEnabled,
        @PpuVar(CP) BooleanRegister masterSlaveSelect,
        @PpuVar(CH) BooleanRegister spriteSize,
        @PpuVar(CB) ShortRegister backgroundPatternTable,
        @PpuVar(CS) ShortRegister spritePatternTable,
        @PpuVar(CI) ByteRegister vramAddressIncrement,
        @PpuVar(T)  ViewPortRegister tempViewPort
    ){
        this.vBlankInterruptEnabled = vBlankInterruptEnabled;
        this.masterSlaveSelect = masterSlaveSelect;
        this.spriteSize = spriteSize;
        this.backgroundPatternTable = backgroundPatternTable;
        this.spritePatternTable = spritePatternTable;
        this.vramAddressIncrement = vramAddressIncrement;
        this.tempViewPort = tempViewPort;
    }
}
