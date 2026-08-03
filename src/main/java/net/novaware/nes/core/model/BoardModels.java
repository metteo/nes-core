package net.novaware.nes.core.model;

import net.novaware.nes.core.config.Region;

public interface BoardModels {

    BoardModel NES = new BoardModel(
        "NES",
        Region.USA,
        ClockModels.NTSC,
        CpuModels.RP2A03,
        PpuModels.RP2C02
    );

    BoardModel NES_RGB = new BoardModel(
        "NES RGB",
        Region.USA,
        ClockModels.NTSC,
        CpuModels.RP2A03,
        PpuModels.RP2C03
    );

    BoardModel FAMICOM = new BoardModel(
        "Family Computer",
        Region.JAPAN,
        ClockModels.NTSC,
        CpuModels.RP2A03,
        PpuModels.RP2C02
    );

    BoardModel NES_EU = new BoardModel(
        "NES EU",
        Region.EUROPE,
        ClockModels.PAL,
        CpuModels.RP2A07,
        PpuModels.RP2C07
    );

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Dendy">Dendy</a>
     */
    BoardModel DENDY = new BoardModel(
        "Dendy",
        Region.RUSSIA,
        ClockModels.PAL,
        CpuModels.UA6527P,
        PpuModels.UA6538
    );

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Pegasus_(console)">Pegasus</a>
     */
    BoardModel PEGASUS = new BoardModel(
        "Pegasus",
        Region.POLAND,
        ClockModels.PAL,
        CpuModels.UA6527P,
        PpuModels.UA6538
    );

    BoardModel PHANTOM_SYSTEM = new BoardModel(
        "Phantom System",
        Region.BRAZIL,
        ClockModels.PALM,
        CpuModels.UA6527,
        PpuModels.UA6548
    );

    BoardModel SUPER_BITGAME = new BoardModel(
        "Super Bitgame",
        Region.ARGENTINA,
        ClockModels.PALN,
        CpuModels.UA6527,
        PpuModels.UA6528P
    );
}
