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

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mole {

    public static final int NORMAL_MOLE = 4, BAD_MOLE = 5, GOLDEN_MOLE = 6;
    public static final int SLOW = 0, MEDIUM = 1, FAST = 2;
    private int moleType = NORMAL_MOLE;
    private int moleSpeed;
    private long launchTime = 0;
    private int struck = 0;
    protected volatile AtomicBoolean visible;
    private int srcX;
    private int srcY;
    public static int TOP_DEAD_CENTER = 7; // head is fully out of hole at highest point
    public static int MIDDLE_CYCLE = 8; // only part of head sticking out of hole
    protected volatile int headPositionStatus = TOP_DEAD_CENTER; // how much of head is up out of hole
    private Rect srcRect = new Rect();
    private Rect dstRect = new Rect();
    private int holeIndex = 0;
    private ArrayList<Hole> holes = new ArrayList<Hole>();
   // public int currentframe = 0;

    public Mole(int moleType, int moleSpeed, ArrayList<Hole> holes) {

        visible = new AtomicBoolean(false);
        this.moleSpeed = moleSpeed;
        this.holes = holes;
        srcX = 0; // initial x position
        setMoleSrc(moleType);
    }

    // set mole graphics source from sprite sheet for mole type
    public void setMoleSrc(int moleTypeSetting) {

        this.moleType = moleTypeSetting;

        switch(moleTypeSetting) {

            case NORMAL_MOLE: {

                if(moleSpeed == SLOW) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 1;

                } else if(moleSpeed == MEDIUM) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 2;

                } else if(moleSpeed == FAST) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 2;

                }

            }
            break;
            case GOLDEN_MOLE: {

                if(moleSpeed == SLOW) {

                    srcY =  GamePanel.SPRITE_FRAME_HEIGHT * 3;

                } else if(moleSpeed == MEDIUM) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 4;

                } else if(moleSpeed == FAST) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 4;

                }

            }
            break;
            case BAD_MOLE:  {

                if(moleSpeed == SLOW) {

                    srcY =  GamePanel.SPRITE_FRAME_HEIGHT * 5;

                } else if(moleSpeed == MEDIUM) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 6;

                } else if(moleSpeed == FAST) {

                    srcY = GamePanel.SPRITE_FRAME_HEIGHT * 6;

                }
            }
            break;

        } // switch mole

        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + GamePanel.SPRITE_FRAME_WIDTH;
        srcRect.bottom = srcY + GamePanel.SPRITE_FRAME_HEIGHT;

        setMolePosition();

    } // init

    // iterate anamation of mole from sprite sheet by position
    public void iterateFrame() {

        if(struck > 0) {

            visible.set(false);
            holes.get(holeIndex).isOccupied = false;
            srcX = 0;

            return;

        }

        switch(moleSpeed) {

            case SLOW: {

                if(srcX < (28 * GamePanel.SPRITE_FRAME_WIDTH)) {

                    setHeadPositionStatus(srcX, 28);

                    srcX += GamePanel.SPRITE_FRAME_WIDTH;

                } else if(srcX >= 28 * GamePanel.SPRITE_FRAME_WIDTH) {

                    visible.set(false);
                    holes.get(holeIndex).isOccupied = false;
                    srcX = 0;

                    return;

                }
            } // GamePanel.slow

            break;

            case MEDIUM: {

                if(srcX < (20 * GamePanel.SPRITE_FRAME_WIDTH)) {

                    setHeadPositionStatus(srcX, 20);

                    srcX += GamePanel.SPRITE_FRAME_WIDTH;

                } else if(srcX >= 20 * GamePanel.SPRITE_FRAME_WIDTH){

                    visible.set(false);
                    holes.get(holeIndex).isOccupied = false;
                    srcX = 0;

                    return;

                }
            }

            break;

            case FAST: {

                if(srcX < (20 * GamePanel.SPRITE_FRAME_WIDTH)) {

                    setHeadPositionStatus(srcX, 20);

                    srcX += GamePanel.SPRITE_FRAME_WIDTH;

                } else if(srcX >= 20 * GamePanel.SPRITE_FRAME_WIDTH) {

                    visible.set(false);
                    holes.get(holeIndex).isOccupied = false;
                    srcX = 0;

                    return;

                }
            }

            break;

        } // switch

        srcRect.left = srcX;
        srcRect.right = srcX + GamePanel.SPRITE_FRAME_WIDTH;

    } // iterateFrame

    private void setHeadPositionStatus(int currentSrcXFrame, int maximumSrcXFrame) {

        if(currentSrcXFrame > (int) ((maximumSrcXFrame * GamePanel.SPRITE_FRAME_WIDTH) / 2)) {

            headPositionStatus = MIDDLE_CYCLE;

        } else {

            headPositionStatus = TOP_DEAD_CENTER;

        }

    } // setHeadPositionStatus

    public void setMolePosition() {

        Random randomHole = new Random();
        int randomHolePosition = randomHole.nextInt(10);

        switch(randomHolePosition) {
            case 0:
                holeIndex = 0;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 1:
                holeIndex = 1;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 2:
                holeIndex = 2;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 3:
                holeIndex = 3;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 4:
                holeIndex = 4;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 5:
                holeIndex = 5;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 6:
                holeIndex = 6;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 7:
                holeIndex = 7;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 8:
                holeIndex = 8;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;
            case 9:
                holeIndex = 9;
                dstRect = new Rect(holes.get(holeIndex).holePositionX, holes.get(holeIndex).holePositionY,
                        holes.get(holeIndex).holePositionX + holes.get(holeIndex).holeWidth, holes.get(holeIndex).holePositionY + holes.get(holeIndex).holeHeight);
                break;

        } // switch

    } // setMolePosition

    public Rect getMolePosition() {

        return dstRect;

    } // get MolePosition

    public Rect getMoleSource() {

        return srcRect;
    }

    public Hole getCurrentHole() {

        return holes.get(holeIndex);
    }

    // get mole visibility status
    public boolean isVisible() {

        return visible.get();

    } // isVisible

    public void setMoleVisible() {

        visible.set(true);
        holes.get(holeIndex).isOccupied = true;

    } // setVisibility

    public void setMoleHoleOccupied() {

        holes.get(holeIndex).isOccupied = true;

    } // setMoleHoleOccupied

    public void setMoleHoleUnOccupied() {

        holes.get(holeIndex).isOccupied = false;

    } // setMoleHoleUnOccupied

    public int getMoleType() {

        return moleType;

    } // getMoleType

    public long getLaunchTime() {

        return launchTime;

    } // getLaunchTime

    public void setLaunchTime(long time) {

        launchTime = time;

    } // setLaunchTime

    public void strike() {

        struck++;

    } // struck

} // Mole