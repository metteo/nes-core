package net.novaware.nes.core.cpu.memory;

import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.register.ShortRegister;

/**
 * @see <a href="https://en.wikibooks.org/wiki/X86_Assembly/X86_Architecture">X86 Architecture</a>
 */
public class MemoryRegisters extends RegisterFile {

    // TODO: segment registers: Code Segment, Video Segment, Extra Segments configured by mappers for debug view
    //       e.g. where is nametable, where is pallete etc.

    SegmentRegister zeroPage = new SegmentRegister("ZP");

    SegmentRegister stackSegment = new SegmentRegister("SS");

    AddressRegister stackBase = new ShortRegister("BP");

    SegmentRegister codeSegment = new SegmentRegister("CS");

    SegmentRegister dataSegment = new SegmentRegister("DS");

    SegmentRegister extraSegment = new SegmentRegister("ES"); // extra data

    protected MemoryRegisters() {
        super("CPU_MEM");
    }
}
