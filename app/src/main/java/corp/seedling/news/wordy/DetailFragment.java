/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package corp.seedling.news.wordy;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();
    WebView webView;
    String searchText;
    String webUrl = null;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "oncreateview of detail frag ENTER");
        Bundle arguments = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        webView = (WebView)rootView.findViewById(R.id.webview);

        if (arguments != null) { //when detailact Launched on click through intent when in portrait
            webUrl = arguments.getString("url");
            searchText = arguments.getString("keyword");
            Log.e(TAG, "oncreateview of detail frag : arguments != null..url = "+ webUrl + "\n keyword =" +searchText);
        }

        else  if (savedInstanceState != null){ //when frag added on orientation change
            webUrl = savedInstanceState.getString("url");
            searchText = savedInstanceState.getString("keyword");
            Log.e(TAG, "oncreateview of detail frag : savedInstanceState != null..url = "+ webUrl + "\n keyword =" +searchText);
        }

        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPageFinished(WebView view, String url) {
                if (searchText != null && !searchText.equals("")) {
                    webView.findAllAsync(searchText);
                }
            }
        });

        Log.e(TAG, "detail frag : LOAD URL :");
        webView.loadUrl(webUrl);


        return rootView;
    }

    public void updateContent(String url , String keyword){
        Log.e(TAG, "updateContent :");
        webUrl = url;
        searchText = keyword;

        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPageFinished(WebView view, String url) {
                if (searchText != null && !searchText.equals("")) {
                    webView.findAllAsync(searchText);
                }
            }
        });

        webView.loadUrl(webUrl);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "********* onSaveInstanceState ENTER********* url = "+ webUrl + " \n keyword = " +searchText);
        super.onSaveInstanceState(outState);

        outState.putString("url", webUrl);
        outState.putString("keyword", searchText);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "ondetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ondestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "ondestroyview");
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onactcreated");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onviewcreated");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.e(TAG, "onViewStateRestored");
    }
}