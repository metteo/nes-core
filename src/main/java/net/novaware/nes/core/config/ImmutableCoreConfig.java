package net.novaware.nes.core.config;

import com.google.auto.value.AutoValue;
import net.novaware.nes.core.memory.MemoryBus;

@AutoValue
public abstract class ImmutableCoreConfig implements CoreConfig {

    public static ImmutableCoreConfig.Builder builder() {
        return new AutoValue_ImmutableCoreConfig.Builder();
    }

    abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setRecordCpuBus(boolean record);

        public abstract ImmutableCoreConfig build();
    }
}
