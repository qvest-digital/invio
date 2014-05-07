package de.tarent.invio.linuxtag2014.products;

import android.util.Log;
import de.tarent.invio.linuxtag2014.task.InvioOsmParserKeys;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The ProductManager stores all our products.
 */
public class ProductManager implements DownloadListener {

    /**
     * This should be the only instance that is used across the whole app. Let's call it "singleton by convention".
     * (i.e. it's uniqueness is not enforced, because that would make it harder to test)
     */
    private static ProductManager mainProductManager = new ProductManager();

    private Map<Long, Product> products;

    private Map<String, ProductItem> productItems;


    /**
     * Constructor.
     */
    public ProductManager() {
        products = new HashMap<Long, Product>();
        productItems = new HashMap<String, ProductItem>();
    }

    public static ProductManager getMainProductManager() {
        return mainProductManager;
    }

    public static void setMainProductManager(ProductManager mainProductManager) {
        ProductManager.mainProductManager = mainProductManager;
    }

    /**
     * Gets a {@link Collection} of unmodifiable {@link Product}s. These should be the ones received from the
     * server and therefore should not be modified.
     *
     * @return a {@link Collection} of unmodifiable products
     */
    public Collection<Product> getUnmodifiableProducts() {
        return Collections.unmodifiableCollection(products.values());
    }

    /**
     * Find a product by its barcode.
     *
     * @param barcode the barcode of the product that we are looking for
     * @return the product, or null if no matching barcode was found
     */
    public Product getProductByBarcode(long barcode) {
        return products.get(barcode);
    }

    /**
     * Gets a {@link Collection} of unmodifiable {@link ProductItem}s. These should be the ones received from the
     * server and therefore should not be modified.
     *
     * @return a {@link Collection} of unmodifiable product items
     */
    public Collection<ProductItem> getUnmodifiableProductItems() {
        return Collections.unmodifiableCollection(productItems.values());
    }

    /**
     * Gets a {@link ProductItem} by it's name.
     *
     * @param name the product name
     * @return the {@link ProductItem} that matches the given name
     */
    public ProductItem getProductItemByName(final String name) {
        return productItems.get(name);
    }

    @Override
    public void onDownloadFinished(DownloadTask task, boolean success, Object data) {
        final Set<Product> productSet = ((Map<String, Set>)data).get(InvioOsmParserKeys.PRODUCTS);
        if(success) {
            if ((productSet != null) && (productSet.size() > 0)) {
                for (Product p : productSet) {
                    products.put(p.getBarcode(), p);
                }
            }

            final Set<ProductItem> productItemSet = ((Map<String, Set>)data).get(InvioOsmParserKeys.PRODUCTITEMS);
            if ((productItemSet != null) && (productItemSet.size() > 0)) {
                for (ProductItem productItem : productItemSet) {
                    productItems.put(productItem.getProduct().getShortName(), productItem);
                }
            }

            Log.d(this.getClass().getCanonicalName(), "Loaded "+products.size()+" products.");
        } else {
            Log.e(this.getClass().getCanonicalName(), "One of the maps data is missing. The products for this map" +
                    " will not be present!");
        }

    }

}
