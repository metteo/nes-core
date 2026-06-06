package net.novaware.nes.core.clock;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.inject.PpuVarName;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.DoubleCounter;
import net.novaware.nes.core.register.IntegerCounter;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

// stats
// predictions
// frame time / power reserve
// percentages of work time cpu / ppu / apu / dma
// sampling into a table to for graphs

@BoardScope
public class MasterClock implements ClockGenerator, Runnable { // TODO: this is a prototype, needs major testing and refactor

    public ClockReceiver cpu;
    public ClockReceiver ppu;
    public ClockReceiver apu;
    public ClockReceiver dma;

    public final VideoStandard videoStandard;

    private final ExecutorService executor;
    private @Nullable Future<?> future = null;
    public Consumer<Exception> exceptionHandler = _ -> {};

    public boolean        timeTicking = false;
    public boolean        timeRestricted = false;
    public IntegerCounter timeBudget = new IntegerCounter("CLK.TB"); // secs
    public IntegerCounter timeCounter = new IntegerCounter("CLK.TC"); // secs

    public DoubleCounter  frameBudget = new DoubleCounter("CLK.FB"); // frames / sec
    public IntegerCounter frameCounter = new IntegerCounter("CLK.FC"); // frames
    public long           frameDuration; // nanos / frame

    // TODO: track cpu odd/even or read/write cycle state
    public IntegerCounter cpuClockBudget = new IntegerCounter("CPU.CB"); // per frame

    public final BooleanRegister oddFrame; // TODO: maybe MasterClock should own this instead of ppu?
    public DoubleCounter ppuClockBudget = new DoubleCounter("PPU.CB"); // per frame (even / odd)
    public DoubleCounter ppuToCpuCycleBudget = new DoubleCounter("PPU.TODO");

    public IntegerCounter apuClockBudget = new IntegerCounter("APU.CB"); // per frame (every other cpu cycle)
    public DoubleCounter apuToCpuCycleBudget = new DoubleCounter("APU.TODO");

    public IntegerCounter dmaClockBudget = new IntegerCounter("DMA.CB"); // per frame (+cpu alignment)

    @Inject
    public MasterClock(
        CoreConfig coreConfig, // TODO: maybe use Cart.Config or separate Clock.Config
        @PpuVar(PpuVarName.OF) BooleanRegister oddFrame,
        @Named("CPU") ClockReceiver cpu,
        @Named("PPU") ClockReceiver ppu,
        @Named("APU") ClockReceiver apu,
        @Named("DMA") ClockReceiver dma,
        @Named("CLK") ExecutorService clockExecutor
    ) {
        this.videoStandard = coreConfig.getVideoStandard();

        this.oddFrame = oddFrame;

        this.cpu = cpu;
        this.ppu = ppu;
        this.apu = apu;
        this.dma = dma;

        this.executor = clockExecutor;
    }

    public void runFrame() {
        frameCounter.increment();
        frameBudget.decrement();

        while (ppuClockBudget.getValue() >= videoStandard.getPpuDivisor()) { // TODO: calculate it correctly (double with epsilon)
            int cpuCyclesConsumed = cpu.cycle();
            cpuClockBudget.decrementBy(cpuCyclesConsumed * videoStandard.getCpuDivisor());

            double ppuCyclesBudget = (double) cpuCyclesConsumed * videoStandard.getCpuDivisor() / videoStandard.getPpuDivisor();
            ppuToCpuCycleBudget.setValue(ppuToCpuCycleBudget.getValue() + ppuCyclesBudget);

            while (ppuToCpuCycleBudget.getValue() > 0.9) { // don't delay last cycle due to fraction inequality
                ppuToCpuCycleBudget.decrement();
                int ppuCyclesConsumed = ppu.cycle();
                ppuClockBudget.decrementBy(ppuCyclesConsumed * videoStandard.getPpuDivisor());
            }

            double apuCyclesBudget = (double) cpuCyclesConsumed * videoStandard.getCpuDivisor() / videoStandard.getApuDivisor();
            apuToCpuCycleBudget.setValue(apuToCpuCycleBudget.getValue() + apuCyclesBudget);

            while (apuToCpuCycleBudget.getValue() > 0.9) { // don't delay last cycle due to fraction inequality
                apuToCpuCycleBudget.decrement();
                int apuCyclesConsumed = apu.cycle();
                apuClockBudget.decrementBy(apuCyclesConsumed * videoStandard.getApuDivisor());
            }

            for (int d = cpuCyclesConsumed; d > 0; d--) {
                // TODO: or steal cycles from cpuClockBudget?
                int dmaCyclesConsumed = dma.cycle();
                dmaClockBudget.decrementBy(dmaCyclesConsumed * videoStandard.getDmaDivisor());
            }

        }
    }

