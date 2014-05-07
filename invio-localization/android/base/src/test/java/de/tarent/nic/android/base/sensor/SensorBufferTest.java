package de.tarent.nic.android.base.sensor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SensorBufferTest {

    private SensorBuffer<Integer> sensorBuffer;

    @Before
    public void setUp() {
        sensorBuffer = new SensorBuffer<Integer>(3);
    }

    @Test
    public void testAdd() {
        sensorBuffer.addMeasurement(5);
        sensorBuffer.addMeasurement(3);

        assertNull(sensorBuffer.get(2));

        sensorBuffer.addMeasurement(1);

        assertTrue(5 == sensorBuffer.get(0));
        assertTrue(3 == sensorBuffer.get(1));
        assertTrue(1 == sensorBuffer.get(2));

        sensorBuffer.addMeasurement(9);

        assertTrue(3 == sensorBuffer.get(0));
        assertTrue(1 == sensorBuffer.get(1));
        assertTrue(9 == sensorBuffer.get(2));
    }

    @Test
    public void testBufferCopy() {
        sensorBuffer.addMeasurement(5);
        sensorBuffer.addMeasurement(3);
        sensorBuffer.addMeasurement(1);

        assertTrue(sensorBuffer.size() == sensorBuffer.getBuffer().size());

        SensorBuffer<Integer> tmpBuffer = new SensorBuffer<Integer>(4);

        tmpBuffer.addMeasurement(2);
        tmpBuffer.addMeasurement(6);
        tmpBuffer.addMeasurement(8);

        sensorBuffer.setBuffer(tmpBuffer.getBuffer());
        assertTrue(2 == sensorBuffer.get(0));
        assertTrue(6 == sensorBuffer.get(1));
        assertTrue(8 == sensorBuffer.get(2));

        tmpBuffer.addMeasurement(4);

        sensorBuffer.setBuffer(tmpBuffer.getBuffer());

        assertTrue(6 == sensorBuffer.get(0));
        assertTrue(8 == sensorBuffer.get(1));
        assertTrue(4 == sensorBuffer.get(2));

        tmpBuffer.clear();
        assertTrue(tmpBuffer.getBuffer().size() == 0);

    }

    @Test
    public void testLowPassFiltering() {
        SensorBuffer<float[]> sensorBuffer = new SensorBuffer<float[]>(3);
        LowPassFilter filter = new LowPassFilter(0.3f);
        float[] tmp = new float[3];
        tmp[0] = 5f;
        tmp[1] = 3f;
        tmp[2] = 8f;

        sensorBuffer.addMeasurement(tmp);
        assertNull(sensorBuffer.getFilteredValue());

        sensorBuffer.setFilter(filter);
        sensorBuffer.addMeasurement(tmp);

        assertEquals(1.5f, sensorBuffer.getFilteredValue()[0], 0.00001);
        assertEquals(0.9f, sensorBuffer.getFilteredValue()[1], 0.00001);
        assertEquals(2.4f, sensorBuffer.getFilteredValue()[2], 0.00001);

        tmp[0] = 7f;
        tmp[1] = 9f;
        tmp[2] = 1f;

        sensorBuffer.addMeasurement(tmp);
        assertEquals(3.15f, sensorBuffer.getFilteredValue()[0], 0.00001);
        assertEquals(3.33f, sensorBuffer.getFilteredValue()[1], 0.00001);
        assertEquals(1.98f, sensorBuffer.getFilteredValue()[2], 0.00001);

        filter.setWeight(1.f);

        tmp[0] = 6f;
        tmp[1] = 4f;
        tmp[2] = 2f;

        sensorBuffer.addMeasurement(tmp);
        assertEquals(6., sensorBuffer.getFilteredValue()[0], 0.00001);
        assertEquals(4., sensorBuffer.getFilteredValue()[1], 0.00001);
        assertEquals(2., sensorBuffer.getFilteredValue()[2], 0.00001);

        filter.setWeight(0.f);

        tmp[0] = 1f;
        tmp[1] = 2f;
        tmp[2] = 3f;

        sensorBuffer.addMeasurement(tmp);
        assertEquals(6., sensorBuffer.getFilteredValue()[0], 0.00001);
        assertEquals(4., sensorBuffer.getFilteredValue()[1], 0.00001);
        assertEquals(2., sensorBuffer.getFilteredValue()[2], 0.00001);
    }

    @Test
    public void testCompassFilter() {
        SensorBuffer<Float> sensorBuffer = new SensorBuffer<Float>(3);
        CompassFilter filter = new CompassFilter(0.5f);
        Float tmp = 5.59f; // 320deg

        sensorBuffer.addMeasurement(tmp);
        assertNull(sensorBuffer.getFilteredValue());

        sensorBuffer.setFilter(filter);
        sensorBuffer.addMeasurement(tmp);

        assertEquals(5.59f, sensorBuffer.getFilteredValue(), 0.00001);

        tmp = 0.79f; // 45deg
        sensorBuffer.addMeasurement(tmp);

        assertEquals(0.0484f, sensorBuffer.getFilteredValue(), 0.00001);

        filter.setWeight(0.2f);
        tmp = 0.44f; // 25deg
        sensorBuffer.addMeasurement(tmp);

        assertEquals(0.122f, sensorBuffer.getFilteredValue(), 0.01);

        filter.setWeight(0.f);
        tmp = 0.44f; // 25deg
        sensorBuffer.addMeasurement(tmp);

        assertEquals(0.122f, sensorBuffer.getFilteredValue(), 0.01);

        filter.setWeight(1.f);
        tmp = 0.17f; // 10deg
        sensorBuffer.addMeasurement(tmp);
        assertEquals(tmp, sensorBuffer.getFilteredValue(), 0.01);
        filter.setWeight(0.3f);
        tmp = 5.24f;
        sensorBuffer.addMeasurement(tmp);
        assertEquals(6.119f, sensorBuffer.getFilteredValue(), 0.01);
    }

    @Test
    public void testCompassFilterOff() {
        SensorBuffer<Float> sensorBuffer = new SensorBuffer<Float>(3);
        CompassFilter filter = new CompassFilter(1.f);
        sensorBuffer.setFilter(filter);

        Float tmp = 5.59f; // 320 deg
        sensorBuffer.addMeasurement(tmp);

        assertEquals(tmp, sensorBuffer.getFilteredValue(), 0.00001);

        tmp = 0.79f; // 45 deg
        sensorBuffer.addMeasurement(tmp);
        assertEquals(tmp, sensorBuffer.getFilteredValue(), 0.00001);

        sensorBuffer.addMeasurement(tmp);
        assertEquals(tmp, sensorBuffer.getFilteredValue(), 0.00001);

        sensorBuffer.addMeasurement(tmp);
        assertEquals(tmp, sensorBuffer.getFilteredValue(), 0.00001);
    }

}
