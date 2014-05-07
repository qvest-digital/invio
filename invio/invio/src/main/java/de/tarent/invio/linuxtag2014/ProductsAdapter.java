package de.tarent.invio.linuxtag2014;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import de.tarent.invio.linuxtag2014.products.Product;

import java.util.ArrayList;
import java.util.List;


/**
 * The ProductsAdapter is an ArrayAdapter that can be used with the AutoCompleteTextView.
 * It allows filtering of products (by name and barcode) and displays the productnames without their categories (the
 * part in braces). Those are not displayed, although they are searched (to find all products of a certain kind).
 */
public class ProductsAdapter extends ArrayAdapter {

    private final Object[] allProducts;

    /**
     * Construct new ProductsAdapter
     * @param context for the base class
     * @param textViewResourceId for the base class
     * @param products must be Product[] (for some reason we couldn't declare it as that). Won't be modified.
     */
    public ProductsAdapter(final Context context, final int textViewResourceId, final Object[] products) {
        super(context, textViewResourceId, new ArrayList<String>());
        this.allProducts = products.clone();
    }

    @Override
    public Filter getFilter() {
        return new ProductFilter();
    }


    /**
     * The ProductFilter does the real filtering.
     */
    public class ProductFilter extends Filter {

        @Override
        public FilterResults performFiltering(final CharSequence searchString) {
            final FilterResults results = new FilterResults();

            if ((searchString == null) || (searchString.length() < 2)) {
                return results;
            }

            final List<String> filteredProducts = getFilteredProducts(searchString);

            results.values = filteredProducts;
            results.count = filteredProducts.size();

            return results;
        }

        @Override
        public void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.count > 0) {
                addAll((ArrayList<String>) results.values);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    private List<String> getFilteredProducts(final CharSequence searchString) {
        final List<String> filteredProducts = new ArrayList<String>();

        for (final Object object : allProducts) {
            final Product product = (Product) object;
            final String name = product.getName();
            if (name.toLowerCase().contains(searchString.toString().toLowerCase())
                    || String.valueOf(product.getBarcode()).contains(searchString)) {
                filteredProducts.add(product.getShortName());
            }
        }

        return filteredProducts;
    }

}
