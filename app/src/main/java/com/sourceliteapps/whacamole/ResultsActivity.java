/*
 * Copyright (C) 2017 Kevin Kasamo
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

package com.sourceliteapps.whacamole;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ResultsActivity extends Activity implements SoundPool.OnLoadCompleteListener, AddNameView.OnNameAddedListener {

    private ListView listViewOne;
    private View contentView;
    private ViewAdapter adapter;
    private ArrayList<ResultsInfo> resultsArray;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageView imageViewOne;
    private Button buttonOne;
    private int score;
    private String scoreList;
    public static final String SCORE_LIST_ARRAY = "scoreListArray";
    private SoundPool mSoundPool;
    private int endSoundId;
    private Gson gson;
    private InputMethodManager inputMethodManager;
    private AddNameView mAddNameView;
    private int visibilityCounter = 0;
    private boolean viewTouchAllowed; // blocks touch while AddNameView class is visible
    private ScheduledThreadPoolExecutor stpe;
    private LaunchHighScoreBanner mLaunchHighScoreBanner = new LaunchHighScoreBanner();
    private Future<?> futureDisplayBanner;
    private FrameLayout frameLayoutOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);

        frameLayoutOne = (FrameLayout) findViewById(R.id.frameLayout1);

        listViewOne = (ListView) findViewById(R.id.listView1);

        imageViewOne = (ImageView) findViewById(R.id.imageView1);

        contentView = this.findViewById(android.R.id.content);

        Drawable drawableImage = getResources().getDrawable(R.drawable.high_score_en);
        imageViewOne.setImageDrawable(drawableImage);

        mSharedPreferences = getSharedPreferences("molePrefs", MODE_PRIVATE);

        inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        stpe = new ScheduledThreadPoolExecutor(1);

        mAddNameView = new AddNameView(this);

        mAddNameView.setOnNameAddedListener(this);

        gson = new Gson();

        Intent intent = getIntent();

        buttonOne = (Button) findViewById(R.id.button1);

        frameLayoutOne.post(new Runnable() {

            @Override
            public void run() {

                imageViewOne.getLayoutParams().width = (int) Math.round(0.8 * (double) StartActivity.screenWidth);
                imageViewOne.getLayoutParams().height = (int) Math.round(0.2 * (double) StartActivity.screenHeight);

            }

        });

        buttonOne.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    view.setAlpha(0.3f);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

                    view.setAlpha(1.0f);

                    Intent intent = new Intent(ResultsActivity.this, GameActivity.class);
                    ResultsActivity.this.startActivity(intent);

                    return true;
                }
                return false;
            }
        });

        resultsArray = new ArrayList<ResultsInfo>();

        if (intent != null) {

            score = intent.getIntExtra("score", 0);

        } // intent null check

        scoreList = mSharedPreferences.getString(SCORE_LIST_ARRAY, "");

        processList();

        // array adapter where you enter the array
        adapter = new ViewAdapter(this, R.layout.listview_row, resultsArray);

        // set array adapter to listview
        listViewOne.setAdapter(adapter);

        mSoundPool = new SoundPool(70, AudioManager.STREAM_MUSIC, 0);

        mSoundPool.setOnLoadCompleteListener(this);

        try {

            AssetManager assetManager = getAssets();

            AssetFileDescriptor descriptor = assetManager.openFd("sounds/end.mp3");
            endSoundId = mSoundPool.load(descriptor, 1);

            descriptor.close();

        } catch (IOException e) {

        }

        imageViewOne.setVisibility(View.GONE);

        viewTouchAllowed = true;

        mAddNameView = new AddNameView(this);

        mAddNameView.setOnNameAddedListener(this);

    } // onCreate

    public void processList() {

        if (scoreList == "") {

            ResultsInfo resultsInfo = new ResultsInfo(this.score, System.currentTimeMillis());
            resultsInfo.rank = 1;
            resultsInfo.currentScoreable = true;

            resultsArray.add(resultsInfo);

            String saveArray = gson.toJson(resultsArray);

            editor = mSharedPreferences.edit();

            editor.putString(SCORE_LIST_ARRAY, saveArray);

            editor.commit();

        } else {

            Type type = new TypeToken<ArrayList<ResultsInfo>>() {
            }.getType();

            resultsArray = gson.fromJson(scoreList, type);

            // clear scorePosition
            for (int u = 0; u < resultsArray.size(); u++) {

                resultsArray.get(u).currentScoreable = false;

            }

            Collections.sort(resultsArray, new ResultsCompare());

            int previousMaxScore = 0;

            if (resultsArray.size() > 0) {

                previousMaxScore = resultsArray.get(0).score;

                if (this.score > previousMaxScore) {

                    // show new high score banner
                    futureDisplayBanner = stpe.scheduleAtFixedRate(mLaunchHighScoreBanner, 1000, 800, TimeUnit.MILLISECONDS);
                }

                ResultsInfo resultsInfo = new ResultsInfo(this.score, System.currentTimeMillis());
                resultsInfo.currentScoreable = true;

                resultsArray.add(resultsInfo);

                // first sort array according to the high score top down
                // array index position 0 is highest score and 1 is next highest and so on
                Collections.sort(resultsArray, new ResultsCompare());

                // add rank
                int rankPosition = 1;
                for (int t = 0; t < resultsArray.size(); t++) {

                    if (t == 0) {

                        resultsArray.get(0).rank = rankPosition;

                    } else if (t > 0) {

                        int previousScore = resultsArray.get(t - 1).score;
                        int thisScore = resultsArray.get(t).score;

                        if (previousScore == thisScore) {

                            resultsArray.get(t).rank = rankPosition;

                        } else {

                            rankPosition++;
                            resultsArray.get(t).rank = rankPosition;

                        }

                    } // if else results.size == 1

                } // for loop add rank

                // remove entries in arraylist what is over 30
                while (resultsArray.size() > 30) {

                    resultsArray.remove(resultsArray.size() - 1);

                }

                String saveArray = gson.toJson(resultsArray);

                editor = mSharedPreferences.edit();

                editor.putString(SCORE_LIST_ARRAY, saveArray);

                editor.commit();

            } // resultsArry larger than 0

        } // else if string not null

    } // processList

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

        mSoundPool.play(endSoundId, 1, 1, 0, 0, 1);

    } // onLoadComplete

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (futureDisplayBanner != null) {
            futureDisplayBanner.cancel(true);
        }

        visibilityCounter = 0;

        ((ViewGroup) contentView).removeView(mAddNameView);

        mSoundPool.release();

        mAddNameView = null;

        stpe.shutdownNow();

    } // onDestroy

    public class ViewAdapter extends ArrayAdapter<ResultsInfo> {

        Context context;
        int resourceId;
        ArrayList<ResultsInfo> list;
        ResultsInfo item;

        public ViewAdapter(Context context, int resource, ArrayList<ResultsInfo> objects) {
            super(context, resource, objects);
            list = objects;
            this.context = context;
            this.resourceId = resource;

        } // ViewAdapter constructor

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;

            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_row, null);

                viewHolder = new ViewHolder();
                viewHolder.relativeLayoutOne = (RelativeLayout) convertView.findViewById(R.id.relativeLayout1);
                viewHolder.textViewTwo = (TextView) convertView.findViewById(R.id.textView2);
                viewHolder.textViewFour = (TextView) convertView.findViewById(R.id.textView4);
                viewHolder.textViewSix = (TextView) convertView.findViewById(R.id.textView6);
                viewHolder.adapterPosition = position;

                convertView.setTag(viewHolder);
                convertView.setTag(R.id.relativeLayout1, viewHolder.relativeLayoutOne);
                convertView.setTag(R.id.textView2, viewHolder.textViewTwo);
                convertView.setTag(R.id.textView4, viewHolder.textViewFour);
                convertView.setTag(R.id.textView6, viewHolder.textViewSix);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            } // if convertView null check

            item = list.get(position);
            final int arrayPosition = position;

            viewHolder.textViewTwo.setText(String.valueOf(list.get(arrayPosition).rank));
            viewHolder.textViewFour.setText(String.valueOf(list.get(arrayPosition).score));
            viewHolder.textViewSix.setText(String.valueOf(list.get(arrayPosition).name));

            if (list.get(arrayPosition).currentScoreable) {

                viewHolder.relativeLayoutOne.setBackgroundColor(Color.argb(255, 255, 255, 153));

                viewHolder.touchable = true;

            } else {

                viewHolder.relativeLayoutOne.setBackgroundColor(Color.parseColor("#5BBA5E"));

                viewHolder.touchable = false;

            }

            viewHolder.relativeLayoutOne.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {

                    ViewHolder viewHolder = (ViewHolder) view.getTag();
                    boolean viewTouchable = viewHolder.touchable;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        if (viewTouchable && viewTouchAllowed) {

                            if (mAddNameView != null) {

                                viewTouchAllowed = false;

                                mAddNameView.setListAndPosition(resultsArray, arrayPosition);
                                mAddNameView.setGravity(Gravity.CENTER);

                                ((ViewGroup) contentView).addView(mAddNameView);
                                mAddNameView.bringToFront();

                            }

                        }

                        return true;
                    }
                    return false;
                }
            });

            return convertView;

        } // getView method

    } // ViewAdapter

    static class ViewHolder {

        RelativeLayout relativeLayoutOne;
        TextView textViewTwo;
        TextView textViewFour;
        TextView textViewSix;
        int adapterPosition;
        boolean touchable;

    } // ViewHolder

    static class ResultsInfo {

        private int score;
        private int rank;
        private long createdTime;
        protected String name = "";
        private boolean currentScoreable = false;

        public ResultsInfo(int score, long createdTime) {

            this.score = score;
            this.createdTime = createdTime;
        }

    } // ResultsInfo

    class ResultsCompare implements Comparator<ResultsInfo> {

        @Override
        public int compare(ResultsInfo r1, ResultsInfo r2) {

            int resultInt = r2.score - r1.score;
            if (resultInt == 0) {
                resultInt = ((int) r2.createdTime) - ((int) r1.createdTime);

            } // resultOne if check

            return resultInt;
        } // compare

    } // ResultsCompare

    public class LaunchHighScoreBanner implements Runnable {

        @Override
        public void run() {

            ResultsActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (visibilityCounter < 8) {
                        if (imageViewOne.getVisibility() == View.GONE) {

                            imageViewOne.setVisibility(View.VISIBLE);


                        } else if (imageViewOne.getVisibility() == View.VISIBLE) {

                            imageViewOne.setVisibility(View.GONE);

                        }

                        visibilityCounter++;

                    }
                }
            });
        } // run

    } // LaunchHighScoreBanner

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(ResultsActivity.this, StartActivity.class);
            ResultsActivity.this.startActivity(intent);

        }

        return super.onKeyDown(keyCode, event);

    } // onKeyDown

    public void launchIt(int x) {

        Toast.makeText(ResultsActivity.this, "the value of x is " + x, Toast.LENGTH_SHORT).show();

    } // launchit

    @Override
    public void onNameAdded(boolean added) {

        // hide software keyboard
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        if (added) {

            ((ViewGroup) contentView).removeView(mAddNameView);

            String saveArray = gson.toJson(resultsArray);

            editor = mSharedPreferences.edit();

            editor.putString(SCORE_LIST_ARRAY, saveArray);

            editor.commit();

            if (adapter != null) {

                adapter.notifyDataSetChanged();

            }

        } else {

            ((ViewGroup) contentView).removeView(mAddNameView);

        }

        viewTouchAllowed = true;

    } // onNameAdded

} // ResultsActivity