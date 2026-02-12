package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.util.Hex
import spock.lang.Specification

import static net.novaware.nes.core.util.Hex.s
import static net.novaware.nes.core.util.UnsignedTypes.ubyte

class ArithmeticLogicSpec extends Specification {

    CpuRegisters regs = new CpuRegisters()

    ArithmeticLogic alu = new ArithmeticLogic(regs)

    def "should bitwise or"() {
        given:
        regs.a().setAsByte(ubyte(a))

        when:
        alu.bitwiseOr(ubyte(operand))

        then:
        regs.a().get() == ubyte(expected)
        regs.status().zero == zero
        regs.status().negative == neg

        where:
        a           | operand     || expected    | zero  | neg
        0b0000_1111 | 0b1111_0000 || 0b1111_1111 | false | true
        0b0000_0000 | 0b0000_0000 || 0b0000_0000 | true  | false
        0b0101_0101 | 0b1010_1010 || 0b1111_1111 | false | true
    }

    def "should bitwise and"() {
        given:
        regs.a().setAsByte(ubyte(a))

        when:
        alu.bitwiseAnd(ubyte(operand))

        then:
        regs.a().get() == ubyte(expected)
        regs.status().zero == zero
        regs.status().negative == neg

        where:
        a           | operand     || expected    | zero  | neg
        0b1111_1111 | 0b1010_1010 || 0b1010_1010 | false | true
        0b1111_0000 | 0b0000_1111 || 0b0000_0000 | true  | false
        0b0000_1111 | 0b0000_0111 || 0b0000_0111 | false | false
    }

    def "should rotate left"() {
        given:
        regs.status().carry = inCarry

        expect:
        alu.rotateLeft(ubyte(inData)) == ubyte(expected)
        regs.status().carry == carry
        regs.status().negative == neg
        regs.status().zero == zero

        where:
        inCarry | inData      || expected    | carry | neg   | zero
        0       | 0b0000_1111 || 0b0001_1110 | false | false | false
        1       | 0b0000_1111 || 0b0001_1111 | false | false | false
        0       | 0b1000_0000 || 0b0000_0000 | true  | false | true
        1       | 0b1000_0001 || 0b0000_0011 | true  | false | false
    }

    def "should rotate right"() {
        given:
        regs.status().carry = inCarry

        expect:
        alu.rotateRight(ubyte(inData)) == ubyte(expected)
        regs.status().carry == carry as boolean
        regs.status().negative == neg as boolean
        regs.status().zero == zero

        where:
        inCarry | inData      || neg | expected    | carry | zero
              0 | 0b1111_0000 ||   0 | 0b0111_1000 | 0     | false
              1 | 0b1111_0000 ||   1 | 0b1111_1000 | 0     | false
              0 | 0b0000_0001 ||   0 | 0b0000_0000 | 1     | true
              1 | 0b1000_0000 ||   1 | 0b1100_0000 | 0     | false
    }
    
    def "should add with carry"() {
        given:
        regs.a().setAsByte(ubyte(a))
        regs.status().carry = inCarry

        when:
        alu.addWithCarry(ubyte(data))

        then:
        s(regs.a().get()) == Hex.s(ubyte(expected))
        regs.status().carry == carry as boolean
        regs.status().overflow == overflow as boolean
        regs.status().zero == zero
        regs.status().negative == neg

        where:
        a    | inCarry | data || expected | neg   | zero  | carry | overflow
        0x01 | 1       | 0x02 || 0x04     | false | false | 0     | 0 // Unsigned: 1 + 2 = 3, basic
        0xFF | 1       | 0xFF || 0xFF     | true  | false | 1     | 0 // Signed: -1 + -1 + 1 = -1
        0x7F | 0       | 0x01 || 0x80     | true  | false | 0     | 1 // Signed: 127 + 1 = 128 (negative)
        0x80 | 0       | 0xFF || 0x7F     | false | false | 1     | 1 // Signed: -128 + -1 = -129 (positive)
        0x80 | 0       | 0x80 || 0x00     | false | true  | 1     | 1 // Signed: -128 + -128 = -256 (zero)
    }

    def "should subtract with borrow"() {
        given:
        regs.a().setAsByte(ubyte(a))
        regs.status().setBorrow(inBorrow as boolean)

        when:
        alu.subtractWithBorrow(ubyte(data))

        then:
        s(regs.a().get()) == s(ubyte(expected))
        regs.status().getBorrow() == borrow as boolean
        regs.status().isOverflow() == overflow as boolean
        regs.status().isZero() == zero
        regs.status().isNegative() == neg

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
}
