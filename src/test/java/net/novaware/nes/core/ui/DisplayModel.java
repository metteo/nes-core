package net.novaware.nes.core.ui;

import javax.swing.event.ChangeListener;

public interface DisplayModel {

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener l);
}
