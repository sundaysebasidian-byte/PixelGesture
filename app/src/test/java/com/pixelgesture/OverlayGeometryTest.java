package com.pixelgesture;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class OverlayGeometryTest {
    @Test public void portraitBarsLeaveTheExpectedContent() {
        assertEquals(2268, OverlayGeometry.available(2400, 84, 48));
        assertEquals(2196, OverlayGeometry.sideHeight(2400, 84, 48, 72));
    }

    @Test public void landscapeCutoutAndNavigationBarBothConsumeWidth() {
        assertEquals(2208, OverlayGeometry.available(2400, 144, 48));
    }

    @Test public void shortWindowNeverGetsAnArtificial240DpOverlay() {
        assertEquals(18, OverlayGeometry.sideHeight(150, 60, 48, 24));
    }

    @Test public void exhaustedOrInvalidViewportHasNoOverlayArea() {
        assertEquals(0, OverlayGeometry.sideHeight(100, 60, 48, 24));
        assertEquals(0, OverlayGeometry.available(-10, 0, 0));
        assertEquals(0, OverlayGeometry.available(100, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
}
