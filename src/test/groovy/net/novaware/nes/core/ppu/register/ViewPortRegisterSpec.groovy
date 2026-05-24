package net.novaware.nes.core.ppu.register

import net.novaware.nes.core.ppu.inject.PpuRegModule
import net.novaware.nes.core.util.Bin
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ViewPortRegisterSpec extends Specification {

    def v = PpuRegModule.provideCurrentViewPort()
    def t = PpuRegModule.provideTempViewPort()

    def "should transform between assembled address and partials"() {
        given:
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)
        v.setNameTable(nt)
        v.setFineY(fineY)
        v.setFineX(fineX)

        def assembledShort = ushort(assembled)

        t.set(assembledShort)

        expect:
        Bin.s(v.get()) == Bin.s(assembledShort)
        v.getFineX() == fineX

        t.getCoarseX() == coarseX
        t.getCoarseY() == coarseY
        t.getNameTable() == nt
        t.getFineY() == fineY

        where:
        assembled             || fineX | fineY | nt   | coarseY | coarseX
        0b0000_0000_0000_0000 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0000_0000_0001_1111 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b11111
        0b0000_0011_1110_0000 || 0b000 | 0b000 | 0b00 | 0b11111 | 0b00000
        0b0000_1100_0000_0000 || 0b000 | 0b000 | 0b11 | 0b00000 | 0b00000
        0b0111_0000_0000_0000 || 0b000 | 0b111 | 0b00 | 0b00000 | 0b00000
        0b0000_0000_0000_0000 || 0b111 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0111_1111_1111_1111 || 0b111 | 0b111 | 0b11 | 0b11111 | 0b11111
    }

    def "should throw when #method ting fineX in T register"() {
        when:
        switch(method) {
            case "get" -> t.getFineX()
            case "set" -> t.setFineX(42)
        }

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "T variant doesn't have fineX component"

        where:
        method << ["get", "set"]
    }

    def "should set high and low bytes separately"() {
        when:
        v.setFineX(fineX)

        v.high(ubyte(hi))
        v.low(ubyte(lo))

        then:
        v.getFineX() == fineX

        v.getCoarseX() == coarseX
        v.getCoarseY() == coarseY
        v.getNameTable() == nt
        v.getFineY() == fineY

        where:
        hi          | lo          || fineX | fineY | nt   | coarseY | coarseX
        0b0000_0000 | 0b0000_0000 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b1000_0000 | 0b0000_0000 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0000_0000 | 0b0001_1111 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b11111
        0b0000_0011 | 0b1110_0000 || 0b000 | 0b000 | 0b00 | 0b11111 | 0b00000
        0b0000_1100 | 0b0000_0000 || 0b000 | 0b000 | 0b11 | 0b00000 | 0b00000
        0b0111_0000 | 0b0000_0000 || 0b000 | 0b111 | 0b00 | 0b00000 | 0b00000
        0b0000_0000 | 0b0000_0000 || 0b111 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0111_1111 | 0b1111_1111 || 0b111 | 0b111 | 0b11 | 0b11111 | 0b11111
    }
}
