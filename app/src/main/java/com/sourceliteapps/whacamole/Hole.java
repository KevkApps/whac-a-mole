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

public class Hole {

    protected int holePositionX = 0;
    protected int holePositionY = 0;
    protected int holeWidth = 0;
    protected int holeHeight = 0;
    public volatile boolean isOccupied;

    public Hole(int centerX, int centerY, int holeWidth, int holeHeight) {

        isOccupied = false;
        this.holeWidth = holeWidth;
        this.holeHeight = holeHeight;
        holePositionX = centerX - (holeWidth/2);
        holePositionY = centerY - (holeHeight/2);

    } // Hole constructor
    
} // Hole