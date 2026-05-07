package net.novaware.nes.core.cpu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.SegmentRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.OS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ZP;

/**
 * CPU Segment Registers
 *
 * @see <a href="https://en.wikibooks.org/wiki/X86_Assembly/X86_Architecture">X86 Architecture</a>
 */
public class CpuSegFile extends RegisterFile {

    private final SegmentRegister zeroPage;

    private final SegmentRegister stackSegment;

    private final SegmentRegister oamSegment;

    private final SegmentRegister codeSegment;

    private final SegmentRegister dataSegment;

    @Inject
    public CpuSegFile(
        @CpuVar(ZP) SegmentRegister zeroPage,
        @CpuVar(SS) SegmentRegister stackSegment,
        @CpuVar(OS) SegmentRegister oamSegment,
        @CpuVar(CS) SegmentRegister codeSegment,
        @CpuVar(DS) SegmentRegister dataSegment
    ) {
        super("CPU.SEGS");

        this.zeroPage = zeroPage;
        this.stackSegment = stackSegment;
        this.oamSegment = oamSegment;
        this.codeSegment = codeSegment;
        this.dataSegment = dataSegment;
    }
}
