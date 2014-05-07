package de.tarent.invio.linuxtag2014.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import de.tarent.invio.linuxtag2014.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "../invio/AndroidManifest.xml")
public class ProductTest {

    /**
     * If this test breaks then do not just adapt the test! This test is to ensure that the methods work the way
     * that the code needs them to work. You must check all usages of the modified methods and test, if the modification
     * doesn't break anything.
     */
    @Test
    public void testGetNames() {
        Product p1 = new Product(1234567890123L, "Zeile 1_Zeile 2 (Kategorie1, Kategorie2)", 99);
        Product p2 = new Product(1234567890123L, "Zeile 1 (Kategorie1)", 99);
        Product p3 = new Product(1234567890123L, "Zeile 1_Zeile 2", 99);
        //TODO: write a test for parsing of the talks
        Product p4 = new Product(1234567890123L, "08.05.2014 12:00-13:00_State of the Union_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=116, 08.05.2014 18:30-19:00_Systemsicherheit_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1142, 08.05.2014 19:00-20:00_Web of Trust or central Cas: Authenticating communities_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1144, 08.05.2014 20:30-21:00_Vom Aussterben bedroht: die Universalmaschine Computer_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1150, 08.05.2014 21:00-21:30_Reducing iptables configuration complexity using chains_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1152, 08.05.2014 21:30-22:00_Ubuntu Privacy Remix: Radikaler Ansatz gegen Bespitzelung durch NSA u. Verfassungsschutz u. Co._http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1154 ", 99);

        assertEquals("Zeile 1", p1.getNameLineOne());
        assertEquals("Zeile 2", p1.getNameLineTwo());
        assertEquals("Zeile 1 Zeile 2", p1.getShortName());
        assertEquals("Zeile 1_Zeile 2 (Kategorie1, Kategorie2)", p1.getName());
        assertEquals("Kategorie1, Kategorie2", p1.getCategories());

        assertEquals("Zeile 1", p2.getNameLineOne());
        assertEquals("", p2.getNameLineTwo());
        assertEquals("Zeile 1", p2.getShortName());
        assertEquals("Zeile 1 (Kategorie1)", p2.getName());
        assertEquals("Kategorie1", p2.getCategories());

        assertEquals("Zeile 1", p3.getNameLineOne());
        assertEquals("Zeile 2", p3.getNameLineTwo());
        assertEquals("Zeile 1 Zeile 2", p3.getShortName());
        assertEquals("Zeile 1_Zeile 2", p3.getName());
        assertEquals("", p3.getCategories());
    }

 /*   @Test
    public void testThatTheCorrectProductIconIsSelected() {
        // Blue icon for the booths.
        Bitmap boothPOI = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.poi_b);

        // Grey icon for the rooms.
        Bitmap roomPOI = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.poi_g);

        // Default icon for products.
        Bitmap defaultPOI = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.poi);

        Product booth = new Booth(0, "booth", 0);
        Product room = new Room(0, "room", 0);
        Product defaultProduct = new Product(0, "default", 0);

        assertEquals(boothPOI, booth.getIcon());
        assertEquals(roomPOI, room.getIcon());
        assertEquals(defaultPOI, defaultProduct.getIcon());
    }*/
}
