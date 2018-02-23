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
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameActivity extends Activity implements SoundPool.OnLoadCompleteListener,
                                                      MediaPlayer.OnPreparedListener, GamePanel.GameOverListener {

    private Bitmap sprites;
    private Bitmap background;
    private Bitmap speedBanner;
    private SoundPool mSoundPool;
    private MediaPlayer mMediaPlayer;
    private LoadImages loadImages;
    private ScheduledThreadPoolExecutor stpe;
    private Future<?> timerFuture;
    private int[] soundIds = new int[5];
    private Handler mHandler;
    private FrameLayout frameLayoutOne;
    private GamePanel gamePanel;
    private boolean loadable = true;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        stpe = new ScheduledThreadPoolExecutor(8);

        setContentView(R.layout.activity_game);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        frameLayoutOne = (FrameLayout) findViewById(R.id.FrameLayout1);

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

    } // onCreate

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        mSoundPool = new SoundPool(70, AudioManager.STREAM_MUSIC, 0);

        mSoundPool.setOnLoadCompleteListener(this);

        try {

            AssetManager assetManager = getAssets();

            AssetFileDescriptor descriptor = assetManager.openFd("sounds/strike1.mp3");
            soundIds[0] = mSoundPool.load(descriptor, 1);

            descriptor = assetManager.openFd("sounds/strike2.mp3");
            soundIds[1] = mSoundPool.load(descriptor, 1);

            descriptor = assetManager.openFd("sounds/strike3.mp3");
            soundIds[2] = mSoundPool.load(descriptor, 1);

            descriptor = assetManager.openFd("sounds/speed_up.mp3");
            soundIds[3] = mSoundPool.load(descriptor, 1);

            descriptor = assetManager.openFd("sounds/woosh.mp3");
            soundIds[4] = mSoundPool.load(descriptor, 1);

            descriptor.close();

        } catch (IOException e) {

        }

         try {

                AssetFileDescriptor assetDescriptor = getAssets().openFd("sounds/game_play.mp3");
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVolume(0.5f, 0.5f);
                mMediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
                mMediaPlayer.prepareAsync();

         }

            catch (Exception e) {

        }

        mMediaPlayer.setOnPreparedListener(this);

    } // onResume

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

        // onLoadComplete is called multiple times and was causing problems with launching
        // many instances of LoadImagesTask, so to fix this problem the boolean variable
        // loadable is used to launch only one instance of LoadImagesTask
        if(loadImages == null && loadable) {

            loadable = false;

            loadImages = new LoadImages();

            if(!stpe.isShutdown()) {

                stpe.schedule(loadImages, 200, TimeUnit.MILLISECONDS);

            }
        }

    } // onLoadComplete

    @Override
    public void onPrepared(MediaPlayer mp) {

        mMediaPlayer.start();

    }// onPrepared

    @Override
    protected void onPause() {
        super.onPause();

        progressBar.setVisibility(View.GONE);

        loadable = true;

        loadImages = null;

        if(gamePanel != null) {

            gamePanel = null;

            if(timerFuture != null) {

                timerFuture.cancel(true);
            }

            stpe.shutdownNow();

            finish();

        }

        mSoundPool.release();

        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();

    } // onPause

    @Override
    public void onGameOver(int score) {

        Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
        intent.putExtra("score", score);
        GameActivity.this.startActivity(intent);

    } // onGameOver

    public class LoadImages implements Runnable {

        @Override
        public void run() {

            // load bitmap for screen background
            BitmapFactory.Options bfoSize = new BitmapFactory.Options();

            bfoSize.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(getResources(), R.drawable.background, bfoSize);

            int  backgroundImageWidth = bfoSize.outWidth;
            int  backgroundImageHeight = bfoSize.outHeight;

            BitmapFactory.Options bfo = new BitmapFactory.Options();

            if (backgroundImageHeight > StartActivity.screenHeight || backgroundImageWidth > StartActivity.screenWidth) {
                if (backgroundImageWidth > backgroundImageHeight) {

                    bfo.inSampleSize = (Math.round((float)backgroundImageHeight / (float) StartActivity.screenHeight));

                } else {

                    bfo.inSampleSize = (Math.round((float) backgroundImageWidth / (float) StartActivity.screenWidth));
                }
            }

            background = BitmapFactory.decodeResource(getResources(), R.drawable.background, bfo);

            speedBanner = BitmapFactory.decodeResource(getResources(), R.drawable.speed_up_en);

            // load Bitmap for sprite images
            InputStream ims;
            try {

                ims = GameActivity.this.getAssets().open("images/sprite_sheet_75.png");
                sprites = BitmapFactory.decodeStream(ims);

            } catch (IOException e) {

            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    progressBar.setVisibility(View.GONE);

                    Bitmap[] bitmaps = new Bitmap[] {sprites, background, speedBanner};

                    gamePanel = new GamePanel(GameActivity.this, bitmaps, mSoundPool, soundIds, stpe, timerFuture);

                    gamePanel.setGameOverListener(GameActivity.this);

                    frameLayoutOne.addView(gamePanel);

                }
            });
        } // run

    } // LoadImages

} // GameActivity