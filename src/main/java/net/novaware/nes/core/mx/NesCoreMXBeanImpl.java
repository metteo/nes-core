package net.novaware.nes.core.mx;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

// TODO: move impl out of this project
// TODO: there should be mxbean interface and MXRec interface
@BoardScope
public class NesCoreMXBeanImpl
        extends NotificationBroadcasterSupport
        implements NesCoreMXBean, NesCoreRecorder
{

    private volatile long secondFrameTime;
    private volatile long secondSpinTime;
    private volatile double framesPerSecond;

    private final Runnable hardwareReset = () -> {};

    private long sequenceNumber = 1;

    @Inject
    public NesCoreMXBeanImpl() {

    }

    @Override
    public void setAttributes(long secondFrameTime, long secondSpinTime, double fps) {
        this.secondFrameTime = secondFrameTime;
        this.secondSpinTime = secondSpinTime;
        this.framesPerSecond = fps;
    }

    public void notifyEvent(String reason) { // "Event" part can be customized
        Notification notification = new Notification(
                "net.novaware.nes.core.event",
                this,
                sequenceNumber++,
                System.currentTimeMillis(),
                "Some event: " + reason
        );

        sendNotification(notification);
    }

    @Override
    public long getSecondFrameTime() {
        return secondFrameTime;
    }

    @Override
    public long getSecondSpinTime() {
        return secondSpinTime;
    }

    @Override
    public double getFramesPerSecond() {
        return framesPerSecond;
    }

    @Override
    public void hardwareReset() {
        notifyEvent("Reset not supported yet :(");
        hardwareReset.run();
    }
}
