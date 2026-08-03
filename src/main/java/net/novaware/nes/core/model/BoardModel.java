package net.novaware.nes.core.model;

import net.novaware.nes.core.config.Region;

public record BoardModel(
    String name,
    Region region,
    ClockModel clock,
    CpuModel cpu,
    PpuModel ppu
) {
    public double cpuFrequency() {
        return clock().frequency() / cpu().divider();
    }

    public double ppuFrequency() {
        return clock().frequency() / ppu().divider();
    }
}
