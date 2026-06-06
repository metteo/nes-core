package net.novaware.nes.core.config;

import net.novaware.nes.core.board.Board;
import net.novaware.nes.core.cpu.CpuConfig;
import net.novaware.nes.core.ppu.PpuConfig;

public interface CoreConfig extends Board.Config, CpuConfig, PpuConfig { // TODO: consider NesCoreConfig name

}
