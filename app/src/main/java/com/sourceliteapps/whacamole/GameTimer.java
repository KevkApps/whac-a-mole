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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameTimer {

        // game duration and times in milliseconds
        public static final int START_DELAY_GAME = 1; // delay for start of game in seconds
        public static final int GAME_DURATION = 120; // length of game in seconds
        public static final int MEDIUM_MOLE_START_TIME = 40; // medium mole start time in seconds
        public static final int FAST_MOLE_START_TIME = 80; // fast mole start time in seconds
        public static final int END_GAME_MARGIN = 3; // seconds of time margin at end of game after last mole
        public static final int BANNER_ONE_LAUNCH_TIME = 37; // launch time for scrolling banner one in seconds
        public static final int BANNER_TWO_LAUNCH_TIME = 77; // launch time for scrolling banner two in seconds

        private ScheduledThreadPoolExecutor stpe;

        private Future<?> timerFuture;

        private TimerRunner mTimerRunner;

        private TimerStopper mTimerStopper;

        private OnGameTickListener mOnGameTickListener;

        public GameTimer(ScheduledThreadPoolExecutor stpe, Future<?> future) {

            this.stpe = stpe;

            this.timerFuture = future;

            mTimerRunner = new TimerRunner();

            mTimerStopper = new TimerStopper();

        } // GameTimer constructor

        public void startTimer() {

             timerFuture = stpe.scheduleAtFixedRate(mTimerRunner, START_DELAY_GAME , 250, TimeUnit.MILLISECONDS);
             stpe.schedule(mTimerStopper, GAME_DURATION + START_DELAY_GAME, TimeUnit.SECONDS);

        } // startTimer

        public class TimerRunner implements Runnable {

            private long tempCountUp = 0;

            private int moleSpeed = Mole.SLOW;

            private int timerSecondsCountDown = GAME_DURATION;

            private long timerMillisecondsCountUp = 0;

            @Override
            public void run() {

                if (timerMillisecondsCountUp > MEDIUM_MOLE_START_TIME * 1000) {

                    moleSpeed = Mole.MEDIUM;
                }

                if (timerMillisecondsCountUp > FAST_MOLE_START_TIME * 1000) {

                    moleSpeed = Mole.FAST;
                }

                if(timerMillisecondsCountUp > tempCountUp  + 997) {

                    timerSecondsCountDown -= 1;

                    tempCountUp += 1000;

                    mOnGameTickListener.onGameTick(timerSecondsCountDown, timerMillisecondsCountUp);

                } else {

                    mOnGameTickListener.onGameTick(timerSecondsCountDown, timerMillisecondsCountUp);

                }

                timerMillisecondsCountUp += 250;

            } // run

        } // TimerRunner

        public class TimerStopper implements Runnable {

            @Override
            public void run() {

              timerFuture.cancel(true);

            } // run

        } // TimerStopper

        public void setOnGameTickListener(OnGameTickListener listener) {

             mOnGameTickListener = listener;

        } // setOnGameTickListener

        public interface OnGameTickListener {

            public void onGameTick(int gameTickTime, long quarterSecondsCountUp);

        } // OnGameTickListener

} // GameTimer