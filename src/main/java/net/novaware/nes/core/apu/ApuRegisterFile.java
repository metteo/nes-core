package net.novaware.nes.core.apu;

import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.register.ByteRegister;

/**
 * @see <a href="https://www.nesdev.org/wiki/APU_registers">APU Registers on nesdev.org</a>
 * @see <a href="https://www.nesdev.org/wiki/2A03">2A03 on nesdev.org</a>
 */
public class ApuRegisterFile {

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

    // DMC Channel
    private ByteRegister dmcFreq = new ByteRegister("DMC_FREQ");
    private ByteRegister dmcRaw = new ByteRegister("DMC_RAW");
    private ByteRegister dmcStart = new ByteRegister("DMC_START");
    private ByteRegister dmcLength = new ByteRegister("DMC_LEN");

    private ByteRegister oamDma = new ByteRegister("OAMDMA"); // 0x4014 // FIXME: same instance as in PPU ? / !
    private ByteRegister sndChn = new ByteRegister("SNDCHN");
    private ByteRegister joy1 = new ByteRegister("JOY1");
    private ByteRegister joy2 = new ByteRegister("JOY2"); // 0x4017

    public ByteRegisterMemory asByteRegisterMemory() {
        return new ByteRegisterMemory("APUREGS", new ByteRegister[]{
                sq1Vol, sq1Sweep, sq1Lo, sq1Hi,
                sq2Vol, sq2Sweep, sq2Lo, sq2Hi,
                triLinear, triUnused, triLo, triHi,
                noiseVol, noiseUnused, noiseLo, noiseHi,
                dmcFreq, dmcRaw, dmcStart, dmcLength,
                oamDma, sndChn, joy1, joy2
        });
    }

}
