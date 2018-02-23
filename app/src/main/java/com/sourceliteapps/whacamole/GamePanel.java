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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback, GameTimer.OnGameTickListener {

    private Bitmap sprites, background, speedBannerBmp;

    private int strikeSoundOne, strikeSoundTwo, strikeSoundThree, speedUpSound, woosh;

    private SoundPool mSoundPool;

    private volatile boolean trumpOrClintonSpeechLaunchable = true;

    private GameOverListener mGameOver;

    private int score = 0;

    private ScheduledThreadPoolExecutor stpe;

    private Future<?> timerfuture;

    // width of each frame in sprite sheet, same as height
    public static final int SPRITE_FRAME_WIDTH = 75;
    public static final int SPRITE_FRAME_HEIGHT = 75;

    public static final int NUMBER_OF_SLOW_MOLES = 25;
    public static final int NUMBER_OF_MEDIUM_MOLES = 35;
    public static final int NUMBER_OF_FAST_MOLES = 55;

    Hole holeOne, holeTwo, holeThree, holeFour, holeFive, holeSix, holeSeven, holeEight, holeNine, holeTen;

    int holeWidth = 0;
    int holeHeight = 0;

    private GameThread thread;

    private ArrayList<Hole> holes = new ArrayList< Hole>();

    private ArrayList< Mole> moles = new ArrayList< Mole>();

    private ArrayList<StruckMole> struckMoles = new ArrayList<StruckMole>();

    // class will create clock to display game time countdown
    public GameTimer mGameTimer;

    // time countdown in seconds
    private volatile int displaySeconds;

    // counts game time, from zero to game end in 250 milliseconds intervals
    private volatile long gameTimerCountUp;

    // graphic banner that crosses screen duing game
    private Banner bannerOne, bannerTwo;

    private Rect srcHoleRect, dstHoleRectOne, dstHoleRectTwo, dstHoleRectThree, dstHoleRectFour, dstHoleRectFive,
            dstHoleRectSix, dstHoleRectSeven, dstHoleRectEight, dstHoleRectNine, dstHoleRectTen;

    private Rect srcBackgroundRect, dstBackgroundRect;

    public GamePanel(Context context, Bitmap[] bitmaps, final SoundPool mSoundPool, int[] soundIds,
           ScheduledThreadPoolExecutor stpe, Future<?> future) {
        super(context);

        this.stpe = stpe;

        this.timerfuture = future;

        sprites = bitmaps[0];
        background = bitmaps[1];
        speedBannerBmp = bitmaps[2];
        strikeSoundOne = soundIds[0];
        strikeSoundTwo = soundIds[1];
        strikeSoundThree = soundIds[2];
        speedUpSound = soundIds[3];
        woosh = soundIds[4];

        this.mSoundPool = mSoundPool;

        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);

        // make the GamePanel focusable so it can handle events
        setFocusable(true);

        // calculate with of hole on screen from screen size, total of 10 holes on screen for mole to pop out of
        holeWidth = (int) Math.round(0.2 * (double) StartActivity.screenWidth);
        holeHeight = (int) Math.round(0.2 * (double)  StartActivity.screenHeight);

        // calculate center points on screen for each hole and create holes
        holeOne = new  Hole((int) (Math.round(0.28 * (double) StartActivity.screenWidth)), (int) (Math.round(0.16 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeTwo = new  Hole((int) (Math.round(0.71 * (double) StartActivity.screenWidth)), (int) (Math.round(0.16 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeThree = new  Hole((int) (Math.round(0.2 * (double) StartActivity.screenWidth)), (int) (Math.round(0.3733 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeFour = new  Hole((int) (Math.round(0.5 * (double) StartActivity.screenWidth)), (int) (Math.round(0.3733 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeFive = new  Hole((int) (Math.round(0.8 * (double) StartActivity.screenWidth)), (int) (Math.round(0.3733 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeSix = new  Hole((int) (Math.round(0.28 * (double) StartActivity.screenWidth)), (int) (Math.round(0.5866 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeSeven = new  Hole((int) (Math.round(0.71 * (double) StartActivity.screenWidth)), (int) (Math.round(0.5866 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeEight = new  Hole((int) (Math.round(0.2 * (double) StartActivity.screenWidth)), (int) (Math.round(0.8 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeNine = new  Hole((int) (Math.round(0.5 * (double) StartActivity.screenWidth)), (int) (Math.round(0.8 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);
        holeTen = new  Hole((int) (Math.round(0.8 * (double) StartActivity.screenWidth)), (int) (Math.round(0.8 * (double)  StartActivity.screenHeight)), holeWidth, holeHeight);

        // store holes in array so that they can be passed to newly created Moles
        holes.add(holeOne);
        holes.add(holeTwo);
        holes.add(holeThree);
        holes.add(holeFour);
        holes.add(holeFive);
        holes.add(holeSix);
        holes.add(holeSeven);
        holes.add(holeEight);
        holes.add(holeNine);
        holes.add(holeTen);

        // get positions of the mole launch places in timer position
        getSlowMoles();
        getMediumMoles();
        getFastMoles();

    } // GamePanel constructor

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    } // surfaceChanged

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // start time for display timer converted from
        // milliseconds to seconds, the onGameTick(int gameTickTime) callback
        // method will countdown this number for each clock second
        displaySeconds =  GameTimer.GAME_DURATION;

        // start new GameTimer
        mGameTimer = new  GameTimer(stpe, timerfuture);

        mGameTimer.setOnGameTickListener(this);

        // at this point the surface is created and the game loop can be safely started
        // create the game loop thread
        thread = new  GameThread(holder, GamePanel.this);
        thread.setRunning(true);
        thread.start();

        // start timer for mole launch positions and future mole positions, start with slow mole
        mGameTimer.startTimer(); // start clock timer

    } // surfaceCreated

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        thread.setRunning(false);

        boolean retry = true;

        while (retry) {

            try {

                thread.join();

                retry = false;

            } catch (InterruptedException e) {

                // try again shutting down the thread
            }
        } // while retry

        // call for clearing memory from operating system heap
        sprites.recycle();
        sprites = null;
        background.recycle();
        background = null;
        speedBannerBmp.recycle();
        speedBannerBmp = null;

    } // surfaceDestroyed

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int touchDownX = (int) Math.round(event.getX());
        int touchDownY = (int) Math.round(event.getY());

        boolean missSound = false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (moles.size() > 0) {

                for (int x = 0; x < moles.size(); x++) {

                    boolean moleTouchedCountX = false;
                    boolean moleTouchedCountY = false;
                    int topTouchAdjustment = 0;

                    if(moles.get(x).headPositionStatus ==  Mole.TOP_DEAD_CENTER) {

                        topTouchAdjustment = (int) Math.round(0.2 * (double) (moles.get(x).getMolePosition().bottom - moles.get(x).getMolePosition().top));


                    } else if (moles.get(x).headPositionStatus ==  Mole.MIDDLE_CYCLE) {

                        topTouchAdjustment = (int) Math.round(0.6 * (double) (moles.get(x).getMolePosition().bottom - moles.get(x).getMolePosition().top));
                    }

                    if (touchDownX > moles.get(x).getMolePosition().left && (touchDownX < moles.get(x).getMolePosition().right)) {
                        moleTouchedCountX = true;
                    }

                    if ((touchDownY - topTouchAdjustment) > moles.get(x).getMolePosition().top && (touchDownY < moles.get(x).getMolePosition().bottom)) {
                        moleTouchedCountY = true;
                    }

                    if (moleTouchedCountX && moleTouchedCountY && moles.get(x).isVisible()) {

                        incrementScore(moles.get(x).getMoleType());

                        moles.get(x).strike();

                        //struckMoles.add(new StruckMole(moles.get(x).getMolePosition(), moles.get(x).getMoleType()));
                        struckMoles.add(new  StruckMole(moles.get(x)));

                        actionSound();

                    } else {

                        missSound = true;

                    }

                } // for

                if(missSound) {

                    mSoundPool.play(woosh, 1, 1, 0, 0, 1);

                }

                missSound = false;

            } // moles.size

        } // if event.getAction

        return super.onTouchEvent(event);

    } // onTouchEvent

    public void actionSound() {

        Random randomSound = new Random();

        switch (randomSound.nextInt(3)) {
            case 0:
                mSoundPool.play(strikeSoundOne, 1, 1, 0, 0, 1);
                break;
            case 1:
                mSoundPool.play(strikeSoundTwo, 1, 1, 0, 0, 1);
                break;
            case 2:
                mSoundPool.play(strikeSoundThree, 1, 1, 0, 0, 1);
        } // switch

    } // actionSound

    public void incrementScore(int moleType) {

                if (moleType == Mole.NORMAL_MOLE) {

                    score += 5;

                } else if (moleType == Mole.BAD_MOLE) {

                    if(score >= 9) {

                        score -= 9;

                    } else {

                        score = 0;

                    }

                } else if (moleType == Mole.GOLDEN_MOLE) {

                    score += 12;

                }

    } // incrementScore

    public void update() {

        // set up drawing position for screen background image
        srcBackgroundRect = new Rect(0, 0, background.getWidth(), background.getHeight());
        dstBackgroundRect = new Rect(0, 0, StartActivity.screenWidth,  StartActivity.screenHeight);

        // setup drawing positions for holes
        srcHoleRect = new Rect(0, 0, 0 + SPRITE_FRAME_WIDTH, 0 + SPRITE_FRAME_HEIGHT);
        dstHoleRectOne = new Rect(holeOne.holePositionX, holeOne.holePositionY, holeOne.holePositionX + holeWidth, holeOne.holePositionY + holeHeight);
        dstHoleRectTwo = new Rect(holeTwo.holePositionX, holeTwo.holePositionY, holeTwo.holePositionX + holeWidth, holeTwo.holePositionY + holeHeight);
        dstHoleRectThree = new Rect(holeThree.holePositionX, holeThree.holePositionY, holeThree.holePositionX + holeWidth, holeThree.holePositionY + holeHeight);
        dstHoleRectFour = new Rect(holeFour.holePositionX, holeFour.holePositionY, holeFour.holePositionX + holeWidth, holeFour.holePositionY + holeHeight);
        dstHoleRectFive = new Rect(holeFive.holePositionX, holeFive.holePositionY, holeFive.holePositionX + holeWidth, holeFive.holePositionY + holeHeight);
        dstHoleRectSix = new Rect(holeSix.holePositionX, holeSix.holePositionY, holeSix.holePositionX + holeWidth, holeSix.holePositionY + holeHeight);
        dstHoleRectSeven = new Rect(holeSeven.holePositionX, holeSeven.holePositionY, holeSeven.holePositionX + holeWidth, holeSeven.holePositionY + holeHeight);
        dstHoleRectEight = new Rect(holeEight.holePositionX, holeEight.holePositionY, holeEight.holePositionX + holeWidth, holeEight.holePositionY + holeHeight);
        dstHoleRectNine = new Rect(holeNine.holePositionX, holeNine.holePositionY, holeNine.holePositionX + holeWidth, holeNine.holePositionY + holeHeight);
        dstHoleRectTen = new Rect(holeTen.holePositionX, holeTen.holePositionY, holeTen.holePositionX + holeWidth, holeTen.holePositionY + holeHeight);

        // add new moles to be displayed that are not visible
        for (int moleIndex = 0; moleIndex < moles.size(); moleIndex++) {

            if (moles.get(moleIndex).getLaunchTime() > gameTimerCountUp && moles.get(moleIndex).getLaunchTime() <= gameTimerCountUp + 250 && (!moles.get(moleIndex).isVisible())) {

                if (moles.get(moleIndex).getCurrentHole().isOccupied) {

                    long postponeLaunchTime = (moles.get(moleIndex).getLaunchTime() + 1000);
                    moles.get(moleIndex).setLaunchTime(postponeLaunchTime);
                    moles.get(moleIndex).setMolePosition();


                } else {

                    // set mole visible to start display, and incrementing animation in renderng method
                    moles.get(moleIndex).setMoleVisible();
                }

            } // moles

        } // add new moles

        // add banner one
        if (gameTimerCountUp >  GameTimer.BANNER_ONE_LAUNCH_TIME * 1000 && bannerOne == null) {

            bannerOne = new Banner(StartActivity.screenWidth,  StartActivity.screenHeight, speedBannerBmp.getWidth(), speedBannerBmp.getHeight());

            mSoundPool.play(speedUpSound, 1, 1, 0, 0, 1);

        }

        // add banner two
        if (gameTimerCountUp >  GameTimer.BANNER_TWO_LAUNCH_TIME * 1000 && bannerTwo == null) {

            bannerTwo = new Banner(StartActivity.screenWidth,  StartActivity.screenHeight, speedBannerBmp.getWidth(), speedBannerBmp.getHeight());

            mSoundPool.play(speedUpSound, 1, 1, 0, 0, 1);

        }

        if (trumpOrClintonSpeechLaunchable) {

            trumpOrClintonSpeechLaunchable = false;

            if(!stpe.isShutdown()) {

                stpe.schedule(new ResetSoundLaunchable(), 250, TimeUnit.MILLISECONDS);

            }

        }

    } // update

    public void render(Canvas canvas) {

        // draw background
        if(background != null && canvas != null) {

            canvas.drawBitmap(background, srcBackgroundRect, dstBackgroundRect, null);

        }

        if(sprites != null && canvas != null) {

            // draw holes
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectOne, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectTwo, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectThree, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectFour, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectFive, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectSix, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectSeven, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectEight, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectNine, null);
            canvas.drawBitmap(sprites, srcHoleRect, dstHoleRectTen, null);

            Paint paint = new Paint();
            paint.setARGB(180, 91, 186, 94);
            canvas.drawRect(0, 0, (float) StartActivity.screenWidth, (float) 0.08 *  StartActivity.screenHeight, paint);

            paint = new Paint();

            float textSize = (float) 0.06 *  StartActivity.screenHeight;
            paint.setTextSize(textSize);

            if (displaySeconds > 59) {

              double displayMinutesMultiplier = (displaySeconds / 60);

              int minutes = (int) Math.floor(displayMinutesMultiplier);

              int seconds = (displaySeconds - (minutes * 60));

              canvas.drawText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds), (float) (0.02 * StartActivity.screenWidth), (float) (0.06 *  StartActivity.screenHeight), paint);
              canvas.drawText(String.valueOf(score), (float) (StartActivity.screenWidth * 0.91 - textSize), (float) (0.06 *  StartActivity.screenHeight), paint);


            } else {

              canvas.drawText("00:" + String.format("%02d", displaySeconds), (float) (0.02 * StartActivity.screenWidth), (float) (0.06 *  StartActivity.screenHeight), paint);
              canvas.drawText(String.valueOf(score), (float) (StartActivity.screenWidth * 0.91 - textSize), (float) (0.06 *  StartActivity.screenHeight), paint);

            }

            // draw moles
            for(int m = 0; m < moles.size(); m++) {

                if(moles.get(m).isVisible()) {

                    Rect moleSrc = moles.get(m).getMoleSource();
                    Rect moleDst = moles.get(m).getMolePosition();

                    canvas.drawBitmap(sprites, moleSrc, moleDst, null);

                    moles.get(m).iterateFrame();

                }
            } // draw moles

            // draw struckMoles
            for (int s = 0; s < struckMoles.size(); s++) {

                if(struckMoles.get(s).isVisible()) {

                    Rect struckMoleSrc = struckMoles.get(s).getMoleSource();
                    Rect struckMoleDst = struckMoles.get(s).getMolePosition();

                    canvas.drawBitmap(sprites, struckMoleSrc, struckMoleDst, null);

                    struckMoles.get(s).iterateFrame();

                }

            } // draw struckMoles

            // draw speed bannerOne
            if(bannerOne != null && bannerOne.bannerIsVisible()) {
                canvas.drawBitmap(speedBannerBmp, bannerOne.getSrcRect(), bannerOne.getDstRect(), null);

                bannerOne.iterateFrame();

            }

            // draw speed bannerTwo
            if(bannerTwo != null && bannerTwo.bannerIsVisible()) {
                canvas.drawBitmap(speedBannerBmp, bannerTwo.getSrcRect(), bannerTwo.getDstRect(), null);

                bannerTwo.iterateFrame();

            }

        } // if sprites != null

    } // render

    public void getSlowMoles() {

        ArrayList<Long> slowMoleLaunchTimes = new ArrayList<Long>();
        ArrayList<Long> alternateSlowMoleLaunchTimes = new ArrayList<Long>();

        Random rand = new Random();

        // add NUMBER_OF_SLOW_MOLES non-repeating random numbers into an ArrayList
        // to represent positions for the NUMBER_OF_SLOW_MOLES mole launchtimes
        do {
            // create random number between 1 and MEDIUM_MOLE_START_TIME in milliseconds
             long num = nextLong(rand,  GameTimer.MEDIUM_MOLE_START_TIME * 1000) + 1;

            // if the list already contains the same number that is being added
            // then cancel and try another number
            if(slowMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                slowMoleLaunchTimes.add(num);

            }

        } while(slowMoleLaunchTimes.size() < NUMBER_OF_SLOW_MOLES);

         Mole mole;

        // load slow moles
        for(int x = 0; x < slowMoleLaunchTimes.size(); x++) {

                mole = new  Mole(Mole.NORMAL_MOLE,  Mole.SLOW, holes);
                mole.setLaunchTime(slowMoleLaunchTimes.get(x));
                moles.add(mole);

        }

        // next get alternate, good and bad moles
        // add 8 non-repeating random numbers from 1 to MEDIUM_MOLE_START_TIME into an ArrayList
        do {
            // create random number between 1 and MEDIUM_MOLE_START_TIME in milliseconds
            long num = nextLong(rand,  GameTimer.MEDIUM_MOLE_START_TIME * 1000) + 1;

            // if the either list already contains the same number that is being added
            // then cancel and try another number
            if(alternateSlowMoleLaunchTimes.contains(num) || slowMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                alternateSlowMoleLaunchTimes.add(num);

            }

        } while(alternateSlowMoleLaunchTimes.size() < 8);

         Mole altMole;

        // add alternate slow moles of good mole type
        for(int x = 0; x < 3; x++) {

            altMole = new  Mole(Mole.GOLDEN_MOLE,  Mole.SLOW, holes);
            altMole.setLaunchTime(alternateSlowMoleLaunchTimes.get(x));

            moles.add(altMole);

        }

        // add alternate slow moles of bad mole type
        for(int x = 3; x < 8 ; x++) {

                altMole = new  Mole(Mole.BAD_MOLE,  Mole.SLOW, holes);
                altMole.setLaunchTime(alternateSlowMoleLaunchTimes.get(x));
                moles.add(altMole);

        }

    } // getSlowMoles

    public void getMediumMoles() {

        ArrayList<Long> mediumMoleLaunchTimes = new ArrayList<Long>();
        ArrayList<Long> alternateMediumMoleLaunchTimes = new ArrayList<Long>();

        Random rand = new Random();

        // add NUMBER_OF_MEDIUM_MOLES non-repeating random numbers into an ArrayList
        // to represent positions for the NUMBER_OF_MEDIUM_MOLES mole launchtimes
        do {
            // create random number between FAST_MOLE_START_TIME - MEDIUM_MOLE_START_TIME in milliseconds;
            long num = nextLong(rand, ( GameTimer.FAST_MOLE_START_TIME * 1000) - ( GameTimer.MEDIUM_MOLE_START_TIME * 1000)) + ( GameTimer.MEDIUM_MOLE_START_TIME * 1000) + 1;

            // if the list already contains the same number that is being added
            // then cancel and try another number
            if(mediumMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                mediumMoleLaunchTimes.add(num);

            }

        } while(mediumMoleLaunchTimes.size() < NUMBER_OF_MEDIUM_MOLES);

         Mole mole;

        // load medium moles
        for(int x = 0; x < mediumMoleLaunchTimes.size(); x++) {

                mole = new  Mole(Mole.NORMAL_MOLE,  Mole.MEDIUM, holes);
                mole.setLaunchTime(mediumMoleLaunchTimes.get(x));
                moles.add(mole);

        }

        // next get alternate, good and bad moles
        // add 10 non-repeating random numbers from 1 to 20 into an ArrayList
        do {
            // create random number between FAST_MOLE_START_TIME - MEDIUM_MOLE_START_TIME in milliseconds;
            long num = nextLong(rand, ( GameTimer.FAST_MOLE_START_TIME * 1000) - ( GameTimer.MEDIUM_MOLE_START_TIME * 1000)) + ( GameTimer.MEDIUM_MOLE_START_TIME * 1000) + 1;

            // if the list already contains the same number that is being added
            // then cancel and try another number
            if(alternateMediumMoleLaunchTimes.contains(num) || mediumMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                alternateMediumMoleLaunchTimes.add(num);

            }

        } while(alternateMediumMoleLaunchTimes.size() < 10);

         Mole altMole;

        // add alternate medium moles of good mole type
        for(int x = 0; x < 4; x++) {

            altMole = new  Mole(Mole.GOLDEN_MOLE,  Mole.MEDIUM, holes);
            altMole.setLaunchTime(alternateMediumMoleLaunchTimes.get(x));

            moles.add(altMole);

        }

        // add alternate medium moles of bad mole type
        for(int x = 4; x < 10 ; x++) {

                altMole = new  Mole(Mole.BAD_MOLE,  Mole.MEDIUM, holes);
                altMole.setLaunchTime(alternateMediumMoleLaunchTimes.get(x));
                moles.add(altMole);

        }

    } // getMediumMoles

    public void getFastMoles() {

        ArrayList<Long> fastMoleLaunchTimes = new ArrayList<Long>();
        ArrayList<Long> alternateFastMoleLaunchTimes = new ArrayList<Long>();

        Random rand = new Random();

        // add NUMBER_OF_FAST_MOLES non-repeating random numbers into an ArrayList
        // to represent positions for the NUMBER_OF_FAST_MOLES mole launchtimes
        do {
            // create random number between FAST_MOLE_START_TIME and GAME_DURATION
            long num = nextLong(rand, (( GameTimer.GAME_DURATION -  GameTimer.END_GAME_MARGIN) * 1000) - ( GameTimer.FAST_MOLE_START_TIME * 1000)) + ( GameTimer.FAST_MOLE_START_TIME * 1000) + 1;

            // if the list already contains the same number that is being added
            // then cancel and try another number
            if(fastMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                fastMoleLaunchTimes.add(num);

            }

        } while(fastMoleLaunchTimes.size() < NUMBER_OF_FAST_MOLES);

         Mole mole;

        // load fast moles
        for(int x = 0; x < fastMoleLaunchTimes.size(); x++) {

                mole = new  Mole(Mole.NORMAL_MOLE,  Mole.FAST, holes);
                mole.setLaunchTime(fastMoleLaunchTimes.get(x));
                moles.add(mole);

        }

        // next get alternate, good and bad moles
        // add 15 non-repeating random numbers from 1 to 30 into an ArrayList
        do {
            // create random number between FAST_MOLE_START_TIME and GAME_DURATION
            long num = nextLong(rand, (( GameTimer.GAME_DURATION -  GameTimer.END_GAME_MARGIN) * 1000) - ( GameTimer.FAST_MOLE_START_TIME * 1000)) + ( GameTimer.FAST_MOLE_START_TIME * 1000) + 1;

            // if the list already contains the same number that is being added
            // then cancel and try another number
            if(alternateFastMoleLaunchTimes.contains(num)) {

                continue;

            } else {

                alternateFastMoleLaunchTimes.add(num);

            }

        } while(alternateFastMoleLaunchTimes.size() < 15);

         Mole altMole;

        // add alternate fast moles of good mole type
        for(int x = 0; x < 6; x++) {

            altMole = new  Mole(Mole.GOLDEN_MOLE,  Mole.FAST, holes);
            altMole.setLaunchTime(alternateFastMoleLaunchTimes.get(x));

            moles.add(altMole);

        }

        // add alternate fast moles of bad mole type
        for(int x = 6; x < 15 ; x++) {

                altMole = new  Mole(Mole.BAD_MOLE,  Mole.FAST, holes);
                altMole.setLaunchTime(alternateFastMoleLaunchTimes.get(x));
                moles.add(altMole);

        }

    } // getFastMoles

    public class ResetSoundLaunchable implements Runnable {

        @Override
        public void run() {

            trumpOrClintonSpeechLaunchable = true;

        }
    }

    @Override
    public void onGameTick(int gameTickTime, long quarterSecondsCountUp) {

        // game timer counting up in 250 milliseconds intervals from zero to end of game
        gameTimerCountUp = quarterSecondsCountUp;

        // display current game time in seconds counting down
        displaySeconds = gameTickTime;

        if(gameTickTime == 0 && (quarterSecondsCountUp %  GameTimer.GAME_DURATION) == 0) {

            mGameOver.onGameOver(score);

        }

    } // onGameTick

    public void setGameOverListener(GameOverListener listener) {

        mGameOver = listener;

    } // setOnGameTickListener

    public interface GameOverListener {

        public void onGameOver(int score);

    } // OnGameTickListener

    // generate a random number in long format between a specific range
    public long nextLong(Random random, long n) {

        long bits, val;

        do {

            bits = (random.nextLong() << 1) >>> 1;
            val = bits % n;

        } while (bits - val + (n - 1) < 0L);

        return val;
    }

} // GamePanel