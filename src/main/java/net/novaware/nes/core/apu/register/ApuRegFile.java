package net.novaware.nes.core.apu.register;

import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

/**
 * @see <a href="https://www.nesdev.org/wiki/APU_registers">APU Registers on nesdev.org</a>
 * @see <a href="https://www.nesdev.org/wiki/2A03">2A03 on nesdev.org</a>
 */
@BoardScope
public class ApuRegFile extends RegisterFile {

    public CycleCounter cycleCounter = new CycleCounter("APUCC");

    // FIXME: there registers are asymetrical, writes are accepted, reads cause open bus (mostly)

    // Pulse 1 Channel
    private ByteRegister sq1Vol = new ByteRegister("SQ1_VOL"); // 0x4000
    private ByteRegister sq1Sweep = new ByteRegister("SQ1_SWEEP");
    private ByteRegister sq1Lo = new ByteRegister("SQ1_LO");
    private ByteRegister sq1Hi = new ByteRegister("SQ1_HI");

    // Pulse 2 Channel
    private ByteRegister sq2Vol = new ByteRegister("SQ2_VOL");
    private ByteRegister sq2Sweep = new ByteRegister("SQ2_SWEEP");
    private ByteRegister sq2Lo = new ByteRegister("SQ2_LO");
    private ByteRegister sq2Hi = new ByteRegister("SQ2_HI");

    // Triangle Channel
    private ByteRegister triLinear = new ByteRegister("TRI_LINEAR");
    private ByteRegister triUnused = new ByteRegister("TRI_UNUSED");
    private ByteRegister triLo = new ByteRegister("TRI_LO");
    private ByteRegister triHi = new ByteRegister("TRI_HI");

    // Noise Channel
    private ByteRegister noiseVol = new ByteRegister("NOISE_VOL");
    private ByteRegister noiseUnused = new ByteRegister("NOISE_UNUSED");
    private ByteRegister noiseLo = new ByteRegister("NOISE_LO");
    private ByteRegister noiseHi = new ByteRegister("NOISE_HI");

    // DMC Channel (APU DMA)
    private ByteRegister dmcFreq = new ByteRegister("DMC_FREQ"); // 0x4010
    private ByteRegister dmcRaw = new ByteRegister("DMC_RAW");
    private ByteRegister dmcStart = new ByteRegister("DMC_START");
    private ByteRegister dmcLength = new ByteRegister("DMC_LEN"); // 0x4013

    private ByteRegister oamDma; // 0x4014
    private ByteRegister sndChn = new ByteRegister("SNDCHN"); // TODO: use dedicate status / control register
    private ByteRegister joy1 = new ByteRegister("JOY1");
    private ByteRegister joy2 = new ByteRegister("JOY2"); // 0x4017

    //@Inject
    public ApuRegFile(
        @DmaVar(OAM) ByteRegister oamDma
    ) {
        super("APU_REG");

        this.oamDma = oamDma;

        // TODO: inject with all the registers from module instead of creating them here

        // TODO: initialize fields inside the list to make sure all items are there
        dataRegisters = List.of(
            sq1Vol,     sq1Sweep,    sq1Lo,    sq1Hi,
            sq2Vol,     sq2Sweep,    sq2Lo,    sq2Hi,
            triLinear,  triUnused,   triLo,    triHi,
            noiseVol,   noiseUnused, noiseLo,  noiseHi,
            dmcFreq,    dmcRaw,      dmcStart, dmcLength,
            oamDma,     sndChn,      joy1,     joy2
        );

        addressRegisters = List.of();
    }

    public ByteRegister[] getCpuRegisters() {
        return new ByteRegister[]{
                sq1Vol, sq1Sweep, sq1Lo, sq1Hi,
                sq2Vol, sq2Sweep, sq2Lo, sq2Hi,
                triLinear, triUnused, triLo, triHi,
                noiseVol, noiseUnused, noiseLo, noiseHi,
                dmcFreq, dmcRaw, dmcStart, dmcLength,
                oamDma, sndChn, joy1, joy2
        };
    }

}
