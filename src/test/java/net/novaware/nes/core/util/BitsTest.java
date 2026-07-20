package net.novaware.nes.core.util;

import net.novaware.nes.core.util.Bits.*;

/**
 * @author gemini
 */
class BitsTest {

    // region 8 bits :: byte

    @u8 int   minU8Int   = 0;
    @u8 long  minU8Long  = 0L;

    @u8 int   maxU8Int   = 0xFF;
    @u8 long  maxU8Long  = 0xFFL;

    @s8 int   minS8Int   = Byte.MIN_VALUE;
    @s8 long  minS8Long  = Byte.MIN_VALUE;

    @s8 int   maxS8Int   = Byte.MAX_VALUE;
    @s8 long  maxS8Long  = Byte.MAX_VALUE;

    // endregion
    // region 16 bits :: short/char

    @u16 int   minU16Int   = 0;
    @u16 long  minU16Long  = 0L;

    @u16 int   maxU16Int   = 0xFFFF;
    @u16 long  maxU16Long  = 0xFFFFL;

    @s16 int   minS16Int   = Short.MIN_VALUE;
    @s16 long  minS16Long  = Short.MIN_VALUE;

    @s16 int   maxS16Int   = Short.MAX_VALUE;
    @s16 long  maxS16Long  = Short.MAX_VALUE;

    // endregion
    // region 32 bits :: int

    @u32 int  minU32Int  = 0;
    @u32 long minU32Long = 0L;

    @u32 int  maxU32Int  = (int) 0xFFFF_FFFFL;
    @u32 long maxU32Long = 0xFFFF_FFFFL;

    @s32 int  minS32Int  = Integer.MIN_VALUE;
    @s32 long minS32Long = Integer.MIN_VALUE;

    @s32 int  maxS32Int  = Integer.MAX_VALUE;
    @s32 long maxS32Long = Integer.MAX_VALUE;

    // endregion
    // region 64 bits :: long

    @u64 long minU64Long = 0L;
    @u64 long maxU64Long = -1L; // 0xFFFF_FFFF_FFFF_FFFFL == -1L

    @s64 long minS64Long = Long.MIN_VALUE;
    @s64 long maxS64Long = Long.MAX_VALUE;

    // endregion
}
