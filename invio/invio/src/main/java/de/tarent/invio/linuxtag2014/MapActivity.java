package de.tarent.invio.linuxtag2014;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.tarent.invio.linuxtag2014.map.InvioIndoorMap;
import de.tarent.invio.linuxtag2014.map.InvioMultiMap;
import de.tarent.invio.linuxtag2014.products.Product;
import de.tarent.invio.linuxtag2014.products.ProductItem;
import de.tarent.invio.linuxtag2014.products.ProductManager;
import de.tarent.invio.linuxtag2014.products.Talk;
import de.tarent.invio.linuxtag2014.task.CachedDownloadGroupDataTask;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.MapServerClientImpl;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The MapActivity is currently the OSM-MapView.
 */
public class MapActivity extends AbstractMapActivity implements ItemizedIconOverlay.OnItemGestureListener<ProductItem> {
    private ItemizedIconOverlay<ProductItem> productOverlay;

    private InvioMultiMap multiMap;

    private ProgressDialog progressDialog;

    private String currentMapShortName;

    private boolean wifiOff;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.mapview, R.id.osmmapview);

        final ActionBar actionBar = getActionBar();
        actionBar.hide();

        handleWifiStatus();

        createProductOverlay();
        mapView.getOverlays().add(productOverlay);
        processPOIInIntent();
    }

    private void handleWifiStatus() {
        final Context ctx = this.getApplicationContext();
        final WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        final int wifiState = wifiManager.getWifiState();

        switch (wifiState) {
            // For all three states (disabled, disabling and unknown) show the same toast
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
                showWifiNotEnabledDialog();
                wifiOff = true;
                break;

            default:
                // Everything  is fine, if WiFi is enabled
        }
    }

    /**
     * Show dialog if WiFi is disabled.
     */
    private void showWifiNotEnabledDialog() {
        final AlertDialog alertDialog = createWifiNotEnabledDialog();
        alertDialog.show();
    }

    private AlertDialog createWifiNotEnabledDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
        alertDialog.setTitle(getString(R.string.wifi_not_enabled_dialog_title));
        alertDialog.setMessage(getString(R.string.wifi_not_enabled_dialog_message));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.dialog_button_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return alertDialog;
    }

    public boolean getWifiOff() {
        return wifiOff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onItemSingleTapUp(final int index, final ProductItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        final Product product = item.getProduct();
        SpannableString messageWithLink = null;
        if(product.getType() == Product.TYPE_BOOTH) {
            messageWithLink = new SpannableString(product.getNameLineTwo() + "\n\n" + product.getCategories());
        } else {
            messageWithLink = new SpannableString(getString(R.string.room_dialog_current_talk_title)
                    + "\n\n" + findCurrentTalk(product));
        }

        Linkify.addLinks(messageWithLink, Linkify.ALL);
        builder.setTitle(product.getNameLineOne())
                .setMessage(messageWithLink)
                .setNeutralButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        final Dialog d = builder.create();
        d.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        return true;
    }

    /**
     * Find the current talk from the list of talks for the given product (room) and return it as a string to show in
     * dialog. If no current talk were found then it will return "keiner".
     *
     * @param product representing room with a list of talks
     * @return string with a name and link of the talk or "keiner" if no current talk were found
     */
    private String findCurrentTalk(final Product product) {

        String result = "keiner";

        if(product == null) {
            return result;
        }

        //Get the current time
        final Calendar currentTime = new GregorianCalendar();
        currentTime.setTime(new Date());

        //Get the list of talks from the room
        final List<Talk> talks = product.getTalks();

        if(talks == null || talks.isEmpty()) {
            return result;
        }

        for(Talk talk : talks) {
            final Calendar startTime = talk.getStartTime();
            final Calendar endTime = talk.getEndTime();

            //For each talk check if current time is between the startTime end endTime of the talk
            if(currentTime.after(startTime) && currentTime.before(endTime)) {
                return talk.getName() + "\n\n" + talk.getLink();
            }

        }

        return result;
    }

    /**
     * TODO: Hardcoded buttons for the tarent AG group, from E to 3 level + "Anbau".
     *
     * @param maps list of the maps
     */
    public void createLevelButtons(List<InvioIndoorMap> maps){
        findViewById(R.id.button_autodetect).setVisibility(View.VISIBLE);

        final ToggleButton button0 = (ToggleButton) findViewById(R.id.button_floor0);
        button0.setVisibility(View.VISIBLE);

        final ToggleButton button1 = (ToggleButton) findViewById(R.id.button_floor1);
        button1.setVisibility(View.VISIBLE);
    }

    /**
     * This is the listener for the hardcoded Floorbuttons.
     * @param view the button that was clicked
     */
    public void onFloorButtonClick(View view) { //NOSONAR
        switch (view.getId()) {
            case R.id.button_autodetect:
                multiMap.switchMap(null);
                break;
            case R.id.button_floor0:
                switchMap(view);
                setLevelFocus(1);
                break;
            case R.id.button_floor1:
                switchMap(view);
                setLevelFocus(0);
                break;
            default:
                break;
        }
    }

    private void switchMap(View view) {
        final Button button = (Button) view;
        multiMap.switchMap(multiMap.findMapByName(button.getText().toString()));
    }

    /**
     * TODO: Missing comment
     *
     * @param level
     */
    public void setLevelFocus(final int level) { //NOSONAR
        resetButtons();
        switch (level) {
            case 0:
                ((ToggleButton)(findViewById(R.id.button_floor1))).setChecked(true);
                currentMapShortName = ((ToggleButton)(findViewById(R.id.button_floor1))).getTextOn().toString();
                break;
            case 1:
                ((ToggleButton)(findViewById(R.id.button_floor0))).setChecked(true);
                currentMapShortName = ((ToggleButton)(findViewById(R.id.button_floor0))).getTextOn().toString();
                break;
            default:
                break;
        }
    }

    private void resetButtons() {
        ((ToggleButton)(findViewById(R.id.button_floor0))).setChecked(false);
        ((ToggleButton)(findViewById(R.id.button_floor1))).setChecked(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongPress(int index, ProductItem item) {
        return false;
    }

    public ItemizedIconOverlay<ProductItem> getProductOverlay() {
        return productOverlay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }


    /**
     * Start scan on resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.mapview_searchview);
        textView.clearFocus();
        startScan();
        if (getIntent().getBooleanExtra("ShowShoppingList", false)) {
            showShoppingListOnMap();
        }
    }

    /**
     * Stop scan on resume
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
        if(multiMap != null) {
            multiMap.detach();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFingerprintManager() {
        fingerprintManager = new FingerprintManager(this, mapServerClient, mapView, mapName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void restoreFingerprintManagerFromBundle(final Bundle savedInstanceState) {
        fingerprintManager = new FingerprintManager(this, mapServerClient, mapView, mapName, false);
        final String fingerprintsJson = savedInstanceState.getString(FINGERPRINTS_JSON);
        fingerprintManager.setFingerprintsJson(fingerprintsJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMultiLevelFingerprintManager(final Bundle savedInstanceState) {
        fingerprintManager = new FingerprintManager(this, null, mapView, null, false);
        downloadGroupData(getString(R.string.map_default_name));
            // TODO: this does not work because it doesn't hold multi-level-data yet:
            if (savedInstanceState != null) {
                final String fingerprintsJson = savedInstanceState.getString(FINGERPRINTS_JSON);
                fingerprintManager.setFingerprintsJson(fingerprintsJson);
            }
    }

    /**
     * TODO: Missing comment
     *
     * @param v the search view
     * @param event the event triggering the placement of markers on the map
     * @return if the event was the right one, the given view was a search field and everything worked true is returned
     */
    protected boolean highlightProductsOnMap(TextView v, KeyEvent event) {
        if ((event != null) && (v instanceof AutoCompleteTextView)) {
            final AutoCompleteTextView view = (AutoCompleteTextView) v;
            final List<String> productNames = getProductsToHighlight(view);
            placeHighlightedProductsOnMap(productNames);
            view.dismissDropDown();
            return true;
        }

        return false;
    }

    private List<String> getProductsToHighlight(AutoCompleteTextView view) {
        final ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) view.getAdapter();
        final List<String> productNames = new ArrayList<String>(arrayAdapter.getCount());
        for(int i = 0; i < arrayAdapter.getCount(); i++) {
            productNames.add(arrayAdapter.getItem(i));
        }
        return productNames;
    }

    /**
     * This method will highlight (red icon) the given list of the product names on a current level map.
     *
     * TODO: This is Linuxtag 2014 specific code, remove or modify it for Invio
     *
     * @param productNames List of the products which should be highlighted
     */
    private void placeHighlightedProductsOnMap(List<String> productNames) {
        //Remove all items first
        productOverlay.removeAllItems();
        //Then get product items only for the current selected or detected map
        final Set<ProductItem> productItems = multiMap.getMapByShortName(currentMapShortName).getProductItems();
        for (ProductItem productItem : productItems) {
            final Product product = productItem.getProduct();
            if(productNames.contains(product.getShortName())) {
                addProductItemToMap(productItem, true);
            } else {
                addProductItemToMap(productItem, false);
            }
        }
        mapView.postInvalidate();
        hideSoftKeyboard();
        // This search must not be overwritten by the shopping list items:
        getIntent().removeExtra("ShowShoppingList");
    }

    private void processPOIInIntent() {
        final String productName = getIntent().getExtras().getString("PoiProductName");
        if (productName != null && !productName.isEmpty()) {
            addProductToMap(productName);
        }
    }

    private void createProductOverlay() {
        final Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.poi);
        final Drawable icon = new BitmapDrawable(getResources(), bm);
        productOverlay = new ItemizedIconOverlay<ProductItem>(
                new ArrayList<ProductItem>(),
                icon,
                this,
                new DefaultResourceProxyImpl(this)) {
        };
    }

    private void showShoppingListOnMap() {
        productOverlay.removeAllItems();
        // TODO: Code nur auskommentiert, da man es evtl. später braucht
//        for (Article article : VisitingList.getMainShoppingList().articles) {
//            if (!article.isChecked()) {
//                addProductToMap(article.getProduct().getShortName());
//            }
//        }
        mapView.postInvalidate();
    }

    /**
     * TODO: Missing comment
     */
    private void addProductToMap(final String productName) {
        final ProductItem item = ProductManager.getMainProductManager().getProductItemByName(productName);
        final Drawable icon = new BitmapDrawable(getResources(), item.getProduct().getIcon());
        item.setMarker(icon);
        productOverlay.addItem(item);
    }

    /**
     * TODO: Missing comment
     */
    public void addProductItemToMap(final ProductItem productItem, final boolean isHighlighted) {
        if(isHighlighted) {
            final Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.poi);
            final Drawable icon = new BitmapDrawable(getResources(), bm);
            productItem.setMarker(icon);
        } else {
            final Drawable icon = new BitmapDrawable(getResources(), productItem.getProduct().getIcon());
            productItem.setMarker(icon);
        }
        productOverlay.addItem(productItem);
    }

    private void hideSoftKeyboard() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * TODO: Missing comment
     */
    public void configureSearch() {
        final BaseSearchHelper mapSearchHelper = new BaseSearchHelper(this,
                R.id.mapview_searchview, getOnSearchItemClickListener()) {
            @Override
            protected void addSpecialListener(AutoCompleteTextView textView) {
                // This listener initiates the placement of a whole bunch of articles,
                // depending on what the completion shows after the user confirms with his "Enter"/Search button
                textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        return highlightProductsOnMap(v, event);
                    }
                });

            }
        };
        mapSearchHelper.configureSearchView();
    }

    private AdapterView.OnItemClickListener getOnSearchItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final List<String> productName = new ArrayList<String>(1);
                productName.add(((TextView)v).getText().toString());
                placeHighlightedProductsOnMap(productName);
            }
        };
    }

    /**
     * show ProgressDialog
     */
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Bitte warten :)");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    /**
     * dismiss ProgressDialog
     */
    public void dismissProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    private void downloadGroupData(final String groupName) {
        //TODO: Download it NOT EVERY TIME the activity is created
        showProgressDialog();

        final String serverEndpoint = getResources().getString(de.tarent.nic.android.base.R.string.server_endpoint);
        final MapServerClient mapServerClient = new MapServerClientImpl(serverEndpoint);
        final CachedDownloadGroupDataTask task = new CachedDownloadGroupDataTask(makeGroupDataDownloadListener(),
                groupName,mapServerClient);
        task.execute();
    }

    private DownloadListener<File> makeGroupDataDownloadListener() {
        return new DownloadListener<File>() {
            @Override
            public void onDownloadFinished(DownloadTask task, boolean success, File data) {
                if(success) {
                    //TODO: Create multilevel fingerprint manager here somehow
                    // TODO: Don't forget about the ways, angle and scale for the user locator, dead reckoning and
                    // particle filter
                    multiMap = new InvioMultiMap(data, MapActivity.this, mapView);
                } else {
                    //TODO: Shit happens - could not download, unzip or find cache group data. Inform the user about it.
                    //TODO: But what should we also do? The app is useless if we don't have any data.
                    Toast.makeText(getApplicationContext(), getString(R.string.zip_download_failed), 7000).show();
                }
            }
        };
    }
}
