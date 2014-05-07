package de.tarent.nic.android.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.ListPreference;


/**
 * The purpose of this activity is to show the preference fragment. PreferenceActivity is more or less deprecated so
 * we must use the PreferenceFragment instead.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    /**
     * This PreferenceFragment is the actual holder of the preferences screen, and not the activity. Actually,
     * it must be possible to show this fragment directly without the wrapping activity, but it causing problems and no
     * solution were found after ours of searching and debugging. For more information see the Api Documentation:
     * http://developer.android.com/guide/topics/ui/settings.html
     */
    public static class SettingsFragment extends PreferenceFragment
                                         implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // init summary to show the chosen values of the list preferences
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initSummary(getPreferenceScreen().getPreference(i));
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            final String keyOutlierList = getString(R.string.key_pref_outlierAlgorithmList);
            final String keyFilterList = getString(R.string.key_pref_filterAlgorithmList);
            final String keyLocalizationList = getString(R.string.key_pref_localizationMethodList);

            if (key.equals(keyOutlierList) || key.equals(keyFilterList) || key.equals(keyLocalizationList)) {
                updatePreferences(findPreference(key));
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        /**
         * This method is needed to update the summary of the list preferences to show the chosen value.
         *
         * @param p preference
         */
        private void initSummary(final Preference p) {
            if (p instanceof PreferenceCategory) {
                final PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initSummary(cat.getPreference(i));
                }
            } else {
                updatePreferences(p);
            }
        }

        private void updatePreferences(final Preference p) {
            if (p instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) p;
                p.setSummary(listPreference.getEntry());
            }
        }
    }
}
