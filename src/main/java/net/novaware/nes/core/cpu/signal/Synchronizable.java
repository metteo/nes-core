package net.novaware.nes.core.cpu.signal;

public interface Synchronizable {

    void ready(Signal s);

    /**
     * ___
     * RDY active-low line
     */
    default void rdy(Signal s) {
        ready(s);
    }

    @FunctionalInterface
    interface SyncListener {

        void onSyncChange(Signal s);

        /**
         * SYNC output line
         */
        default void sync(Signal s) {
            onSyncChange(s);
        }
    }

    void setSyncListener(SyncListener listener);

    void clearSyncListener();
}
