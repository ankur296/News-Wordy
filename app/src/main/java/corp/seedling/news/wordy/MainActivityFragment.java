package corp.seedling.news.wordy;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableString;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import corp.seedling.news.wordy.data.WordContract.WordEntry;
import corp.seedling.news.wordy.utilities.Parser;
import corp.seedling.news.wordy.utilities.Utility;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment implements OnItemClickListener{

    @Bind (R.id.fav_button) Button mFavButton;
    @Bind (R.id.meaning_textview) TextView mMeaningsTextView;

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private MeaningsAdapter mMeaningsAdapter;
    private WeakReference<FetchNewsTask> mFetchNewsTaskWeakReference;
    private WeakReference<FetchMeaningTask> mFetchMeaningTaskWeakReference;
    ArrayList<Generic> mGenericList;
    FetchNewsTask mFetchNewsTask;
    FetchMeaningTask mFetchMeaningTask;
    String mKeyword;
    private boolean isFavorite = false;
    private boolean mWaitingForResult = false;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;
    //pref related
    final String BASE_URL = "https://api.datamarket.azure.com/Bing/Search/v1/News?";
    public static  String SORT_BY_COMPONENT = "NewsSortBy=%27Date%27";
    public static String ADULT_COMPONENT = "";
    public static String CATEGORY_COMPONENT = "";

    public MainActivityFragment() {
        Log.e(TAG, "Constructor Called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "********* oncreateview enter");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, rootView);

        if (isFavorite)
            mFavButton.setContentDescription(getString(R.string.unmark_fav));
        else
            mFavButton.setContentDescription(getString(R.string.mark_fav));

        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onclick: isFavorite = " + isFavorite);
                if (isFavorite) {
                    //remove from fav
                    removeFromFav();
                    mFavButton.setBackgroundResource(R.mipmap.ic_action_star_0);
                    isFavorite = false;
                    Log.i(TAG, "onclick: Removed fr FAV");

                } else {
                    //add to fav
                    addToFav();
                    mFavButton.setBackgroundResource(R.mipmap.ic_action_star_10);
                    isFavorite = true;
                    Log.i(TAG, "onclick: Added to FAV");
                }
            }
        });


        //put keyword in edit text
        ((EditText)rootView.findViewById(R.id.clearable_edit)).setText(mKeyword);

        SORT_BY_COMPONENT = Utility.getSortByPreference(getActivity());
        ADULT_COMPONENT = Utility.getAdultPreference(getActivity());
        CATEGORY_COMPONENT = Utility.getCategoryPreference(getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.e(TAG, "********* onactcreated");
        super.onActivityCreated(savedInstanceState);

        if (getActivity().findViewById(R.id.fragment_detail) != null) {
            Log.e(TAG, "*********set choice mode");
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            setActivateOnItemClick(true);
        }

        getListView().setOnItemClickListener(this);
        setListAdapter(mMeaningsAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "********* onattach enter");
        //Start default Welcome search
        mMeaningsAdapter =new MeaningsAdapter(getActivity() , this);
        mKeyword = ((MainActivity)getActivity()).getKeyword();
        if (mKeyword != null)
            startNewAsyncTask(mKeyword);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "********* oncreate enter");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "********* onViewCreated ENTER*********");

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {

            int lastSavedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
            Log.e(TAG, "********* onViewCreated: activate position=*********" + lastSavedPosition);
            setActivatedPosition(lastSavedPosition);

        }else{
            setActivatedPosition(0);
        }
    }

    void setActivatedPosition(int position) {
        Log.e(TAG, "********* setActivatedPosition ENTER : pos = *********" + position);

        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
            mActivatedPosition = position;
        }

        Log.e(TAG, "********* setActivatedPosition mtwopane true: showDetails at *********" + mActivatedPosition);
        mWaitingForResult = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "********* onSaveInstanceState ENTER*********");
        super.onSaveInstanceState(outState);

        if (mActivatedPosition != ListView.INVALID_POSITION) {
            Log.e(TAG, "********* onSaveInstanceState ENTER : save pos = *********" + mActivatedPosition);
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);

            if (mGenericList != null && mGenericList.size() != 0)
                outState.putString("url", mGenericList.get(mActivatedPosition).getText3());

            outState.putString("keyword", mKeyword);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e(TAG, "********* onItemClick: showDetails at *********" + position);
        view.setSelected(true);
        mActivatedPosition = position;
        getListView().setItemChecked(mActivatedPosition, true);
        ((Callback) getActivity()).onItemSelected(mGenericList.get(mActivatedPosition).getText3());

        MyApplication.getInstance().trackEvent(
                "NewsItem Clicked",
                "Heading:" + mGenericList.get(mActivatedPosition).getText1(),
                "position#" + mActivatedPosition);
    }

    private void addToFav(){
        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_WORD, mKeyword);
        values.put(WordEntry.COLUMN_HOT_TODAY, 0);
        getActivity().getContentResolver().insert(WordEntry.CONTENT_URI, values);
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setSelector(R.color.sunshine_dark_blue);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }


    private boolean checkIsFavorite() {
        Cursor cursor = getActivity().getContentResolver().query(
                WordEntry.CONTENT_URI,
                new String[]{WordEntry.COLUMN_WORD},
                WordEntry.COLUMN_WORD + " LIKE ?",
                new String[] {mKeyword},
                null,
                null
        );

        Log.e(TAG, "checkIsFavorite CURSOR:" + DatabaseUtils.dumpCursorToString(cursor));
        if (cursor.moveToFirst())
            return true;
        else
            return false;
    }

    private void removeFromFav(){
        getActivity().getContentResolver().delete(
                WordEntry.CONTENT_URI,
                WordEntry.COLUMN_WORD + " LIKE ?", new String[] {mKeyword}

        );
    }



    public void startNewAsyncTask(String keyword) {
        this.mKeyword = keyword;
        mMeaningsAdapter.setKeyword(keyword);

        mFetchMeaningTask = new FetchMeaningTask(this);
        this.mFetchMeaningTaskWeakReference = new WeakReference<>(mFetchMeaningTask);
        mFetchMeaningTask.execute(keyword);

        mFetchNewsTask = new FetchNewsTask(this);
        this.mFetchNewsTaskWeakReference = new WeakReference<>(mFetchNewsTask);

//        putFavFeature();
    }

    public void putFavFeature(){
        //make star visible
        mFavButton.setVisibility(View.VISIBLE);

        if (checkIsFavorite()){
            isFavorite = true;
            mFavButton.setBackgroundResource(R.mipmap.ic_action_star_10);
        }else{
            isFavorite = false;
            mFavButton.setBackgroundResource(R.mipmap.ic_action_star_0);
        }
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "ondetach");
        if (isFetchNewsTaskPendingOrRunning())
            mFetchNewsTask.cancel(true);
        if (isFetchMeaningTaskPendingOrRunning())
            mFetchMeaningTask.cancel(true);
        super.onDetach();
    }

    private boolean isFetchNewsTaskPendingOrRunning() {
        return this.mFetchNewsTaskWeakReference != null &&
                this.mFetchNewsTaskWeakReference.get() != null &&
                !this.mFetchNewsTaskWeakReference.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    private boolean isFetchMeaningTaskPendingOrRunning() {
        return this.mFetchMeaningTaskWeakReference != null &&
                this.mFetchMeaningTaskWeakReference.get() != null &&
                !this.mFetchMeaningTaskWeakReference.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }


    private class FetchNewsTask extends AsyncTask<String, Void, ArrayList<News>> {

        private final String TAG = FetchNewsTask.class.getSimpleName();

        private WeakReference<MainActivityFragment> fragmentWeakRef;

        private FetchNewsTask(MainActivityFragment fragment) {
            this.fragmentWeakRef = new WeakReference<MainActivityFragment>(fragment);
        }

        @Override
        protected ArrayList<News> doInBackground(String... params) {
            Log.i(TAG, "doinbg");
            HttpURLConnection urlConnection = null;
            String responseJson = null;
            BufferedReader bufferedReader = null;
            try{
                ///NY
//
//                String urlStr = "http://api.nytimes.com/svc/search/v2/articlesearch.json?q="+params[0]+
//                        "&hl=true&fl=headline,web_url&sort=newest&fq=source:The New York Times&api-key=<Key>:10:72954071";
//                URL url = new URL(urlStr);
//                URI uri = null;
//                try {
//                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//                url = uri.toURL();
//                Log.e(TAG,"final URL = " + url);
//
//                urlConnection = (HttpURLConnection)url.openConnection();
//                urlConnection.setRequestMethod("GET");

                ///NY



                /////////////////////// BING !! /////////////////
                String FINAL_URL =
                        BASE_URL + SORT_BY_COMPONENT + ADULT_COMPONENT + CATEGORY_COMPONENT
                                + "&Query=%27"+ params[0].replaceAll(" ", "%20") + "%27&%24format=json";

                Uri builtUri = Uri.parse(FINAL_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());
                Log.e(TAG,"final URL = " + url);

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                String API_KEY = "<Enter Key Here>";

                String auth = API_KEY + ":" + API_KEY;
                String encodedAuth = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
                Log.e("", encodedAuth);
                urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuth);
                /////////////////////// BING !! /////////////////


                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while( (line = bufferedReader.readLine()) != null)
                    stringBuffer.append(line + "\n");

                if (stringBuffer ==null)
                    return null;

                responseJson = stringBuffer.toString();
                Log.i(TAG, "RESPONSE: " + responseJson);
            }

            catch (MalformedURLException e) {
                Log.e(TAG, "Incorrect URL");
                e.printStackTrace();
                return null;
            }

            catch (IOException e) {
                Log.e(TAG, "Error occurred while opening the connection");
                e.printStackTrace();
                return null;
            }
            finally {
                if (urlConnection !=null)
                    urlConnection.disconnect();

                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                return Parser.getWordUsageBing(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(final ArrayList<News> newses) {
            Log.i(TAG, "onPostExecute");
            mGenericList = new ArrayList<>();

            if (this.fragmentWeakRef.get() != null   &&  newses != null) {
                for (News news : newses) {
                    mGenericList.add(new Generic(news.getHeadline(), news.getSnippet(), news.getWebUrl()));
                }
            }
            else{
                Toast.makeText(getActivity(), getString(R.string.no_nwk), Toast.LENGTH_SHORT).show();
            }

            //intimate adapter even if no results
            mMeaningsAdapter.setGenericList(mGenericList);
            mMeaningsAdapter.notifyDataSetChanged();

            Log.e(TAG, "Final generic list length = " + mGenericList.size());
            //inform main act that results are ready
            if (mWaitingForResult && getActivity().findViewById(R.id.fragment_detail) != null) {
                ((Callback) getActivity()).onItemSelected(mGenericList.get(mActivatedPosition).getText3());
                mWaitingForResult = false;
                getListView().smoothScrollToPosition(mActivatedPosition);
            }
        }


    }


    //////////////////Asynctask to Fetch Meanings/////////////
    private class FetchMeaningTask extends AsyncTask<String, Void, ArrayList<Meaning>> {

        private final String TAG = FetchMeaningTask.class.getSimpleName();

        private WeakReference<MainActivityFragment> fragmentWeakRef;

        private FetchMeaningTask(MainActivityFragment fragment) {
            this.fragmentWeakRef = new WeakReference<MainActivityFragment>(fragment);
        }

        @Override
        protected ArrayList<Meaning> doInBackground(String... params) {
            Log.e(TAG, "doinbg");
            HttpURLConnection urlConnection = null;
            String responseJson = null;
            BufferedReader bufferedReader = null;
            try{
                String urlStr = "http://api.wordnik.com:80/v4/word.json/"+params[0]+
                                "/definitions?limit=200&includeRelated=true&sourceDictionaries=wiktionary&useCanonical=true&includeTags=false&api_key=<Enter Key Here>";
                URL url = new URL(urlStr);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();
                Log.e(TAG,"final URL = " + url);

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null)
                    return null;

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while( (line = bufferedReader.readLine()) != null)
                    stringBuffer.append(line + "\n");

                if (stringBuffer ==null)
                    return null;

                responseJson = stringBuffer.toString();
                Log.i(TAG, "RESPONSE: " + responseJson);
            }

            catch (MalformedURLException e) {
                Log.e(TAG, "Incorrect URL");
                e.printStackTrace();
                return null;
            }

            catch (IOException e) {
                Log.e(TAG, "Error occurred while opening the connection");
                e.printStackTrace();
                return null;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }finally {
                if (urlConnection !=null)
                    urlConnection.disconnect();

                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                return Parser.getWordMeanings(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(final ArrayList<Meaning> meanings) {//MEANINGS!!
            Log.i(TAG, "onPostExecute");
            putFavFeature();
            mFetchNewsTask.execute(mKeyword);

            if (meanings != null) {

                String meaningFetched = "";
                SpannableString formattedText = new SpannableString("");

                //set meanings to tv
                if (meanings.size() == 0) {
                    mMeaningsTextView.setText(getString(R.string.no_word));
                } else {

                    for (int i = 0; i < meanings.size(); i++) {
                        meaningFetched += meanings.get(i).getType().toUpperCase(Locale.US);
                        meaningFetched += " : ";
                        meaningFetched += meanings.get(i).getMeaning();
                        if (i != meanings.size() - 1)
                            meaningFetched += "\n\n";
                    }
                    mMeaningsTextView.setText(meaningFetched);
                }
            }
            //smooth scroll to top
            getListView().smoothScrollToPosition(0);

        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "********* ondestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "*********ondestroyview");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onstop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onpause");
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.e(TAG, "onViewStateRestored");
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onItemSelected(String webUrl);
    }
}
