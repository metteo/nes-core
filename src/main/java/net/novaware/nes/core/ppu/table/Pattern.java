package net.novaware.nes.core.ppu.table;

public class Pattern {

    public enum Size {
        /**
         * Square
         */
        SINGLE( 8, 8),

        /**
         * Tall / big
         */
        DOUBLE(16, 8),

        UNKNOWN(-1, -1),
        ;
        private final int lines;
        private final int dots;

        Size(int lines, int dots) {
            this.lines = lines;
            this.dots = dots;
        }

        public int lines() {
            return lines;
        }

        public int dots() {
            return dots;
        }
    }
}
