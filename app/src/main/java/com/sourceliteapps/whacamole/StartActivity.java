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
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class StartActivity extends Activity {

    private ImageView imageButtonOne;
    private FrameLayout frameLayoutOne;
    public static int screenWidth = 0;
    public static int screenHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        imageButtonOne = (ImageView) findViewById(R.id.imageButton1);

        frameLayoutOne = (FrameLayout) findViewById(R.id.frameLayout1);

        frameLayoutOne.post(new Runnable() {

            @Override
            public void run() {

                screenWidth = frameLayoutOne.getWidth();
                screenHeight = frameLayoutOne.getHeight();

                imageButtonOne.getLayoutParams().width = (int) Math.round(0.65 * (double) screenWidth);
                imageButtonOne.getLayoutParams().height = (int) Math.round(0.36 * (double) screenHeight);

            } // run

        });

        imageButtonOne.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    view.setAlpha(0.5f);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

                    view.setAlpha(1.0f);

                Intent intent = new Intent(StartActivity.this, GameActivity.class);
                startActivity(intent);

                    return true;
                }
                return false;
            }
        });

    } // onCreate

} // StartActivity