package de.tarent.nic.android.kunde;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.task.NicOsmParser;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.MultiLevelFingerprintManager;


/**
 * The MapActivity is currently the OSM-MapView.
 */
public class MapActivity extends AbstractMapActivity {

    protected static final int MENU_LEVEL_GROUP = 12345;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.mapview, R.id.osmmapview);

        //Hardcoded buttons for 30c3 are set visible
        if (fingerprintManager instanceof MultiLevelFingerprintManager) {
            findViewById(R.id.button_autodetect).setVisibility(View.VISIBLE);
            findViewById(R.id.button_floor0).setVisibility(View.VISIBLE);
            findViewById(R.id.button_floor1).setVisibility(View.VISIBLE);
            findViewById(R.id.button_floor2).setVisibility(View.VISIBLE);
            findViewById(R.id.button_floor3).setVisibility(View.VISIBLE);
            findViewById(R.id.button_floor4).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start scan on resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        startScan();
    }


    /**
     * Stop scan on resume
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (fingerprintManager instanceof MultiLevelFingerprintManager) {
            addLevelsSubmenu(menu);
        }
        return true;
    }

    /**
     * This is the listener for the hardcoded Floorbuttons.
     * @param view the button that was clicked
     */
    public void onFloorButtonClick(View view) { //NOSONAR
        switch (view.getId()) {
            case R.id.button_autodetect:
                ((MultiLevelFingerprintManager) fingerprintManager).switchLevel();
                // This is only temporary until the highlighting is fixed.
                setLevelFocus(((MultiLevelFingerprintManager) fingerprintManager).getLevel());
                break;
            case R.id.button_floor0:
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(0);
                break;
            case R.id.button_floor1:
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(1);
                break;
            case R.id.button_floor2:
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(2);
                break;
            case R.id.button_floor3:
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(3);
                break;
            case R.id.button_floor4:
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(4);
                break;
            default:
                break;
        }

    }

    public void setLevelFocus(final int level) { //NOSONAR
        resetButtons();
        switch (level) {
            case 0:
                findViewById(R.id.button_floor0).setPressed(true);
                break;
            case 1:
                findViewById(R.id.button_floor1).setPressed(true);
                break;
            case 2:
                findViewById(R.id.button_floor2).setPressed(true);
                break;
            case 3:
                findViewById(R.id.button_floor3).setPressed(true);
                break;
            case 4:
                findViewById(R.id.button_floor4).setPressed(true);
                break;
            default:
                break;
        }
    }

    private void resetButtons() {
        findViewById(R.id.button_floor0).setPressed(false);
        findViewById(R.id.button_floor1).setPressed(false);
        findViewById(R.id.button_floor2).setPressed(false);
        findViewById(R.id.button_floor3).setPressed(false);
        findViewById(R.id.button_floor4).setPressed(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // The items in this group exist only for multi-level-maps:
        handleLevelGroupItems(item);

        switch (item.getItemId()) {
            case R.id.feedback_menu:
                openFeedbackEmail();
                return true;
            case R.id.schedule_menu:
                openSchedule();
                return true;
            case R.id.imprint_menu:
                openImprint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean handleLevelGroupItems(MenuItem item) {
        if (item.getGroupId() == MENU_LEVEL_GROUP) {
            if (item.getItemId() == Integer.MAX_VALUE) {
                ((MultiLevelFingerprintManager) fingerprintManager).switchLevel();
            } else {
                ((MultiLevelFingerprintManager) fingerprintManager).setLevel(item.getItemId());
            }
            return true;
        }
        return false;
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
        fingerprintManager = new MultiLevelFingerprintManager(this, mapServerClient, mapView, mapName,
                new NicOsmParser());
        // TODO: this does not work because it doesn't hold multi-level-data yet:
        if (savedInstanceState != null) {
            final String fingerprintsJson = savedInstanceState.getString(FINGERPRINTS_JSON);
            fingerprintManager.setFingerprintsJson(fingerprintsJson);
        }
    }

    /**
     * Add one item for each available building-level to the menu, as well as an "autodetect" option.
     *
     * @param menu the Menu which will have the items added
     */
    private void addLevelsSubmenu(final Menu menu) {
        final MultiLevelFingerprintManager mlfm = (MultiLevelFingerprintManager) fingerprintManager;
        final SubMenu levelMenu = menu.addSubMenu("Floors");
        final int minLevel = mlfm.getMinLevel();
        final int maxLevel = mlfm.getMaxLevel();
        for (int i = minLevel; i <= maxLevel; i++) {
            levelMenu.add(MENU_LEVEL_GROUP, i, i, "floor " + i);
        }
        levelMenu.add(MENU_LEVEL_GROUP, Integer.MAX_VALUE, maxLevel, "Autodetect");
    }

    private void openFeedbackEmail() {
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:" + getString(R.string.feedback_email_address)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_body));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_chooser)));
    }

    private void openSchedule() {
        try {
            final Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getString(R.string.schedule_app_package_name)));
            startActivity(Intent.createChooser(marketIntent, getString(R.string.schedule_chooser)));
        } catch (android.content.ActivityNotFoundException anfe) {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" +
                            getString(R.string.schedule_app_package_name)));
            startActivity(Intent.createChooser(browserIntent, getString(R.string.schedule_chooser)));
        }
    }

    private void openImprint() {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.imprint_uri)));
        startActivity(Intent.createChooser(browserIntent, getString(R.string.imprint_chooser)));
    }

}
