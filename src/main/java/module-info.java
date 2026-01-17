module net.novaware.nes.core {
    requires static auto.value.annotations; //FIXME: filename-based
    requires static java.compiler; // @j.a.p.Generated

    requires org.jspecify;
    requires org.checkerframework.checker.qual;

    // TODO: tests in maven are running in classpath mode
}
