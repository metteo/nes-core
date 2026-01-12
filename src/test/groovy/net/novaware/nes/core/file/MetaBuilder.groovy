package net.novaware.nes.core.file;

import net.novaware.nes.core.file.NesMeta.Layout;
import net.novaware.nes.core.file.NesMeta.VideoStandard
import net.novaware.nes.core.test.TestDataBuilder
import net.novaware.nes.core.util.QuantityBuilder

import static net.novaware.nes.core.file.NesMeta.System.NES
import static net.novaware.nes.core.file.NesMeta.System.PLAY_CHOICE_10
import static net.novaware.nes.core.file.ProgramMemoryBuilder.battery8kb
import static net.novaware.nes.core.file.ProgramMemoryBuilder.none
import static net.novaware.nes.core.util.QuantityBuilder.banks16kb
import static net.novaware.nes.core.util.QuantityBuilder.banks512b
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb
import static net.novaware.nes.core.util.QuantityBuilder.bytes

class MetaBuilder implements TestDataBuilder<NesMeta> {

    private String title
    private String info

    private NesMeta.System system

    private short mapper
    private boolean busConflicts

    private ProgramMemoryBuilder programMemory
    private QuantityBuilder trainer
    private QuantityBuilder programData

    private QuantityBuilder videoMemory
    private VideoDataBuilder videoData
    private VideoStandard videoStandard

    private QuantityBuilder footer

    static MetaBuilder marioBros() {
        new MetaBuilder().title("Mario Bros.")
                .info("DiskDude!")
                .system(NES)
                .mapper(0)
                .busConflicts(false)
                .trainer(banks512b(0))
                .programMemory(none())
                .programData(banks16kb(1))
                .videoMemory(banks8kb(0))
                .videoData(VideoDataBuilder.vertical(1))
                .videoStandard(VideoStandard.NTSC)
                .footer(bytes(0))
    }

    static MetaBuilder complexMeta() {
        new MetaBuilder().title("Complex")
                .info("astra")
                .system(PLAY_CHOICE_10)
                .mapper(252)
                .busConflicts(true)
                .trainer(banks512b(1))
                .programMemory(battery8kb(3))
                .programData(banks16kb(5))
                .videoMemory(banks8kb(7))
                .videoData(VideoDataBuilder.videoData(Layout.ALTERNATIVE_HORIZONTAL, 9))
                .videoStandard(VideoStandard.PAL)
                .footer(bytes(127))
    }

    MetaBuilder title(String title) {
        this.title = title;
        return this;
    }

    MetaBuilder info(String info) {
        this.info = info;
        return this;
    }

    MetaBuilder system(NesMeta.System system) {
        this.system = system;
        return this;
    }

    MetaBuilder mapper(int mapper) {
        this.mapper = (short) mapper;
        return this;
    }

    MetaBuilder busConflicts(boolean busConflicts) {
        this.busConflicts = busConflicts;
        return this;
    }

    MetaBuilder trainer(QuantityBuilder trainer) {
        this.trainer = trainer;
        return this;
    }

    MetaBuilder programMemory(ProgramMemoryBuilder programMemory) {
        this.programMemory = programMemory;
        return this;
    }

    MetaBuilder programData(QuantityBuilder programData) {
        this.programData = programData;
        return this;
    }

    MetaBuilder videoMemory(QuantityBuilder videoMemory) {
        this.videoMemory = videoMemory;
        return this;
    }

    MetaBuilder videoData(VideoDataBuilder videoData) {
        this.videoData = videoData;
        return this;
    }

    MetaBuilder videoStandard(VideoStandard videoStandard) {
        this.videoStandard = videoStandard;
        return this;
    }

    MetaBuilder footer(QuantityBuilder footer) {
        this.footer = footer
        return this
    }

    @Override
    NesMeta build() {
        return new NesMeta(
                title,
                info,
                system,
                mapper,
                busConflicts,
                programMemory.build(),
                trainer.build(),
                programData.build(),
                videoMemory.build(),
                videoData.build(),
                videoStandard,
                footer.build()
        )
    }
}
