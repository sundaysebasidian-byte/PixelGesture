package com.pixelgesture;

/** Pixel geometry independent of a device brand or Android window object. */
final class OverlayGeometry {
    private OverlayGeometry() { }

    static int available(int length, int leadingInset, int trailingInset) {
        long result = (long) Math.max(0, length)
                - Math.max(0, leadingInset) - Math.max(0, trailingInset);
        return (int) Math.max(0L, result);
    }

    static int sideHeight(int screenHeight, int topInset, int bottomInset, int gap) {
        return available(available(screenHeight, topInset, bottomInset), gap, 0);
    }
}
