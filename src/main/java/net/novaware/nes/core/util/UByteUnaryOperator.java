package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Represents an operation on a single {@code @Unsigned ubyte}-valued operand that produces
 * an {@code @Unsigned byte}-valued result.  This is the primitive type specialization of
 * {@link java.util.function.UnaryOperator} for {@code @Unsigned byte}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsUByte(byte)}.
 *
 * @see java.util.function.IntUnaryOperator

 */
@FunctionalInterface
public interface UByteUnaryOperator {

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand
     * @return the operator result
     */
    @Unsigned byte applyAsUByte(@Unsigned byte operand);

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @return a unary operator that always returns its input argument
     */
    static UByteUnaryOperator identity() {
        return t -> t;
    }
}
