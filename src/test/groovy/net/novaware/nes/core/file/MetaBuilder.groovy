package net.novaware.nes.core.file;

import net.novaware.nes.core.file.NesFile.Layout;
import net.novaware.nes.core.file.NesFile.VideoStandard;
import net.novaware.nes.core.util.QuantityBuilder;

import static net.novaware.nes.core.file.NesFile.System.NES;
import static net.novaware.nes.core.file.ProgramMemoryBuilder.none;
import static net.novaware.nes.core.util.QuantityBuilder.banks16kb;
import static net.novaware.nes.core.util.QuantityBuilder.banks512b;
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb;
import static net.novaware.nes.core.util.QuantityBuilder.bytes;

class MetaBuilder implements TestDataBuilder<NesFile.Meta> {

    private String title;
    private String info;

    private NesFile.System system;

    private short mapper;
    private boolean busConflicts;

    private QuantityBuilder trainer;

    private ProgramMemoryBuilder programMemory;
    private QuantityBuilder programData;

    private QuantityBuilder videoMemory;
    private QuantityBuilder videoData;
    private VideoStandard videoStandard;
    private Layout layout;
    private QuantityBuilder remainder;

    static MetaBuilder marioBros() {
        return new MetaBuilder().title("Mario Bros.")
                .info("DiskDude!")
                .system(NES)
                .mapper(0)
                .busConflicts(false)
                .trainer(banks512b(0))
                .programMemory(none())
                .programData(banks16kb(1))
                .videoMemory(banks8kb(0))
                .videoData(banks8kb(1))
                .videoStandard(VideoStandard.NTSC)
                .layout(Layout.STANDARD_VERTICAL)
                .remainder(bytes(0));
    }

    MetaBuilder title(String title) {
        this.title = title;
        return this;
    }

    MetaBuilder info(String info) {
        this.info = info;
        return this;
    }

    MetaBuilder system(NesFile.System system) {
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

    MetaBuilder videoData(QuantityBuilder videoData) {
        this.videoData = videoData;
        return this;
    }

    MetaBuilder videoStandard(VideoStandard videoStandard) {
        this.videoStandard = videoStandard;
        return this;
    }

    MetaBuilder layout(Layout layout) {
        this.layout = layout;
        return this;
    }

    MetaBuilder remainder(QuantityBuilder remainder) {
        this.remainder = remainder;
        return this;
    }

    @Override
    NesFile.Meta build() {
        return new NesFile.Meta(
                title,
                info,
                system,
                mapper,
                busConflicts,
                trainer.build(),
                programMemory.build(),
                programData.build(),
                videoMemory.build(),
                videoData.build(),
                videoStandard,
                layout,
                remainder.build()
        );
    }
}
