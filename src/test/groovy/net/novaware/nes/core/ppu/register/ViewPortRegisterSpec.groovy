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

    def "should get/set name table x component separately"() {
        given:
        v.setNameTable(inNameTable)

        when:
        v.setNameTableX(nameTableX)

        then:
        v.getNameTable() == outNameTable
        v.getNameTableX() == nameTableX

        where:
        inNameTable | nameTableX || outNameTable
        0b00        | 0b0          || 0b00
        0b00        | 0b1          || 0b01
        0b01        | 0b0          || 0b00
        0b01        | 0b1          || 0b01
    }

    def "should get/set name table y component separately"() {
        given:
        v.setNameTable(inNameTable)

        when:
        v.setNameTableY(inNameTableY)

        then:
        v.getNameTable() == outNameTable
        v.getNameTableY() == outNameTableY

        where:
        inNameTable | inNameTableY || outNameTable | outNameTableY
        0b00        | 0b0          || 0b00         | 0b0
        0b00        | 0b1          || 0b10         | 0b1
        0b10        | 0b0          || 0b00         | 0b0
        0b10        | 0b1          || 0b10         | 0b1
    }

    def "should increment x"() {
        given:
        v.setNameTable(inNt)
        v.setFineX(inFineX)
        v.setCoarseX(inCoarseX)

        when:
        v.incrementX()

        then:
        v.getNameTable() == outNt
        v.getFineX() == outFineX
        v.getCoarseX() == outCoarseX

        // no wrapping to Y
        v.getFineY() == 0
        v.getCoarseY() == 0

        where:
         inNt | inFineX | inCoarseX || outNt | outFineX | outCoarseX | comment
         0b00 | 0b000   | 0b0_0000  || 0b00  | 0b000    | 0b0_0001   | "basic"
         0b00 | 0b000   | 0b0_1110  || 0b00  | 0b000    | 0b0_1111   | "before overflow to nt"
         0b00 | 0b000   | 0b0_1111  || 0b01  | 0b000    | 0b0_0000   | "overflow to nt 1"
         0b01 | 0b000   | 0b0_1111  || 0b00  | 0b000    | 0b0_0000   | "overflow to nt 0"
    }

    def "should increment y"() {
        given:
        v.setNameTable(inNt)
        v.setCoarseY(inCoarseY)
        v.setFineY(inFineY)

        when:
        v.incrementY()

        then:
        v.getNameTable() == outNt
        v.getCoarseY() == outCoarseY
        v.getFineY() == outFineY

        v.getFineX() == 0
        v.getCoarseX() == 0

        where:
        inNt | inCoarseY | inFineY || outNt | outCoarseY | outFineY | comment
        0b00 | 0b0_0000  | 0b000   || 0b00  | 0b0_0000   | 0b001    | "basic"
        0b00 | 0b0_0000  | 0b110   || 0b00  | 0b0_0000   | 0b111    | "before fine overflow"
        0b00 | 0b0_0000  | 0b111   || 0b00  | 0b0_0001   | 0b000    | "overflow to coarse"
        0b00 | 0b1_1111  | 0b110   || 0b00  | 0b1_1111   | 0b111    | "before coarse overflow"
        0b00 | 0b1_1111  | 0b111   || 0b10  | 0b0_0000   | 0b000    | "overflow to nt 1"
        0b10 | 0b1_1111  | 0b111   || 0b00  | 0b0_0000   | 0b000    | "overflow to nt 0"
    }

    def "should transfer X from T to V"() {
        given:
        t.setNameTableX(ntX)
        t.setCoarseX(coarseX)
        // t.fineX doesn't exist

        when:
        t.transferX(v)

        then:
        t.getNameTableX() == ntX
        t.getCoarseX() == coarseX

        v.getNameTableX() == ntX
        v.getCoarseX() == coarseX

        v.getFineX() == 0
        v.getFineY() == 0
        v.getCoarseY() == 0
        v.getNameTableY() == 0

        where:
        ntX | coarseX
        0b0 | 0b00000
        0b0 | 0b11111
        0b1 | 0b00000
        0b1 | 0b11111
    }

    def "should transfer Y from T to V"() {
        given:
        t.setNameTableY(ntY)
        t.setCoarseY(coarseY)
        t.setFineY(fineY)

        when:
        t.transferY(v)

        then:
        t.getNameTableY() == ntY
        t.getCoarseY() == coarseY
        t.getFineY() == fineY

        v.getNameTableY() == ntY
        v.getCoarseY() == coarseY
        v.getFineY() == fineY

        v.getFineX() == 0
        v.getCoarseX() == 0
        v.getNameTableX() == 0

        where:
        ntY | coarseY | fineY
        0b0 | 0b00000 | 0b000
        0b0 | 0b00000 | 0b111
        0b0 | 0b11111 | 0b000
        0b1 | 0b00000 | 0b000
        0b1 | 0b11111 | 0b111
    }

    def "should return name table address"() {
        given:
        v.setNameTable(nt)
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)

        expect:
        v.getNameTableAddress() == ushort(ntAddr)

        where:
        nt   | coarseY | coarseX || ntAddr
        0b00 | 0b00000 | 0b00000 || 0b10_00_00000_00000
        0b00 | 0b00000 | 0b11111 || 0b10_00_00000_11111
        0b00 | 0b11111 | 0b00000 || 0b10_00_11111_00000
        0b11 | 0b00000 | 0b00000 || 0b10_11_00000_00000
        0b11 | 0b11111 | 0b11111 || 0b10_11_11111_11111 // TODO: seems like attribute table? sus
    }

    def "should return attr table address"() {
        given:
        v.setNameTable(nt)
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)


        expect:
        v.getAttrTableAddress() == ushort(atAddr)

        where:
        nt   | coarseY  | coarseX  || atAddr
        0b00 | 0b000_00 | 0b000_00 || 0b10_00_1111_000_000
        0b00 | 0b000_00 | 0b111_00 || 0b10_00_1111_000_111
        0b00 | 0b111_00 | 0b000_00 || 0b10_00_1111_111_000
        0b11 | 0b000_00 | 0b000_00 || 0b10_11_1111_000_000
        0b11 | 0b111_00 | 0b111_00 || 0b10_11_1111_111_111
    }
}
