module net.novaware.nes.core {
    requires static auto.value.annotations; //FIXME: filename-based
    requires static java.compiler; // @j.a.p.Generated

    requires dagger;
    requires jakarta.inject;

    requires org.jspecify;
    requires org.checkerframework.checker.qual;

    exports net.novaware.nes.core;
}
