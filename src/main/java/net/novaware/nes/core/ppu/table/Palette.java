package net.novaware.nes.core.ppu.table;

public class Palette {

    public enum Layer {
        BACKGROUND(0x00), // 0
        SPRITE(0x10),     // 1
        ;
        private final int bit;

        Layer(int bit) {
            this.bit = bit;
        }

        public int bit() {
            return bit;
        }
    }
}
