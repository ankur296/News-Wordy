package corp.seedling.news.wordy.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import corp.seedling.news.wordy.MainActivityFragment;
import corp.seedling.news.wordy.R;

/**
 * Created by Ankur Nigam on 13-10-2015.
 */
public class Utility {

    public static String getSortByPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        switch(prefs.getString(context.getString(R.string.pref_sort_by_key), "def")){

            case "0": // Relevance
                return  "NewsSortBy=%27Relevance%27";

            case "1": //Date
                return "NewsSortBy=%27Date%27";

            default: //Date
                return "NewsSortBy=%27Date%27";
        }
    }

    public static String getCategoryPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        switch(prefs.getString(context.getString(R.string.pref_category_key), "def")){

            case "8": // All
                return "";

            case "7": //Sports
                return "&NewsCategory=%27rt_Sports%27";

            case "6": // Politics
                return "&NewsCategory=%27rt_Politics%27";

            case "5": //Entertainment
                return "&NewsCategory=%27rt_Entertainment%27";

            case "4": //Health
                return "&NewsCategory=%27rt_Health%27";

            case "3": //Business
                return "&NewsCategory=%27rt_Business%27";

            case "2": //World
                return "&NewsCategory=%27rt_World%27";

            case "1": //US
                return "&NewsCategory=%27rt_US%27";

            case "0": //ScienceAndTechnology
                return "&NewsCategory=%27rt_ScienceAndTechnology%27";

            default: //All
                return "";
        }
    }

    public static String getAdultPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        switch(prefs.getString(context.getString(R.string.pref_adult_key), "def")){

            case "2": //OFF
                return "";

            case "1": //Moderate
                return "&Adult=%27Moderate%27";

            case "0": //Strict
                return "&Adult=%27Strict%27";

            default: //Off
                return "";
        }

    }
}
