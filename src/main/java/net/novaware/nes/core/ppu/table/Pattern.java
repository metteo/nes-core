package net.novaware.nes.core.ppu.table;

public class Pattern {

    public enum Size {
        /**
         * Square tiles
         */
        SINGLE( 8, 8),

        /**
         * Tall tiles
         */
        DOUBLE(16, 8),

        UNKNOWN(-1, -1),
        ;
        private final int height;
        private final int width;

        Size(int height, int width) {
            this.height = height;
            this.width = width;
        }


        public int height() {
            return height;
        }

        public int width() {
            return width;
        }
    }
}
