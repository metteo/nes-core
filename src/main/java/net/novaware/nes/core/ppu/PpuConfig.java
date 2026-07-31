package net.novaware.nes.core.ppu;

import net.novaware.nes.core.config.VideoStandard;

public interface PpuConfig { // TODO: move to .config package
    VideoStandard getVideoStandard();
}
