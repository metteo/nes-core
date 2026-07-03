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
        v.setLayoutTable(lt)
        v.setFineY(fineY)
        v.setFineX(fineX)

        def assembledShort = ushort(assembled)

        t.set(assembledShort)

        expect:
        Bin.s(v.get()) == Bin.s(assembledShort)
        v.getFineX() == fineX

        t.getCoarseX() == coarseX
        t.getCoarseY() == coarseY
        t.getLayoutTable() == lt
        t.getFineY() == fineY

        where:
        assembled             || fineX | fineY | lt   | coarseY | coarseX
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
        v.getLayoutTable() == lt
        v.getFineY() == fineY

        where:
        hi          | lo          || fineX | fineY | lt   | coarseY | coarseX
        0b0000_0000 | 0b0000_0000 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b1000_0000 | 0b0000_0000 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0000_0000 | 0b0001_1111 || 0b000 | 0b000 | 0b00 | 0b00000 | 0b11111
        0b0000_0011 | 0b1110_0000 || 0b000 | 0b000 | 0b00 | 0b11111 | 0b00000
        0b0000_1100 | 0b0000_0000 || 0b000 | 0b000 | 0b11 | 0b00000 | 0b00000
        0b0111_0000 | 0b0000_0000 || 0b000 | 0b111 | 0b00 | 0b00000 | 0b00000
        0b0000_0000 | 0b0000_0000 || 0b111 | 0b000 | 0b00 | 0b00000 | 0b00000
        0b0111_1111 | 0b1111_1111 || 0b111 | 0b111 | 0b11 | 0b11111 | 0b11111
    }

    def "should get/set layout table x component separately"() {
        given:
        v.setLayoutTable(inLayoutTable)

        when:
        v.setLayoutTableX(layoutTableX)

        then:
        v.getLayoutTable() == outLayoutTable
        v.getLayoutTableX() == layoutTableX

        where:
        inLayoutTable | layoutTableX || outLayoutTable
        0b00          | 0b0          || 0b00
        0b00          | 0b1          || 0b01
        0b01          | 0b0          || 0b00
        0b01          | 0b1          || 0b01
    }

    def "should get/set layout table y component separately"() {
        given:
        v.setLayoutTable(inLayoutTable)

        when:
        v.setLayoutTableY(inLayoutTableY)

        then:
        v.getLayoutTable() == outLayoutTable
        v.getLayoutTableY() == outLayoutTableY

        where:
        inLayoutTable | inLayoutTableY || outLayoutTable | outLayoutTableY
        0b00          | 0b0            || 0b00           | 0b0
        0b00          | 0b1            || 0b10           | 0b1
        0b10          | 0b0            || 0b00           | 0b0
        0b10          | 0b1            || 0b10           | 0b1
    }

    def "should increment x"() {
        given:
        v.setLayoutTable(inLt)
        v.setFineX(inFineX)
        v.setCoarseX(inCoarseX)

        when:
        v.incrementX()

        then:
        v.getLayoutTable() == outLt
        v.getFineX() == outFineX
        v.getCoarseX() == outCoarseX

        // no wrapping to Y
        v.getFineY() == 0
        v.getCoarseY() == 0

        where:
         inLt | inFineX | inCoarseX || outLt | outFineX | outCoarseX | comment
         0b00 | 0b000   | 0b0_0000  || 0b00  | 0b000    | 0b0_0001   | "basic"
         0b00 | 0b000   | 0b1_1110  || 0b00  | 0b000    | 0b1_1111   | "before overflow to lt"
         0b00 | 0b000   | 0b1_1111  || 0b01  | 0b000    | 0b0_0000   | "overflow to lt 1"
         0b01 | 0b000   | 0b1_1111  || 0b00  | 0b000    | 0b0_0000   | "overflow to lt 0"
    }

    def "should increment y"() {
        given:
        v.setLayoutTable(inLt)
        v.setCoarseY(inCoarseY)
        v.setFineY(inFineY)

        when:
        v.incrementY()

        then:
        v.getLayoutTable() == outLt
        v.getCoarseY() == outCoarseY
        v.getFineY() == outFineY

        v.getFineX() == 0
        v.getCoarseX() == 0

        where:
        inLt | inCoarseY | inFineY || outLt | outCoarseY | outFineY | comment
        0b00 | 0b0_0000  | 0b000   || 0b00  | 0b0_0000   | 0b001    | "basic"
        0b00 | 0b0_0000  | 0b110   || 0b00  | 0b0_0000   | 0b111    | "before fine overflow"
        0b00 | 0b0_0000  | 0b111   || 0b00  | 0b0_0001   | 0b000    | "overflow to coarse"
        0b00 | 0b1_1101  | 0b110   || 0b00  | 0b1_1101   | 0b111    | "before coarse overflow"
        0b00 | 0b1_1101  | 0b111   || 0b10  | 0b0_0000   | 0b000    | "overflow to lt 1"
        0b10 | 0b1_1101  | 0b111   || 0b00  | 0b0_0000   | 0b000    | "overflow to lt 0"
        0b00 | 0b1_1111  | 0b111   || 0b00  | 0b0_0000   | 0b000    | "overflow without lt change"
    }

    def "should transfer X from T to V"() {
        given:
        t.setLayoutTableX(ltX)
        t.setCoarseX(coarseX)
        // t.fineX doesn't exist

        when:
        t.transferX(v)

        then:
        t.getLayoutTableX() == ltX
        t.getCoarseX() == coarseX

        v.getLayoutTableX() == ltX
        v.getCoarseX() == coarseX

        v.getFineX() == 0
        v.getFineY() == 0
        v.getCoarseY() == 0
        v.getLayoutTableY() == 0

        where:
        ltX | coarseX
        0b0 | 0b00000
        0b0 | 0b11111
        0b1 | 0b00000
        0b1 | 0b11111
    }

    def "should transfer Y from T to V"() {
        given:
        t.setLayoutTableY(ltY)
        t.setCoarseY(coarseY)
        t.setFineY(fineY)

        when:
        t.transferY(v)

        then:
        t.getLayoutTableY() == ltY
        t.getCoarseY() == coarseY
        t.getFineY() == fineY

        v.getLayoutTableY() == ltY
        v.getCoarseY() == coarseY
        v.getFineY() == fineY

        v.getFineX() == 0
        v.getCoarseX() == 0
        v.getLayoutTableX() == 0

        where:
        ltY | coarseY | fineY
        0b0 | 0b00000 | 0b000
        0b0 | 0b00000 | 0b111
        0b0 | 0b11111 | 0b000
        0b1 | 0b00000 | 0b000
        0b1 | 0b11111 | 0b111
    }

    def "should return provide useful VX toString"() {
        given:
        v.set(ushort(addr))
        v.setFineX(fX)

        expect:
        v.toString() == s

        where:
        addr   | fX  || s
        0x2345 | 3   || "PPU.VX: 0x2345  LT: 0  Y: 26.2  X: 5.3"
        0x3FFF | 7   || "PPU.VX: 0x3FFF  LT: 3  Y: 31.3  X: 31.7" // fY goes out of 14 bit range by 1 bit
    }

    def "should return provide useful T toString"() {
        given:
        t.set(ushort(addr))

        expect:
        t.toString() == s

        where:
        addr   || s
        0x2345 || "PPU.T: 0x2345  LT: 0  Y: 26.2  X: 5"
        0x3FFF || "PPU.T: 0x3FFF  LT: 3  Y: 31.3  X: 31" // fY goes out of 14 bit range by 1 bit
    }
}
