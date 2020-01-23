/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private static final String OURTAG = "GACS_WS";
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack<LetterTile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();

                /** **  YOUR CODE GOES HERE **/
                if( word.length() == WORD_LENGTH ) {
                    words.add(word);
                }
            }
            Log.d(OURTAG, String.format("Loaded %d words from words.txt", words.size()));
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
//        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
//        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    ViewGroup w1 = findViewById(R.id.word1);
                    String usrWord1 = getUserWord(w1);
                    ViewGroup w2 = findViewById(R.id.word2);
                    String usrWord2 = getUserWord(w2);
                    validateAndShowWords(usrWord1, usrWord2);
                }
                /** ** **  YOUR CODE GOES HERE ** **/
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        // grab the words the user put into the boxes
                        ViewGroup w1 = findViewById(R.id.word1);
                        String usrWord1 = getUserWord(w1);
                        ViewGroup w2 = findViewById(R.id.word2);
                        String usrWord2 = getUserWord(w2);

                        Log.d(OURTAG, String.format("user entered %s and %s", usrWord1, usrWord2));

                        // if they're valid words we show them in the messageBox
                        validateAndShowWords(usrWord1, usrWord2);
                    }
                    /** ** **  YOUR CODE GOES HERE **/
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    private String getUserWord(ViewGroup v) {
        String result = new String();
        char usrWord[] = new char[WORD_LENGTH];

        // get letters from the word1 ViewGroup onto result
        for( int i = 0; i < v.getChildCount(); i++ ) {
            TextView tile = (TextView) v.getChildAt(i);
            usrWord[i] = tile.getText().charAt(0);
            result = new String(usrWord);
        }

        return result;
    }

    private boolean validateAndShowWords(String userWord1, String userWord2) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);

        // check to see if user's words are valid (even if not the ones we used to scramble)
        if( words.contains(userWord1) && words.contains(userWord2)) {
            messageBox.setText("Winner, winner, chicken diner!! " + userWord1 + " and " + userWord2 + " are correct!");
            Toast toast = Toast.makeText(this, "We have a winner!!", Toast.LENGTH_LONG);
            toast.show();
        } else {
            messageBox.setText("nope, try again");
        }

        return true;
    }

    private String mergeWithMap(String str1, String str2) {
        // create scrambled string builder (we can easily append to this data structure
        StringBuilder scrambled = new StringBuilder();

        // create hashMap with our two words as keys and a counter as we go through the letters for each
        HashMap<String, Integer> myWords = new HashMap<String, Integer>();
        myWords.put(str1, 0);
        myWords.put(str2, 0);

        // loop through all letters of both words (WORD_LENGTH * 2)
        for( int i = 0; i < WORD_LENGTH*2 ; i++) {

            // randomly pick one of our 2 words
            String pickedWord = random.nextBoolean() ? word1 : word2;

            // if we've used all letters in the picked word already, switch to the other word
            if(myWords.get(pickedWord) >= WORD_LENGTH) {
                pickedWord = (pickedWord == word1) ? word2 : word1;
            }

            // get the count of letters left in the picked word
            int pickedNextLetter = myWords.get(pickedWord);

//            Log.d(OURTAG, String.format("picked word is %s, next letter in it is %d", pickedWord, pickedNextLetter));

            // add the next letter of picked word to scrambled string and increase picked word's letter counter
            scrambled.append(pickedWord.charAt(pickedNextLetter));
            myWords.put(pickedWord, ++pickedNextLetter);
        }

        Log.d(OURTAG, String.format("scrambled string is %s", scrambled.toString()));

        return scrambled.toString();
    }

    public boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);

        /** ** **  YOUR CODE GOES HERE ** **/
        // reset the game if it's been played already
        messageBox.setText("Resetting game...");
        Log.d(OURTAG, "resetting game...");

        // reset the word1 ViewGroup
        ViewGroup word1LinearLayout = findViewById(R.id.word1);
        if(word1LinearLayout.getChildCount() > 0 ) {
            word1LinearLayout.removeAllViews();
        }
        // reset the word2 ViewGroup
        ViewGroup word2LinearLayout = findViewById(R.id.word2);
        if(word2LinearLayout.getChildCount() > 0 ) {
            word2LinearLayout.removeAllViews();
        }

        stackedLayout.clear();

        // grab a couple of random words
        word1 = words.get(random.nextInt(words.size()));
        word2 = words.get(random.nextInt(words.size()));
        Log.d(OURTAG, String.format("random words are %s and %s", word1, word2));

        String combinedWords = mergeWithMap(word1, word2);

        // for every letter in combinedWords, in reverse order...
        for( int i = combinedWords.length()-1; i >= 0; i--){
            // create a tile for the next letter...
            LetterTile tile = new LetterTile(this, combinedWords.charAt(i));

            // ...and push it onto stackedLayout
            stackedLayout.push(tile);
        }

        messageBox.setText("Game started");
        return true;
    }

    public boolean onUndo(View view) {
        /** ** **  YOUR CODE GOES HERE ** **/
        if(!placedTiles.empty()){
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup((ViewGroup) stackedLayout);
        }
        return true;
    }
}
