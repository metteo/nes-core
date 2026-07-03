package net.novaware.nes.core.mx;

// TODO: rename to MasterClockRec
public interface NesCoreRecorder {
    void setAttributes(long secondFrameTime, long secondSpinTime, double fps);
}
