package de.tarent.invio.linuxtag2014;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import de.tarent.invio.linuxtag2014.products.ProductManager;

/**
 * This class centralizes all the functionality needed for the different searches in the views.
 */
public abstract class BaseSearchHelper {

    private final Activity activity;
    private final AdapterView.OnItemClickListener itemClickListener;
    private final int searchViewId;

    /**
     * Constructor
     *
     * @param activity          the activity the searchview belongs to
     * @param searchViewId      the searchviews id
     * @param itemClickListener the listener to be set to each list item in the resultset
     */
    public BaseSearchHelper(Activity activity, int searchViewId, AdapterView.OnItemClickListener itemClickListener) {
        this.activity = activity;
        this.searchViewId = searchViewId;
        this.itemClickListener = itemClickListener;
    }

    /**
     * Sets the adapter to the searchview and also sets all the needed listeners and other configuration.
     */
    public void configureSearchView() {
        final AutoCompleteTextView textView = (AutoCompleteTextView) activity.findViewById(searchViewId);
        createOrReplaceAdapter(textView);
        addSpecialListener(textView);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClickListener.onItemClick(parent, view, position, id);
                textView.setText("");
                textView.clearFocus();
                hideSoftKeyboard();
            }
        });
    }

    /**
     * Creates or refreshes the product completion data.
     *
     * @param textView the searchview getting new data
     */
    protected void createOrReplaceAdapter(AutoCompleteTextView textView) {
        final ProductsAdapter adapter = new ProductsAdapter(
                activity,
                R.layout.searchresulttext,
                ProductManager.getMainProductManager().getUnmodifiableProducts().toArray());
        textView.setAdapter(adapter);
    }

    /**
     * This method should be used to add any listeners that are needed especially for your searchview
     * @param textView the searchview getting the listener
     */
    protected abstract void addSpecialListener(AutoCompleteTextView textView);

    private void hideSoftKeyboard() {
        final InputMethodManager inputManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
