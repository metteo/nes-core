package net.novaware.nes.core.util;

import net.novaware.nes.core.util.Bits.*;

/**
 * @author gemini
 */
class BitsTest {

    // region 8 bits :: byte :: [u8: 0 to 255] [s8: -128 to 127]

    @u8 byte  minU8OfByte  = 0;
    @u8 char  minU8OfChar  = 0;
    @u8 short minU8OfShort = 0;
    @u8 int   minU8OfInt   = 0;
    @u8 long  minU8OfLong  = 0L;

    @u8 byte  maxU8OfByte  = (byte) 255; // Fits as unsigned raw 0xFF
    @u8 char  maxU8OfChar  = 255;
    @u8 short maxU8OfShort = 255;
    @u8 int   maxU8OfInt   = 255;
    @u8 long  maxU8OfLong  = 255L;

    @s8 byte  minS8OfByte  = -128;
    @s8 char  minS8OfChar  = (char) -128;
    @s8 short minS8OfShort = -128;
    @s8 int   minS8OfInt   = -128;
    @s8 long  minS8OfLong  = -128L;

    @s8 byte  maxS8OfByte  = 127;
    @s8 char  maxS8OfChar  = 127;
    @s8 short maxS8OfShort = 127;
    @s8 int   maxS8OfInt   = 127;
    @s8 long  maxS8OfLong  = 127L;

    // endregion
    // region 16 bits :: short/char :: [u16: 0 to 65535] [s16: -32768 to 32767]

    @u16 short minU16OfShort = 0;
    @u16 char  minU16OfChar  = 0;
    @u16 int   minU16OfInt   = 0;
    @u16 long  minU16OfLong  = 0L;

    @u16 short maxU16OfShort = (short) 65535; // 0xFFFF
    @u16 char  maxU16OfChar  = 65535;
    @u16 int   maxU16OfInt   = 65535;
    @u16 long  maxU16OfLong  = 65535L;

    @s16 short minS16OfShort = -32768;
    @s16 char  minS16OfChar  = (char) -32768;
    @s16 int   minS16OfInt   = -32768;
    @s16 long  minS16OfLong  = -32768L;

    @s16 short maxS16OfShort = 32767;
    @s16 char  maxS16OfChar  = 32767;
    @s16 int   maxS16OfInt   = 32767;
    @s16 long  maxS16OfLong  = 32767L;

    // endregion
    // region 32 bits :: int :: [u32: 0 to 4294967295] [s32: -2147483648 to 2147483647]

    @u32 int  minU32OfInt  = 0;
    @u32 long minU32OfLong = 0L;

    @u32 int  maxU32OfInt  = (int) 4294967295L; // 0xFFFF_FFFF
    @u32 long maxU32OfLong = 4294967295L;

    @s32 int  minS32OfInt  = -2147483648;
    @s32 long minS32OfLong = -2147483648L;

    @s32 int  maxS32OfInt  = 2147483647;
    @s32 long maxS32OfLong = 2147483647L;

    // endregion
    // region 64 bits :: long :: [u64: 0 to Unsigned Max] [s64: Min Long to Max Long]

    @u64 long minU64OfLong = 0L;
    @u64 long maxU64OfLong = -1L; // Under standard two's complement, 0xFFFF_FFFF_FFFF_FFFFL evaluates to -1L

    @s64 long minS64OfLong = -9223372036854775808L; // Long.MIN_VALUE
    @s64 long maxS64OfLong = 9223372036854775807L;  // Long.MAX_VALUE

    // endregion
}
