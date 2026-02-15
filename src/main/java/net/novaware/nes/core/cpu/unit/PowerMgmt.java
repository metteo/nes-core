package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;

public class PowerMgmt implements Unit {

    @Inject
    public PowerMgmt () {

    }

    // Cold boot (power on), Warm boot (reset), Sleep states (idle loop, suspend, resume)
}
