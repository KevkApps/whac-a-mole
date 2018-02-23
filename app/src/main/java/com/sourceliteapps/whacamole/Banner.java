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

public class Banner {

    private int bannerWidth = 0;
    private int bannerHeight = 0;
    private int dstX = 0;
    private int screenWidth = 0;
    private int bitmapWidth = 0;
    private int bitmapHeight = 0;
    private Rect srcRect;
    private Rect dstRect;
    private volatile AtomicBoolean visible;

    public Banner(int screenWidth, int screenHeight, int bmpWidth, int bmpHeight) {

        visible = new AtomicBoolean(true);

        this.screenWidth = screenWidth;

        bannerWidth = (int) Math.round(0.6 * (double) screenWidth);
        bannerHeight = (int) Math.round(0.08 * (double) screenHeight);

        bitmapWidth = bmpWidth;
        bitmapHeight = bmpHeight;

        srcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);

        int bannerTopPosition = screenHeight - bannerHeight;

        dstX = -bannerWidth;

        dstRect = new Rect(dstX, bannerTopPosition, dstX + bannerWidth, screenHeight);

    } // Banner

    public void iterateFrame() {

        if(dstX < screenWidth) {

            dstX += 18;

            dstRect.left = dstX;
            dstRect.right = dstX + bannerWidth;

        } else if (dstX >= screenWidth) {

            visible.set(false);
            dstX = 0;

        }

    } // interateBannerPosition

    public boolean bannerIsVisible() {

        return visible.get();

    }

    public Rect getSrcRect() {

        return srcRect;
    }

    public Rect getDstRect() {

        return dstRect;
    }

} // Banner