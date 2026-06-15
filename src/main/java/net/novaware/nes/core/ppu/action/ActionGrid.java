package net.novaware.nes.core.ppu.action;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.IntegerCounter;

import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.FC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LC;

@BoardScope
public class ActionGrid {

    private final IntegerCounter frameCounter;
    private final IntegerCounter lineCounter;
    private final IntegerCounter dotCounter;

    // private final ScanLine[] scanLines;

    @Inject
    public ActionGrid(
        VideoStandard videoStandard,
        @PpuVar(FC) IntegerCounter frameCounter,
        @PpuVar(LC) IntegerCounter lineCounter,
        @PpuVar(DC) IntegerCounter dotCounter
    ) {
        this.frameCounter = frameCounter;
        this.lineCounter = lineCounter;
        this.dotCounter = dotCounter;
        int height = videoStandard.getPhysicalHeight();

        //scanLines = new ScanLine[height];
    }
}
