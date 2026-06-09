package net.novaware.nes.core.ui;

import javax.swing.event.ChangeListener;
import java.awt.*;

public interface DisplayModel {

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener l);

    Color getColor(int y, int x);
}
