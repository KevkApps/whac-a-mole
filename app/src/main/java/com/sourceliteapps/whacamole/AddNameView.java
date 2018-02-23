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
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;

public class AddNameView extends LinearLayout {

    private EditText editTextOne;
    private Button buttonOne;
    private Button buttonTwo;
    private int position;
    private ArrayList<ResultsActivity.ResultsInfo> resultsArray;
    private OnNameAddedListener mOnNameAddedListener;

    public AddNameView(Context context) {
        super(context);
        init(context);
    }

    public AddNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddNameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        LayoutInflater  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.add_name_view, this);

        editTextOne = (EditText) view.findViewById(R.id.editText1);
        buttonOne = (Button) view.findViewById(R.id.button1);
        buttonTwo = (Button) view.findViewById(R.id.button2);

        editTextOne.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    updateName(editTextOne.getText().toString().trim());
                    mOnNameAddedListener.onNameAdded(true);

                    return true;
                }
                return false;
            }
        });

        buttonOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                updateName(editTextOne.getText().toString().trim());
                mOnNameAddedListener.onNameAdded(true);

            }
        });

        buttonTwo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mOnNameAddedListener.onNameAdded(false);

            }
        });

    } // init

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    } // onLayout

    public void updateName(String nameToAdd) {

        resultsArray.get(position).name = nameToAdd.trim();

    } // updateName

    public void setListAndPosition(ArrayList<ResultsActivity.ResultsInfo> list, int position) {

        this.resultsArray = list;
        this.position = position;

        editTextOne.setText((resultsArray.get(position).name).trim());

    } // addNameList

    public void setOnNameAddedListener(OnNameAddedListener listener) {

        mOnNameAddedListener = listener;

    } // setOnNameAddedListener

    public interface OnNameAddedListener {

        public void onNameAdded(boolean added);

    }

} // AddNameView