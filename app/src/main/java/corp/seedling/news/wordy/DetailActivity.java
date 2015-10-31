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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "********* oncreate enter *********");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (findViewById(R.id.fragment_detail) == null
                &&
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {

            Log.e(TAG, "oncreate detail act: fetch data");
            Bundle arguments = new Bundle();
            arguments.putString("url", getIntent().getStringExtra("url"));
            arguments.putString("keyword", getIntent().getStringExtra("keyword"));

            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_detail);
            detailFragment.updateContent(
                    getIntent().getStringExtra("url"),
                    getIntent().getStringExtra("keyword"));

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "********* onpause enter *********");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "********* onstart enter *********");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "********* onresumeenter *********");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "********* onstop enter *********");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "********* ondestroy enter *********");
    }

}