package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.memory.RecordingBus
import net.novaware.nes.core.register.DataRegister
import net.novaware.nes.core.util.RegsAndRamBaseSpec
import spock.lang.Subject

import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte

class ArithmeticLogicSpec extends RegsAndRamBaseSpec<RecordingBus> {

    @Subject
    ArithmeticLogic alu = factory.newArithmeticLogic()

    def "should bitwise or"() {
        given:
        regs a: aIn, do: operand

        when:
        alu.bitwiseOr()

        then:
        expectRegs a: aOut, z: zero, n: neg as boolean

        where:
        aIn         | operand     || neg | aOut        | zero
        0b0000_1111 | 0b1111_0000 ||   1 | 0b1111_1111 | false
        0b0000_0000 | 0b0000_0000 ||   0 | 0b0000_0000 | true
        0b0101_0101 | 0b1010_1010 ||   1 | 0b1111_1111 | false
        0b0000_0001 | 0b0000_0010 ||   0 | 0b0000_0011 | false
    }

    def "should bitwise and"() {
        given:
        regs a: a

        when:
        alu.bitwiseAnd(ubyte(operand))

        then:
        expectRegs(
            a: expected,
            z: zero,
            n: neg
        )

        where:
        a           | operand     || expected    | zero  | neg
        0b1111_1111 | 0b1010_1010 || 0b1010_1010 | false | true
        0b1111_0000 | 0b0000_1111 || 0b0000_0000 | true  | false
        0b0000_1111 | 0b0000_0111 || 0b0000_0111 | false | false
    }

    def "should bitwise xor"() {
        given:
        regs a: a

        when:
        alu.bitwiseXor(ubyte(operand))

        then:
        expectRegs(
            a: expected,
            z: zero,
            n: neg
        )

        where:
        a           | operand     || expected    | zero  | neg
        0b0101_0101 | 0b1010_1010 || 0b1111_1111 | false | true
        0b0000_0000 | 0b0000_0001 || 0b0000_0001 | false | false
        0b0000_1111 | 0b0000_1111 || 0b0000_0000 | true  | false
    }

    def "should rotate left"() {
        given:
        regs c: inCarry

        expect:
        alu.rotateLeft(ubyte(inData)) == ubyte(expected)
        expectRegs(
            c: carry,
            z: zero,
            n: neg
        )

        where:
        inCarry | inData      || expected    | carry | neg   | zero
        0       | 0b0000_1111 || 0b0001_1110 | false | false | false
        1       | 0b0000_1111 || 0b0001_1111 | false | false | false
        0       | 0b1000_0000 || 0b0000_0000 | true  | false | true
        1       | 0b1000_0001 || 0b0000_0011 | true  | false | false
    }

    def "should rotate right"() {
        given:
        regs c: inCarry

        expect:
        alu.rotateRight(ubyte(inData)) == ubyte(expected)

        expectRegs(
            c: carry as boolean,
            z: zero,
            n: neg as boolean
        )

        where:
        inCarry | inData      || neg | expected    | carry | zero
              0 | 0b1111_0000 ||   0 | 0b0111_1000 | 0     | false
              1 | 0b1111_0000 ||   1 | 0b1111_1000 | 0     | false
              0 | 0b0000_0001 ||   0 | 0b0000_0000 | 1     | true
              1 | 0b1000_0000 ||   1 | 0b1100_0000 | 0     | false
    }
    
    def "should add with carry"() {
        given:
        regs(
            a: a,
            c: inCarry
        )

        when:
        alu.addWithCarry(ubyte(data))

        then:
        expectRegs(
            a: expected,
            c: carry as boolean,
            v: ov as boolean,
            z: zero,
            n: neg
        )

        where: // TODO: change other data tests to include a comment column instead of code comment
        a    | inCarry | data || expected | neg   | zero  | carry | ov | comment
        0x01 | 1       | 0x02 || 0x04     | false | false | 0     | 0  | "Unsigned: 1 + 2 = 3, basic"
        0xFF | 1       | 0xFF || 0xFF     | true  | false | 1     | 0  | "Signed: -1 + -1 + 1 = -1"
        0x7F | 0       | 0x01 || 0x80     | true  | false | 0     | 1  | "Signed: 127 + 1 = 128 (negative)"
        0x80 | 0       | 0xFF || 0x7F     | false | false | 1     | 1  | "Signed: -128 + -1 = -129 (positive)"
        0x80 | 0       | 0x80 || 0x00     | false | true  | 1     | 1  | "Signed: -128 + -128 = -256 (zero)"
    }

