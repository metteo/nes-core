module net.novaware.nes.core {
    requires static auto.value.annotations; //FIXME: filename-based
    requires static java.compiler; // @j.a.p.Generated

    requires dagger;
    requires jakarta.inject;

    requires org.jspecify;
    requires org.checkerframework.checker.qual;
    requires java.desktop; // TODO: temporary for testing, move to tests later
    requires java.logging;
    requires java.management; // TODO: make it a separate add on module

    exports net.novaware.nes.core;
    exports net.novaware.nes.core.board;
    exports net.novaware.nes.core.config;
    exports net.novaware.nes.core.port;
    exports net.novaware.nes.core.cart;
    exports net.novaware.nes.core.util;

    exports net.novaware.nes.core.memory; // TODO: Consider creating a dedicated package for peripherals like MemoryDevice
    // TODO: should not be exposed, unless it's a debug build / diagnostic module
    exports net.novaware.nes.core.clock;
    exports net.novaware.nes.core.cpu.unit;
    exports net.novaware.nes.core.port.internal;
    exports net.novaware.nes.core.cpu.memory;
    exports net.novaware.nes.core.cpu.inject;
    exports net.novaware.nes.core.cpu.register;
}
