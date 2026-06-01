package net.novaware.nes.core.config;

public enum BorderRegion { // TODO: reference video standard instead of defining it's name here again.
    NTSC (16, 11, 0, 2),
    PAL  ( 2,  2, 1, 0)
    ;
    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    BorderRegion(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }
}
