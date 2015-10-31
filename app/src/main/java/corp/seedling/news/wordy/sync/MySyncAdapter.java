package corp.seedling.news.wordy.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import corp.seedling.news.wordy.R;
import corp.seedling.news.wordy.data.WordContract.WordEntry;
import corp.seedling.news.wordy.utilities.Parser;

public class MySyncAdapter extends AbstractThreadedSyncAdapter {

    Context mContext;
    private static final String TAG = MySyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;
    public static final int SYNC_INTERVAL =  24*60*60 * 1;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    public static final String ACTION_DATA_UPDATED =
            "corp.seedling.news.wordy.ACTION_DATA_UPDATED";

    //pref related
    final String BASE_URL = "https://api.datamarket.azure.com/Bing/Search/v1/News?";
    public static  String SORT_BY_COMPONENT = "NewsSortBy=%27Date%27";
    public static String ADULT_COMPONENT = "";
    public static String CATEGORY_COMPONENT = "";

    public MySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    public MySyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

//        #Step1: Get the list of favorite words
        ArrayList<String> favList = getFavWordsList(mContext);

//        #Step2: Loop over the list of favorite words and check for today's news
        boolean hotToday = false;
        ContentValues values;
        String word;
        int updatedRows = 0;
        for (int i = 0 ; i < favList.size() ; i++) {
            word = favList.get(i);
            hotToday = checkNewsToday(word);

//        #Step3: Make an entry of 1 in the "Hot Today" col
            if (hotToday) {

                values = new ContentValues();
                values.put(WordEntry.COLUMN_WORD, word);
                values.put(WordEntry.COLUMN_HOT_TODAY, 1);

                updatedRows = mContext.getContentResolver().update(
                        WordEntry.CONTENT_URI,
                        values,
                        WordEntry.COLUMN_WORD + " LIKE ?",
                        new String[]{word}
                );
                Log.e(TAG, "updatedRows : "+updatedRows);
            }
// #Step3: Make an entry of 0 in the "Hot Today" col
            else {

                values = new ContentValues();
                values.put(WordEntry.COLUMN_WORD, word);
                values.put(WordEntry.COLUMN_HOT_TODAY, 0);

                updatedRows = mContext.getContentResolver().update(
                        WordEntry.CONTENT_URI,
                        values,
                        WordEntry.COLUMN_WORD + " LIKE ?",
                        new String[]{word}
                );
                Log.e(TAG, "updatedRows : "+updatedRows);
            }
        }

//TODO:update widget only if anything changed
        updateWidgets();

        Cursor cursor = mContext.getContentResolver().query(
                WordEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        Log.e(TAG, "CURSOR:" + DatabaseUtils.dumpCursorToString(cursor));
        cursor.close();
    }

    private void updateWidgets() {
        Log.e(TAG, "send broadcast to widget to UPDATE ");
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private boolean checkNewsToday(String word) {
        HttpURLConnection urlConnection = null;
        String responseJson = null;
        BufferedReader bufferedReader = null;
        try {

            String FINAL_URL =
                    BASE_URL + SORT_BY_COMPONENT + ADULT_COMPONENT + CATEGORY_COMPONENT
                            + "&Query=%27"+ word.replaceAll(" ", "%20") + "%27&%24format=json";

            Uri builtUri = Uri.parse(FINAL_URL).buildUpon().build();

            URL url = new URL(builtUri.toString());
            Log.e(TAG, "final URL = " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            String API_KEY = "giYYX5PVWZc9MMpTssH3IlSIpESY7cA8s25bwzsy2l4";

            String auth = API_KEY + ":" + API_KEY;
            String encodedAuth = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
            urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null)
                return false;

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null)
                stringBuffer.append(line + "\n");

            if (stringBuffer == null)
                return false;

            responseJson = stringBuffer.toString();
            Log.i(TAG, "RESPONSE: " + responseJson);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Incorrect URL");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while opening the connection");
            e.printStackTrace();
            return false;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                    e.printStackTrace();
                }
            }
        }
        try {
            return Parser.isNewsToday(responseJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;

    }

    private ArrayList<String> getFavWordsList(Context context) {

        Cursor cursor = context.getContentResolver().query(
                WordEntry.CONTENT_URI,
                new String[]{WordEntry.COLUMN_WORD},
                null,
                null,
                null
        );

        ArrayList<String> favList = new ArrayList<>();
        while(cursor.moveToNext()){
            favList.add(cursor.getString(cursor.getColumnIndexOrThrow(WordEntry.COLUMN_WORD)));
        }
        cursor.close();
        return favList;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MySyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
}
