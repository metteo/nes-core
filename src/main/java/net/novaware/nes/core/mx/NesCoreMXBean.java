package net.novaware.nes.core.mx;

// TODO: consider custom JFR Events too
// TODO: consider JVMTI / Tool Interface to create diagnostics.jar
// TODO: expose internal through MXBean instead of building debugger guis
// TODO: rename to MasterClockMXBean and have separate beans for different components
public interface NesCoreMXBean {

    long getSecondFrameTime();

    long getSecondSpinTime();

    double getFramesPerSecond();

    // TODO: move to BoardMXBean
    void hardwareReset();
}