    def "should subtract with borrow"() {
        given:
        regs(
            a: a,
            b: inBorrow
        )

        when:
        alu.subtractWithBorrow(ubyte(data))

        then:
        expectRegs(
            a: expected,
            b: borrow as boolean,
            v: overflow as boolean,
            z: zero,
            n: neg
        )

        where:
        a    | inBorrow | data || expected | neg   | zero  | borrow | overflow
        0x05 | 0        | 0x03 || 0x02     | false | false | 0      | 0 // Basic: 5 - 3 = 2 (No borrow)
        0x08 | 1        | 0x02 || 0x05     | false | false | 0      | 0 // Prev borrow: 8 - 2 - 1 = 5 (No new borrow)
        0x05 | 0        | 0x05 || 0x00     | false | true  | 0      | 0 // Result is zero: 5 - 5 = 0
        0x00 | 0        | 0x01 || 0xFF     | true  | false | 1      | 0 // Trigger a borrow: 0 - 1 = 255 (Borrow occurs)
        0x7F | 0        | 0xFF || 0x80     | true  | false | 1      | 1 // Positive - Negative = Overflow: 127 - (-1) = 128
        0x80 | 0        | 0x01 || 0x7F     | false | false | 0      | 1 // Negative - Positive = Overflow: -128 - 1 = -129
        0xFF | 0        | 0xFF || 0x00     | false | true  | 0      | 0 // Signed: -1 - -1 = 0 (using 0xFF - 0xFF)
    }

    def "should increment memory"() {
        when:
        byte result = alu.incrementMemory(ubyte(data))

        then:
        sint(result) == expected

        expectRegs(
            z: zero,
            n: neg
        )

        where:
        data || expected | neg   | zero
        0x00 || 0x01     | false | false
        0xFF || 0x00     | false | true
        0x7F || 0x80     | true  | false // 127 + 1 = -128
    }

    def "should decrement memory"() {
        when:
        byte result = alu.decrementMemory(ubyte(data))

        then:
        sint(result) == expected

        expectRegs(
            z: zero,
            n: neg
        )

        where:
        data || expected | neg   | zero
        0x02 || 0x01     | false | false
        0x01 || 0x00     | false | true
        0x00 || 0xFF     | true  | false // 0 - 1 = -1
    }

    def "should increment index register"() {
        given:
        DataRegister register = switch(reg) {
            case "x" -> registers.indexX
            case "y" -> registers.indexY
        }
        Runnable aluMethod = switch(method) {
            case "incrementX" -> alu::incrementX
            case "incrementY" -> alu::incrementY
            case "decrementX" -> alu::decrementX
            case "decrementY" -> alu::decrementY
        }

        when:
        register.setAsByte(data)
        aluMethod.run()
        int result = register.getAsInt()

        then:
        result == expected
        expectRegs(
            z: zero,
            n: neg
        )

        where:
        reg | data | method       || expected | neg   | zero
        "x" | 0x00 | "incrementX" || 0x01     | false | false
        "x" | 0xFF | "incrementX" || 0x00     | false | true
        "x" | 0x7F | "incrementX" || 0x80     | true  | false

        "y" | 0x00 | "incrementY" || 0x01     | false | false
        "y" | 0xFF | "incrementY" || 0x00     | false | true
        "y" | 0x7F | "incrementY" || 0x80     | true  | false

        "x" | 0x00 | "decrementX" || 0xFF     | true  | false
        "x" | 0x01 | "decrementX" || 0x00     | false | true
        "x" | 0x20 | "decrementX" || 0x1F     | false | false

        "y" | 0x00 | "decrementY" || 0xFF     | true  | false
        "y" | 0x01 | "decrementY" || 0x00     | false | true
        "y" | 0x20 | "decrementY" || 0x1F     | false | false
    }

