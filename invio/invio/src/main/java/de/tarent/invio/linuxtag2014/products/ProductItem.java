package de.tarent.invio.linuxtag2014.products;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * The ProductItem is an OverlayItem which is associated with a product. I.e. it marks a spot on the map where this
 * product is available.
 */
public class ProductItem extends OverlayItem {

    private final Product product;


    /**
     * Create a new ProductItem.
     *
     * @param product the product represented by this item.
     * @param point the position of the product on the map
     */
    public ProductItem(final Product product, final GeoPoint point) {
        super("title", "snippet", point);
        this.product = product;
    }

    @Override //NOSONAR - Not duplicate code.
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ProductItem that = (ProductItem) o;

        if (product != null ? !product.equals(that.product) : that.product != null) {
            return false;
        }
        final GeoPoint thisPoint = getPoint();
        final GeoPoint thatPoint = that.getPoint();
        if (thisPoint != null ? !thisPoint.equals(thatPoint) : thatPoint != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int productHash = product != null ? product.hashCode() : 0;
        final GeoPoint thisPoint = getPoint();
        final int pointHash = thisPoint != null ? thisPoint.hashCode() : 0;

        final int result = 31 * productHash + pointHash;
        return result;
    }

    public Product getProduct() {
        return product;
    }
}
