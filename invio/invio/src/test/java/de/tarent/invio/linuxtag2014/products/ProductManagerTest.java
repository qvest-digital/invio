package de.tarent.invio.linuxtag2014.products;

import de.tarent.invio.linuxtag2014.task.InvioOsmParserKeys;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class ProductManagerTest {

    ProductManager pm = new ProductManager();
    Set<Product> expectedProducts = new HashSet<Product>();
    Set<ProductItem> expectedProductItems = new HashSet<ProductItem>();

    @Test
    public void testDownload() {
        Map<String, Set> result = makeDownloadResult();
        pm.onDownloadFinished(null, true, result);

        Collection<Product> products = pm.getUnmodifiableProducts();
        assertTrue(expectedProducts.containsAll(products));
        assertTrue(products.containsAll(expectedProducts));
        try {
            products.clear();
            fail("The products should be unmodifiable!");
        } catch (UnsupportedOperationException e) {
            // Fine!
            int i=0;
        }

        Collection<ProductItem> productItems = pm.getUnmodifiableProductItems();
        assertTrue(expectedProductItems.containsAll(productItems));
        assertTrue(productItems.containsAll(expectedProductItems));
        try {
            productItems.clear();
            fail("The productItems should be unmodifiable!");
        } catch (UnsupportedOperationException e) {
            // Fine!
            int i=0;
        }
    }

    private Map<String, Set> makeDownloadResult() {
        Product p1 = new Product(4102610005744L, "Vulkanpark-Quelle Classic 0,7L", 49);
        Product p2 = new Product(111122223L, "Rubbellos (Niete)", 100);
        Product p3 = new Product(1234567890123L, "Brot (Vollkorn)", 219);
        expectedProducts.add(p1);
        expectedProducts.add(p2);
        expectedProducts.add(p3);

        expectedProductItems.add(new ProductItem(p1, new NicGeoPointImpl()));
        expectedProductItems.add(new ProductItem(p2, new NicGeoPointImpl()));
        expectedProductItems.add(new ProductItem(p3, new NicGeoPointImpl()));


        Map<String, Set> result = new HashMap<String, Set>();
        result.put(InvioOsmParserKeys.PRODUCTS, expectedProducts);
        result.put(InvioOsmParserKeys.PRODUCTITEMS, expectedProductItems);

        return result;
    }

    @Test
    public void testSingleton() {
        ProductManager defaultPm = ProductManager.getMainProductManager();

        assertTrue(pm instanceof ProductManager);

        ProductManager.setMainProductManager(pm);
        ProductManager testPm = ProductManager.getMainProductManager();

        assertEquals(pm, testPm);
    }

    @Test
    public void testGet() {
        Map<String, Set> result = makeDownloadResult();
        pm.onDownloadFinished(null, true, result);

        assertEquals("Brot", pm.getProductByBarcode(1234567890123L).getShortName());

        assertEquals(111122223L, pm.getProductItemByName("Rubbellos").getProduct().getBarcode());
    }



}
