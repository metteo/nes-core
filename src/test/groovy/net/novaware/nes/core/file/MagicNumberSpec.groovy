package net.novaware.nes.core.file

import spock.lang.Specification

import static MagicNumber.GAME_FDS
import static MagicNumber.GAME_NES
import static MagicNumber.GAME_UNIF
import static MagicNumber.MUSIC_NSF_CLASSIC
import static MagicNumber.MUSIC_NSF_EXTENDED
import static MagicNumber.MUSIC_NSF_MODERN
import static MagicNumber.PATCH_BPS
import static MagicNumber.PATCH_IPS
import static MagicNumber.PATCH_UPS

class MagicNumberSpec extends Specification {

    def "should have correct bytes in magic numbers" () {
        expect:
        GAME_NES.numbers()  == new byte[]{ 0x4E, 0x45, 0x53, 0x1A }
        GAME_FDS.numbers()  == new byte[]{ 0x46, 0x44, 0x53, 0x1A }
        GAME_UNIF.numbers() == new byte[]{ 0x55, 0x4E, 0x49, 0x46 }

        PATCH_IPS.numbers() == new byte[]{ 0x50, 0x41, 0x54, 0x43, 0x48 }
        PATCH_BPS.numbers() == new byte[]{ 0x42, 0x50, 0x53, 0x31 }
        PATCH_UPS.numbers() == new byte[]{ 0x55, 0x50, 0x53, 0x31 }

        MUSIC_NSF_CLASSIC.numbers()  == new byte[]{ 0x4E, 0x45, 0x53, 0x4D, 0x1A, 0x01 }
        MUSIC_NSF_MODERN.numbers()   == new byte[]{ 0x4E, 0x45, 0x53, 0x4D, 0x1A, 0x02 }
        MUSIC_NSF_EXTENDED.numbers() == new byte[]{ 0x4E, 0x53, 0x46, 0x65 }
    }
    
    def "should return 0-100% match of magic numbers" () {
        given:
        byte[] noMatch      = [0x50, 0x4B, 0x03, 0x04      ] as byte[] // ZIP magic
        byte[] partialMatch = [0x4E, 0x45, 0x58, 0x1A      ] as byte[] // NEX^
        byte[] shortMatch   = [0x4E, 0x45                  ] as byte[]
        byte[] fullMatch    = [0x4E, 0x45, 0x53, 0x1A      ] as byte[]
        byte[] longMatch    = [0x4E, 0x45, 0x53, 0x1A, 0x00] as byte[]

        expect:
        GAME_NES.matchesPartially(noMatch) == 0
        GAME_NES.matchesPartially(partialMatch) == 75 // 'NE_^' matches
        GAME_NES.matchesPartially(shortMatch) == 50 // 'NE' matches
        GAME_NES.matchesPartially(fullMatch) == 100
        GAME_NES.matches(fullMatch)
        GAME_NES.matchesPartially(longMatch) == 100 // full magic bytes match, extra bytes ignored
        GAME_NES.matches(longMatch)
    }
}
