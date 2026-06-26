package net.novaware.nes.core.mx;

// TODO: consider custom JFR Events too
// TODO: consider JVMTI / Tool Interface to create diagnostics.jar
// TODO: expose internal through MXBean instead of building debugger guis
public interface NesCoreMXBean {

    long getSecondFrameTime();

    long getSecondSpinTime();

    double getFramesPerSecond();

    void hardwareReset();
}
