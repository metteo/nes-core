package net.novaware.nes.core.ppu.register

import spock.lang.Specification

class ObjAttrRegisterSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new ObjAttrRegister("OAM.PRI", 0x100)

        then:
        instance.getName() == "OAM.PRI"
        instance.toString() == "OAM.PRI: 0x00"
    }

    def "should mask secondary oam address by its size"() {
        given:
        def secOamAddr = new ObjAttrRegister("OAM.SEC", 0x10)

        when:
        secOamAddr.setAsByte(0xF + 5)

        then:
        secOamAddr.getAsInt() == 0x4
    }


}
