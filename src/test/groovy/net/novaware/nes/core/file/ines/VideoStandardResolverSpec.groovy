package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesMeta
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC_DUAL
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL_DUAL
import static net.novaware.nes.core.file.NesMeta.VideoStandard.UNKNOWN

class VideoStandardResolverSpec extends Specification {

    VideoStandardResolver resolver

    def setup() {
        resolver = new VideoStandardResolver()
    }

    def "should resolve different combinations of video standard params" () {
        when:
        NesMeta.VideoStandard actual = resolver.resolve(basic, extended)

        then:
        actual == expected

        where:
        basic   | extended  | expected
        NTSC    | NTSC      | NTSC
        PAL     | PAL       | PAL

        // promote to dual
        NTSC    | NTSC_DUAL | NTSC_DUAL
        PAL     | PAL_DUAL  | PAL_DUAL

        // prefer official byte 9 over unofficial byte 10
        NTSC    | PAL       | NTSC
        PAL     | NTSC      | PAL

        // allow dual, user will decide if works in the other
        NTSC    | PAL_DUAL  | NTSC_DUAL
        PAL     | NTSC_DUAL | PAL_DUAL

        // no extended bit, fallback to basic
        NTSC    | UNKNOWN   | NTSC
        PAL     | UNKNOWN   | PAL

        // force user selection, default to NTSC in UI
        UNKNOWN | UNKNOWN   | UNKNOWN
    }

    // TODO: create tests for validation
}
