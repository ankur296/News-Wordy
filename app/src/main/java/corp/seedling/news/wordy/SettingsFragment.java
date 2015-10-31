package corp.seedling.news.wordy;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_by_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_category_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_adult_key)));
    }


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_by_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_category_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_adult_key)));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_by_key))){

            switch(sharedPreferences.getString(getString(R.string.pref_sort_by_key), "def")){

                case "0": // Relevance
                    MainActivityFragment.SORT_BY_COMPONENT = "NewsSortBy=%27Relevance%27";
                    break;

                case "1": //Date
                    MainActivityFragment.SORT_BY_COMPONENT = "NewsSortBy=%27Date%27";
                    break;

                default: //Date
                    MainActivityFragment.SORT_BY_COMPONENT = "NewsSortBy=%27Date%27";
                    break;
            }
        }
        else if (key.equals(getString(R.string.pref_adult_key))){

            switch(sharedPreferences.getString(getString(R.string.pref_adult_key), "def")){

                case "2": //OFF
                    MainActivityFragment.ADULT_COMPONENT = "";
                    break;

                case "1": //Moderate
                    MainActivityFragment.ADULT_COMPONENT = "&Adult=%27Moderate%27";
                    break;

                case "0": //Strict
                    MainActivityFragment.ADULT_COMPONENT = "&Adult=%27Strict%27";
                    break;
                default: //Off
                    MainActivityFragment.ADULT_COMPONENT = "";
                    break;
            }
        }
        else if (key.equals(getString(R.string.pref_category_key))){

            switch(sharedPreferences.getString(getString(R.string.pref_category_key), "def")){

                case "8": // All
                    MainActivityFragment.CATEGORY_COMPONENT = "";
                    break;

                case "7": //Sports
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_Sports%27";
                    break;

                case "6": // Politics
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_Politics%27";
                    break;

                case "5": //Entertainment
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_Entertainment%27";
                    break;

                case "4": //Health
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_Health%27";
                    break;

                case "3": //Business
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_Business%27";
                    break;

                case "2": //World
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_World%27";
                    break;

                case "1": //US
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_US%27";
                    break;

                case "0": //ScienceAndTechnology
                    MainActivityFragment.CATEGORY_COMPONENT = "&NewsCategory=%27rt_ScienceAndTechnology%27";
                    break;

                default: //All
                    MainActivityFragment.CATEGORY_COMPONENT = "";
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
