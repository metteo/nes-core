package net.novaware.nes.core.model;

import net.novaware.nes.core.tv.ColorNtsc;
import net.novaware.nes.core.tv.ColorPal;
import net.novaware.nes.core.tv.ColorPalM;
import net.novaware.nes.core.tv.ColorPalN;

public interface ClockModels {
    ClockModel NTSC = new ClockModel("Clock NTSC", ColorNtsc.SUBCARRIER * 6);
    ClockModel PAL  = new ClockModel("Clock PAL",  ColorPal .SUBCARRIER * 6);
    ClockModel PALM = new ClockModel("Clock PALM", ColorPalM.SUBCARRIER * 6);
    ClockModel PALN = new ClockModel("Clock PALN", ColorPalN.SUBCARRIER * 6);
}
