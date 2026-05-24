package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public final class SegmentRegister extends Register {

    private @Unsigned short start;
    private @Unsigned short end;

    // also cover mirrorring case?

    // add methods to get the end and possibly mirroring mask or sth

    public SegmentRegister(String name) {
        super(name);
    }

    public SegmentRegister setStart(@Unsigned short start) {
        this.start = start;
        return this;
    }

    public @Unsigned short getStart() {
        return start;
    }

    public int getStartAsInt() {
        return sint(start);
    }

    public SegmentRegister setEnd(@Unsigned short end) {
        this.end = end;
        return this;
    }

    public @Unsigned short getEnd() {
        return end;
    }
}