    public void runAndMeasureFrame() {
        long workStart = System.nanoTime();
        runFrame();

        long workDuration = System.nanoTime() - workStart;                      // TODO: replace all System.nanoTime with TimeSource injected dependency
        long targetSpinDuration = Math.max(0, frameDuration - workDuration);
        long spinDuration = 0;

        if (targetSpinDuration > 0) {
            long spinStart = System.nanoTime();
            long targetTime = spinStart + targetSpinDuration;

            while (System.nanoTime() < targetTime) {
                // TODO: use some of the spin time to move video buffer / apu buffer to external threads safely
                // TODO: also safely poll the inputs for the next frame
                Thread.onSpinWait();
            }

            spinDuration = System.nanoTime() - spinStart;
            long overspin = Math.max(0, spinDuration - targetSpinDuration);
        }
    }

    public void tick() {
        calculateSecondBudget();

        //long tickStart = System.nanoTime();

        while (frameBudget.getValue() > 1) {
            calculateFrameBudget();
            runAndMeasureFrame();
        }

        timeCounter.increment();
    }

    @Override
    public void start() {
        if (future != null) {
            return;
        }

        timeTicking = true;
        future = executor.submit(this);
    }

    @Override
    public void stop() {
        if (future == null) {
            return;
        }

        timeTicking = false; // TODO: improve
    }

    @Override
    public void forceStop() {
        if (future == null) {
            return;
        }

        timeTicking = false;
        future.cancel(true);
        future = null;

        executor.close();
    }

    @Override
    public void setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            run0();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    private void run0() {
        // TODO: consider scheduling every second instead
        while (timeTicking) {
            tick();

            if (timeRestricted) {
                timeBudget.decrement();

                if (timeBudget.getValue() < 1) {
                    timeTicking = false;
                }
            }
        }
    }

    public void reset() {
        timeCounter.reset();
        timeBudget.reset();

        frameCounter.reset();
        frameBudget.reset();

        ppuClockBudget.reset();
        cpuClockBudget.reset();
        apuClockBudget.reset();
        dmaClockBudget.reset();
    }

    void calculateSecondBudget() {
        frameBudget.setValue(frameBudget.getValue() + videoStandard.getRefreshRate());

        frameDuration = 1_000L /* ms */ * 1_000_000L /* ns */ / (long) frameBudget.getValue();
    }

    void calculateFrameBudget() {
        double masterCycles = videoStandard.getMasterCycles();
        int ppuCycles = (int) (ppuClockBudget.getValue()
            + masterCycles
            - ((videoStandard.isOddFrameCycleSkip() && oddFrame.get()) ? videoStandard.getPpuDivisor() : 0)
        );

        ppuClockBudget.setValue(ppuCycles);
        cpuClockBudget.setValue((int)(cpuClockBudget.getValue() + masterCycles));
        apuClockBudget.setValue((int)(apuClockBudget.getValue() + masterCycles));
        dmaClockBudget.setValue((int)(dmaClockBudget.getValue() + masterCycles));
    }
}
