package de.tarent.nic.android.base.particlefilter;


import android.content.Context;
import de.tarent.nic.android.base.position.UserPositionManager;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Config(manifest = "../base/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class ParticleFilterTest {

    // The Object under Test:
    ParticleFilter pf;

    @Mock
    private Context ctx;

    @Mock
    private UserPositionManager userPosManager;

    @Before
    public void setUp() {
        initMocks(this);
        when(ctx.getResources()).thenReturn(Robolectric.application.getResources());
    }

    @Test
    public void testInitialization() {

        pf = new ParticleFilterTestable();
        pf.setNumParticles(6);
        pf.setScale(1.f);
        pf.sigmaInit = 1.f;

        pf.initialize(0, 0);

        assertTrue(6 == pf.particleList.size());
        assertEquals(0.17f, pf.particleList.get(0).getWeight(), 0.01f);
        assertEquals(0.1f, pf.particleList.get(0).getX(), 0.01f);
        assertEquals(0.1f, pf.particleList.get(0).getY(), 0.01f);
    }

    @Test
    public void testResampling() {
        pf = new ParticleFilterTestable();
        pf.setNumParticles(4);

        Particle tmp = new Particle(1.f, 2.f, 0.25f);
        pf.particleList.add(tmp);

        tmp = new Particle(3.f, 4.f, 0.01f);
        pf.particleList.add(tmp);

        tmp = new Particle(5.f, 6.f, 0.73f);
        pf.particleList.add(tmp);

        tmp = new Particle(7.f, 8.f, 0.01f);
        pf.particleList.add(tmp);

        pf.sigmaSensor = 1.f;
        pf.setScale(1.f);

        pf.resampling();

        assertEquals(1.1f, pf.particleList.get(0).getX(), 0.01f);
        assertEquals(5.1f, pf.particleList.get(1).getX(), 0.01f);
        assertEquals(5.1f, pf.particleList.get(2).getX(), 0.01f);
        assertEquals(5.1f, pf.particleList.get(3).getX(), 0.01f);
    }

    @Test
    public void testUpdateAction() {
        double deltaX = 2.f;
        double deltaY = 0.5f;

        pf = new ParticleFilterTestable();
        pf.setNumParticles(2);

        Particle tmp = new Particle(1.f, 2.f, 0.6f);
        pf.particleList.add(tmp);

        tmp = new Particle(4.f, 5.f, 0.4f);
        pf.particleList.add(tmp);

        pf.sigmaAction = 1.f;
        pf.setScale(1.f);

        pf.updateAction(deltaX, deltaY);

        assertEquals(3.1f, pf.particleList.get(0).getX(), 0.01f);
        assertEquals(2.6f, pf.particleList.get(0).getY(), 0.01f);
        assertEquals(6.1f, pf.particleList.get(1).getX(), 0.01f);
        assertEquals(5.6f, pf.particleList.get(1).getY(), 0.01f);

    }

    @Test
    public void testUpdateSensor() {
        double wifiX = 1.f;
        double wifiY = 5.f;

        pf = new ParticleFilterTestable();
        pf.setNumParticles(2);

        Particle tmp = new Particle(2.f, 4.f, 0.5f);
        pf.particleList.add(tmp);

        tmp = new Particle(20.f, 40.f, 0.5f);
        pf.particleList.add(tmp);

        pf.updateSensor(wifiX, wifiY);

        assertEquals(0.966f, pf.particleList.get(0).getWeight(), 0.01f);
        assertEquals(0.034f, pf.particleList.get(1).getWeight(), 0.01f);

    }

    @Test
    public void testCalculatePosition() {
        pf = new ParticleFilterTestable();
        pf.setNumParticles(4);

        Particle tmp = new Particle(1.f, 2.f, 0.25f);
        pf.particleList.add(tmp);

        tmp = new Particle(3.f, 4.f, 0.01f);
        pf.particleList.add(tmp);

        tmp = new Particle(5.f, 6.f, 0.73f);
        pf.particleList.add(tmp);

        tmp = new Particle(7.f, 8.f, 0.01f);
        pf.particleList.add(tmp);

        NicGeoPoint pos = pf.calculatePosition();

        assertEquals(4.f, pos.getX(), 0.2f);
        assertEquals(5.f, pos.getY(), 0.2f);
    }

    class ParticleFilterTestable extends ParticleFilter {
        public double nextRandomValue = 0.5f;
        public double nextGaussianValue = 0.1f;

        ParticleFilterTestable() {
            super(ctx, userPosManager);
        }

        protected double nextRandom() {
            return nextRandomValue;
        }

        protected double nextGaussian() {
            return nextGaussianValue;
        }
    }

}
