package net.novaware.nes.core.config

import net.novaware.nes.core.test.TestDataBuilder

class CoreConfigBuilder implements TestDataBuilder<CoreConfig> {

    private boolean recordCpuBus = false
    private Region region = Region.USA
    private Platform platform = Platform.NES_FAMICOM
    private VideoStandard videoStandard = VideoStandard.NTSC

    CoreConfigBuilder videoStandard(VideoStandard vs) {
        this.videoStandard = vs
        return this
    }

    static CoreConfigBuilder ntsc() {
        new CoreConfigBuilder()
    }

    @Override
    CoreConfig build() {
        ImmutableCoreConfig.builder()
            .setRecordCpuBus(recordCpuBus)
            .setRegion(region)
            .setPlatform(platform)
            .setVideoStandard(videoStandard)
            .build()
    }
}