    def "should compare register with memory"() {
        when: "accumulator"
        registers.status().initialize() // TODO: maybe should be part of factory

        registers.a().setAsByte(reg)
        alu.compareA(ubyte(data))

        then:
        expectRegs(
            a: reg, // no change
            b: borrow,
            z: zero,
            n: neg
        )

        and: "index x"
        registers.status().initialize()

        regs x: reg
        alu.compareX(ubyte(data))

        then:
        expectRegs(
            x: reg, // no change
            b: borrow,
            z: zero,
            n: neg
        )

        and: "index y"
        registers.status().initialize()

        regs y: reg
        alu.compareY(ubyte(data))

        then:
        expectRegs(
            y: reg, // no change
            b: borrow,
            z: zero,
            n: neg
        )

        where:
        reg  | data || borrow | zero  | neg
        0x25 | 0x25 || false  | true  | false // 0x25 - 0x25 = 0x00 -> basic
        0x00 | 0x00 || false  | true  | false // 0x00 - 0x00 = 0x00 -> basic with 0s
        0x05 | 0x06 || true   | false | true  // 0x05 - 0x06 = -1
        0x05 | 0x03 || false  | false | false // all false
        0x81 | 0x01 || false  | false | true  // neg only
        0x00 | 0xFF || true   | false | false // borrow only
        0x00 | 0xFF || true   | false | false // 0−255=−255. Bit 7 of the 8-bit result (0x01) is 0.
        0x7F | 0x80 || true   | false | true  // 127−128=−1. Bit 7 of the 8-bit result (0xFF) is 1.
        0xFE | 0xFF || true   | false | true  // 254−255=−1. Result is 0xFF.
        0x80 | 0x00 || false  | false | true  // 128−0=128. Result is 0x80.
    }

    def "should bit test"() {
        given:
        regs a: a

        when:
        alu.bitTest(ubyte(data))

        then:
        expectRegs(
            a: a, // no change
            z: zero,
            v: overflow,
            n: neg
        )

        where:
        a           | data        || zero  | neg    | overflow
        0b0000_0000 | 0b0000_0000 || true  | false  | false
        0b0010_0000 | 0b0001_0000 || true  | false  | false
        0b0000_0001 | 0b0000_0001 || false | false  | false
        0b0010_0000 | 0b0010_0000 || false | false  | false
        0b0100_0000 | 0b0100_0000 || false | false  | true
        0b1000_0000 | 0b1000_0000 || false | true   | false
        0b1100_0000 | 0b1100_0000 || false | true   | true
        0b0000_0000 | 0b1100_0000 || true  | true   | true
    }

    def "should shift left (arithmetic)"() {
        when:
        def result = alu.arithmeticShiftLeft(ubyte(data))

        then:
        sint(result) == expected
        expectRegs(
            c: carry as boolean,
            z: zero,
            n: neg
        )

        where:
        data        || carry | expected    | zero  | neg
        0b1111_0000 || 1     | 0b1110_0000 | false | true
        0b0000_1111 || 0     | 0b0001_1110 | false | false
        0b1000_0000 || 1     | 0b0000_0000 | true  | false
    }

    def "should shift right (logical)"() {
        when:
        def result = alu.logicalShiftRight(ubyte(data))

        then:
        sint(result) == expected

        expectRegs(
            c: carry as boolean,
            z: zero,
            n: false
        )

        where:
        data        || expected    | carry | zero
        0b1111_0000 || 0b0111_1000 | 0     | false
        0b0000_1111 || 0b0000_0111 | 1     | false
        0b0000_0001 || 0b0000_0000 | 1     | true
    }
}
