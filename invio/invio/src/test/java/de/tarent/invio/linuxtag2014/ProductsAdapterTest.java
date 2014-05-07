package de.tarent.invio.linuxtag2014;

import android.content.Context;
import de.tarent.invio.linuxtag2014.products.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "../invio/AndroidManifest.xml")
public class ProductsAdapterTest {

    ProductsAdapter productsAdapter;

    ProductsAdapter.ProductFilter filter;

    @Before
    public void setup() {
        Product[] products = new Product[3];
        products[0] = new Product(123456, "Tofu (Vegan, lecker)", 199);
        products[1] = new Product(456789, "Brot (Brot, Vollkorn)", 229);
        products[2] = new Product(789123, "Wein (Wein, Rot)", 399);

        productsAdapter = new ProductsAdapter(mock(Context.class), R.layout.searchresulttext, products);
        filter = (ProductsAdapter.ProductFilter) productsAdapter.getFilter();
    }

    @Test
    public void testFilter() {
        filter.publishResults("tofu", filter.performFiltering("tofu"));
        assertEquals(1, productsAdapter.getCount());
        assertEquals("Tofu", productsAdapter.getItem(0));

        filter.publishResults("rot", filter.performFiltering("rot"));
        assertEquals(2, productsAdapter.getCount());
    }

    @Test
    public void testEmptyFilter() {
        filter.publishResults(null, filter.performFiltering(null));
        assertEquals(0, productsAdapter.getCount());

        filter.publishResults("", filter.performFiltering(""));
        assertEquals(0, productsAdapter.getCount());

        filter.publishResults("o", filter.performFiltering("o"));
        assertEquals(0, productsAdapter.getCount());
    }

    @Test
    public void testFilterBarcode() {
        filter.publishResults("567", filter.performFiltering("567"));
        assertEquals(1, productsAdapter.getCount());
        assertEquals("Brot", productsAdapter.getItem(0));

        filter.publishResults("23", filter.performFiltering("23"));
        assertEquals(2, productsAdapter.getCount());
    }

}
