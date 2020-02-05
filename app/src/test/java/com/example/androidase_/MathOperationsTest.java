package com.example.androidase_;

import org.junit.Test;

import static com.example.androidase_.other_classes.MathOperations.measureDistanceInMeters;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
public class MathOperationsTest {
    @Test
    public void testMeasureDistanceInMeters() {
        System.out.println("measureDistanceInMeters");
        double lat1 = 12.4;
        double lon1 = -12.4;
        double lat2 = lat1;
        double lon2 = lon1;
        double expResult = 0;
        double result = measureDistanceInMeters(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result);
        assertNotEquals(0, measureDistanceInMeters(lat1, lon1, lat2 + 1, lon2 + 1));
        assertTrue(String.valueOf(true), measureDistanceInMeters(lat1, lon1, lat2 + 1, lon2 + 1) > 0);
        assertFalse(String.valueOf(true), measureDistanceInMeters(lat1, lon1, lat2, lon2) > 0);
    }
}
