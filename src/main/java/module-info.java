module net.novaware.nes.core {
    requires auto.value.annotations;
    requires java.compiler; // @j.a.p.Generated

    requires org.jspecify;
    requires org.checkerframework.checker.qual;
    requires org.checkerframework.checker.util;

    // FIXME: uncomment for local IJ testing or use cp based launch configurations (for single tests)
    /*
    opens net.novaware.nes.core;
    opens net.novaware.nes.core.cpu;
    opens net.novaware.nes.core.cpu.instruction;
    opens net.novaware.nes.core.file;
    opens net.novaware.nes.core.file.ines;
    opens net.novaware.nes.core.file.nesy;
    opens net.novaware.nes.core.file.pasofami;
    opens net.novaware.nes.core.file.unif;
    opens net.novaware.nes.core.net;
    opens net.novaware.nes.core.util;
    */
}
