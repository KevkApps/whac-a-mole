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

import java.util.concurrent.atomic.AtomicBoolean;

public class StruckMole {

    protected int srcX;
    protected int srcY;
    protected int moleType = Mole.NORMAL_MOLE;
    private volatile AtomicBoolean visible;
    private Rect srcRect = new Rect();
    private Rect dstRect;
    private Mole mole;

    public StruckMole(Mole mole) {

        visible = new AtomicBoolean(true);
        srcX =  GamePanel.SPRITE_FRAME_WIDTH;
        srcY = 0;
        this.mole = mole;
        this.dstRect = mole.getMolePosition();
        this.moleType = mole.getMoleType();
        mole.setMoleHoleOccupied();

        init();
    }

    public void init() {

        if(moleType == Mole.NORMAL_MOLE) {

            srcY = GamePanel.SPRITE_FRAME_HEIGHT * 7;

        } else if(moleType == Mole.GOLDEN_MOLE) {

            srcY =  GamePanel.SPRITE_FRAME_HEIGHT * 8;

        } else if(moleType == Mole.BAD_MOLE) {

            srcY =  GamePanel.SPRITE_FRAME_HEIGHT * 9;
        }

        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + GamePanel.SPRITE_FRAME_WIDTH;
        srcRect.bottom = srcY + GamePanel.SPRITE_FRAME_HEIGHT;

    } // init

    public void iterateFrame() {

        if(srcX < 12 * GamePanel.SPRITE_FRAME_WIDTH) {

            srcX += GamePanel.SPRITE_FRAME_WIDTH;

            srcRect.left = srcX;
            srcRect.top = srcY;
            srcRect.right = srcX + GamePanel.SPRITE_FRAME_WIDTH;
            srcRect.bottom = srcY + GamePanel.SPRITE_FRAME_HEIGHT;

        } else if(srcX >= 12 * GamePanel.SPRITE_FRAME_WIDTH) {

            mole.setMoleHoleUnOccupied();
            visible.set(false);
            srcX = 0;

        }

    } // iterateFrame

    // get mole visibility status
    public boolean isVisible() {

        return visible.get();

    } // isVisible

    public Rect getMolePosition() {

        return dstRect;

    }

    public Rect getMoleSource() {

        return srcRect;
    }

} // StruckMole