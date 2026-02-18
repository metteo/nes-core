module net.novaware.nes.core {
    requires static auto.value.annotations; //FIXME: filename-based
    requires static java.compiler; // @j.a.p.Generated

    requires dagger;
    requires jakarta.inject;

    requires org.jspecify;
    requires org.checkerframework.checker.qual;

    exports net.novaware.nes.core;
    exports net.novaware.nes.core.port;
    exports net.novaware.nes.core.cart;

    exports net.novaware.nes.core.memory; // TODO: Consider creating a dedicated package for peripherals like MemoryDevice
    // TODO: should not be exposed
    exports net.novaware.nes.core.clock;
    exports net.novaware.nes.core.cpu.unit;
    exports net.novaware.nes.core.port.internal;
}
