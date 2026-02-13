package net.novaware.nes.core.cpu;

public interface Synchronizable {

    void ready(boolean high);

    /**
     * ___
     * RDY active-low line
     */
    default void rdy(boolean high) {
        ready(high);
    }

    @FunctionalInterface
    interface SyncListener {

        void onSyncChange(boolean high);

        /**
         * SYNC output line
         */
        default void sync(boolean high) {
            onSyncChange(high);
        }
    }

    void addSyncListener(SyncListener listener);

    void removeSyncListener(SyncListener listener);
}
