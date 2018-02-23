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

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    // surface holder that can access the physical surface
    private SurfaceHolder surfaceHolder;

    // the view that handles inputs and draws to the surface
    private GamePanel gamePanel;

    // game status
    public volatile boolean running;

    // desired frames per second
    private final static int MAX_FPS = 28;

    // maximum number of frames to be skipped
    private final static int MAX_FRAME_SKIPS = 5;

    // the frame period
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;

    public GameThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {

        Canvas canvas = null;
        long beginTime;		// cycle beginning time
        long timeDiff;		// cycle to execution time
        int sleepTime;		// milliseconds of sleep, less than zero if behind
        int framesSkipped;	// frames skipped

        while (running) {

            // try locking the canvas to for drawing on the surface
            try {

                canvas = surfaceHolder.lockCanvas();
                
                synchronized (surfaceHolder) {

                    beginTime = System.currentTimeMillis();
                    framesSkipped = 0; // resetting the frames skipped

                    // update game state
                    gamePanel.update();

                    // render state to the screen
                    gamePanel.render(canvas);				

                    // calculate length of cycle
                    timeDiff = System.currentTimeMillis() - beginTime;

                    // sleep time calculation
                    sleepTime = (int)(FRAME_PERIOD - timeDiff);

                    if (sleepTime > 0) {

                        // if sleepTime > 0 then ok
                        try {

                            // short time thread will sleep for battery saving
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {

                        }
                    }

                    while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {

                        // if need to catch up, update without rendering
                        gamePanel.update(); 

                        // add frame period to checking if in the next frame
                        sleepTime += FRAME_PERIOD;	
                        framesSkipped++;
                    }

                }
 
            } finally {

                // canvas is not left in incostant state if there is an excepion
                if (canvas != null) {

                    surfaceHolder.unlockCanvasAndPost(canvas);

                }
            } // finally
        } // while running
    } // run
    
} // GameThread