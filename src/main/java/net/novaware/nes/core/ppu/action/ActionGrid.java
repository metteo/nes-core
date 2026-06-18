package net.novaware.nes.core.ppu.action;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.BooleanPipeline;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.util.Initializable;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.FC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.FT;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;

@BoardScope
public class ActionGrid implements Initializable { // TODO: consider ActionRegistry name

    @Owned
    private final IntegerCounter frameCounter;

    @Owned
    private final BooleanRegister frameToggle;

    @Owned
    private final IntegerCounter lineCounter;

    @Owned
    private final IntegerCounter dotCounter;

    @Used
    private final BooleanPipeline renderSprite;

    @Used
    private final BooleanPipeline renderBackground;

    private final boolean skipDot;
    private final int lineLimit;
    private final int dotLimit;

    private int dotSkip;

    // private final ScanLine[] scanLines;

    @Inject
    public ActionGrid(
        VideoStandard videoStandard,
        @PpuVar(FC) IntegerCounter frameCounter,
        @PpuVar(FT) BooleanRegister frameToggle,
        @PpuVar(LC) IntegerCounter lineCounter,
        @PpuVar(DC) IntegerCounter dotCounter,

        @PpuVar(RS) BooleanPipeline renderSprite,
        @PpuVar(RB) BooleanPipeline renderBackground
    ) {
        this.frameCounter = frameCounter;
        this.frameToggle = frameToggle;
        this.lineCounter = lineCounter;
        this.dotCounter = dotCounter;

        this.renderSprite = renderSprite;
        this.renderBackground = renderBackground;

        skipDot = videoStandard.isSkipDot();
        lineLimit = videoStandard.getPhysicalHeight();
        dotLimit = videoStandard.getPhysicalWidth();

        //scanLines = new ScanLine[height];
    }

    public void initialize() {
        dotSkip = getDotSkip();
    }

    private int getDotSkip() {
        boolean lastLine = lineCounter.getValue() == lineLimit - 1;
        boolean renderOn = renderSprite.get() || renderBackground.get();
        boolean specialFrame = frameToggle.get();

        return skipDot && renderOn && specialFrame && lastLine ? 1 : 0;
    }

    public void increment() {
        dotCounter.increment();

        boolean nextLine = dotCounter.getValue() >= dotLimit - dotSkip;

        if(nextLine) {
            // TODO: consider mathematically overflowing to lineCounter instead of branching
            lineCounter.increment();
            dotCounter.reset();
        }

        dotSkip = getDotSkip();

        boolean nextFrame = lineCounter.getValue() >= lineLimit;

        if(nextFrame) {
            frameCounter.increment();
            frameToggle.toggle();
            lineCounter.reset();
        }
    }
}
