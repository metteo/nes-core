package net.novaware.nes.core.ui;

import net.novaware.nes.core.ppu.memory.DisplayMemory;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultDisplayModel implements DisplayModel {

    private final EventListenerList listeners = new EventListenerList();
    private ChangeEvent changeEvent = null; // reused

    private DisplayMemory pixels; // TODO: do not expose display mem like that?

    private int[] palette = new int[0x40];

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(ChangeListener.class, listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
    }

    protected void fireStateChanged() {
        Object[] typeListenerPairs = listeners.getListenerList();

        for (int i = typeListenerPairs.length - 2; i >= 0; i -= 2) {
            if (typeListenerPairs[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ChangeListener changeListener = (ChangeListener) typeListenerPairs[i + 1];
                changeListener.stateChanged(changeEvent);
            }
        }
    }

    public void setPixels(DisplayMemory pixels) {
        this.pixels = pixels;
        fireStateChanged();
    }

    public DisplayMemory getPixels() {
        return pixels;
    }
}
