package com.example.wordquizgameliveclass14;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.wordquizgameliveclass14.db.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();
    private static final int NUM_QUESTIONS_PER_QUIZ = 3;

    private int mDifficulty;
    private int mNumChoices;

    // เก็บรายชื่อไฟล์ภาพทั้งหมด (ตัวอย่างนี้คือ 50 ชื่อไฟล์)
    private ArrayList<String> mFileNameList = new ArrayList<>();
    // เก็บรายชื่อไฟล์ภาพคำถาม
    private ArrayList<String> mQuizWordList = new ArrayList<>();
    // เก็บคำศัพท์ที่เป็นตัวเลือก (choice) ของคำถามแต่ละข้อ
    private ArrayList<String> mChoiceWordList = new ArrayList<>();

    private String mAnswerFileName;
    private int mTotalGuesses;
    private int mScore;

    private Random mRandom = new Random();
    private Handler mHandler = new Handler();
    private Animation mShakeAnimation;

    private TextView mQuestionNumberTextView;
    private ImageView mQuestionImageView;
    private TextView mAnswerTextView;
    private TableLayout mButtonTableLayout;

    private DatabaseHelper mHelper;
    private SQLiteDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mHelper = new DatabaseHelper(this);
        mDatabase = mHelper.getWritableDatabase();

        mQuestionNumberTextView = (TextView) findViewById(R.id.question_number_text_view);
        mQuestionImageView = (ImageView) findViewById(R.id.question_image_view);
        mButtonTableLayout = (TableLayout) findViewById(R.id.button_table_layout);
        mAnswerTextView = (TextView) findViewById(R.id.answer_text_view);

        Intent i = getIntent();
        mDifficulty = i.getIntExtra("diff", 0);
        switch (mDifficulty) {
            case 0: // ง่าย
                mNumChoices = 2;
                break;
            case 1: // ปานกลาง
                mNumChoices = 4;
                break;
            case 2: // ยาก
                mNumChoices = 6;
                break;
        } // ปิด switch

        mShakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mShakeAnimation.setRepeatCount(3);

        getImageFileNames();

    } // ปิดเมธอด onCreate

    private void getImageFileNames() {
        String[] categories = new String[]{
                "animals", "body", "colors", "numbers", "objects"
        };

        AssetManager am = getAssets();
        for (String c : categories) {
            try {
                String[] fileNames = am.list(c);

                for (String f : fileNames) {
                    mFileNameList.add(f.replace(".png", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error listing file name in " + c);
            } // ปิด try-catch
        } // ปิด for

        Log.i(TAG, "***** รายชื่อไฟล์ภาพทั้งหมด");
        for (String f : mFileNameList) {
            Log.i(TAG, f);
        }

        startQuiz();
    } // ปิดเมธอด getImageFileNames

    private void startQuiz() {
        mScore = 0;
        mTotalGuesses = 0;
        mQuizWordList.clear();

        while (mQuizWordList.size() < NUM_QUESTIONS_PER_QUIZ) {
            int randomIndex = mRandom.nextInt(mFileNameList.size());
            String fileName = mFileNameList.get(randomIndex);

            if (!mQuizWordList.contains(fileName)) {
                mQuizWordList.add(fileName);
            }
        } // ปิด while

        Log.i(TAG, "***** รายชื่อไฟล์คำถามที่สุ่มได้");
        for (String f : mQuizWordList) {
            Log.i(TAG, f);
        }

        loadNextQuestion();
    } // ปิดเมธอด startQuiz

    private void loadNextQuestion() {
        mAnswerTextView.setText(null);
        mAnswerFileName = mQuizWordList.remove(0);

        String msg = String.format(
                "คำถาม %d จาก %d",
                mScore + 1,
                NUM_QUESTIONS_PER_QUIZ
        );
        mQuestionNumberTextView.setText(msg);

        Log.i(TAG, "***** ชื่อไฟล์ภาพคำถามข้อปัจจุบันคือ " + mAnswerFileName);

        loadQuestionImage();
        prepareChoiceWords();
    } // ปิดเมธอด loadNextQuestion

    private void loadQuestionImage() {
        String category = mAnswerFileName.substring(
                0,
                mAnswerFileName.indexOf('-')
        );

        String filePath = category + "/" + mAnswerFileName + ".png";

        AssetManager am = getAssets();
        InputStream stream = null;

        try {
            stream = am.open(filePath);
            Drawable image = Drawable.createFromStream(stream, null);
            mQuestionImageView.setImageDrawable(image);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading image file: " + filePath);
        }
    } // ปิดเมธอด loadQuestionImage

    private void prepareChoiceWords() {
        mChoiceWordList.clear();
        String answerWord = getWord(mAnswerFileName);

        while (mChoiceWordList.size() < mNumChoices) {
            int randomIndex = mRandom.nextInt(mFileNameList.size());
            String randomWord = getWord(mFileNameList.get(randomIndex));

            if (!mChoiceWordList.contains(randomWord) &&
                    !answerWord.equals(randomWord)) {
                mChoiceWordList.add(randomWord);
            }
        }

        int randomIndex = mRandom.nextInt(mChoiceWordList.size());
        mChoiceWordList.set(randomIndex, answerWord);

        Log.i(TAG, "***** คำศัพท์ตัวเลือกที่สุ่มได้");
        for (String w : mChoiceWordList) {
            Log.i(TAG, w);
        }

        createChoiceButtons();
    } // ปิดเมธอด prepareChoiceWords

    private String getWord(String fileName) {
        return fileName.substring(fileName.indexOf('-') + 1);
    }

    private void createChoiceButtons() {
        for (int row = 0; row < mButtonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);
            tr.removeAllViews();
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int row = 0; row < mNumChoices / 2; row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);

            for (int column = 0; column < 2; column++) {
                Button guessButton = (Button) inflater.inflate(R.layout.guess_button, tr, false);
                guessButton.setText(mChoiceWordList.remove(0));
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitGuess((Button) v);
                    }
                });
                tr.addView(guessButton);
            }
        }
    } // ปิดเมธอด createChoiceButtons

    private void submitGuess(Button guessButton) {
        Log.i(TAG, "You selected " + guessButton.getText().toString());

        mTotalGuesses++;

        String guessWord = guessButton.getText().toString();
        String answerWord = getWord(mAnswerFileName);

        // ตอบถูก
        if (guessWord.equals(answerWord)) {
            mScore++;

            MediaPlayer mp = MediaPlayer.create(this, R.raw.applause);
            mp.start();

            String msg = guessWord + " " + getString(R.string.correct_label);
            mAnswerTextView.setText(msg);
            mAnswerTextView.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark));

            disableAllButtons();

            // ตอบถูก และเล่นครบทุกข้อแล้ว (จบเกม)
            if (mScore == NUM_QUESTIONS_PER_QUIZ) {
                double percentScore = 100 * NUM_QUESTIONS_PER_QUIZ / (double) mTotalGuesses;
                saveScore(percentScore);

                String dialogMsg = getString(R.string.total_guesses_label, mTotalGuesses);
                dialogMsg += "\n" + getString(R.string.success_percentage_label, percentScore);

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.result_title))
                        .setMessage(dialogMsg)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.restart_quiz_label),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startQuiz();
                                    }
                                })
                        .setNegativeButton(getString(R.string.return_to_menu_label),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .show();
            }
            // ตอบถูก แต่ยังไม่ครบทุกข้อ (ยังไม่จบเกม)
            else {
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                loadNextQuestion();
                            }
                        }
                        , 2000
                );
            }
        }
        // ตอบผิด
        else {
            guessButton.setEnabled(false);

            MediaPlayer mp = MediaPlayer.create(this, R.raw.fail);
            mp.start();

            mQuestionImageView.startAnimation(mShakeAnimation);

            String msg = getString(R.string.incorrect_label);
            mAnswerTextView.setText(msg);
            mAnswerTextView.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    } // ปิดเมธอด submitGuess

    private void disableAllButtons() {
        for (int row = 0; row < mButtonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);

            for (int column = 0; column < tr.getChildCount(); column++) {
                Button b = (Button) tr.getChildAt(column);
                b.setEnabled(false);
            }
        }
    } // ปิดเมธอด disableAllButtons

    private void saveScore(double percentScore) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_SCORE, String.format("%.1f", percentScore));
        cv.put(DatabaseHelper.COL_DIFFICULTY, mDifficulty);
        mDatabase.insert(DatabaseHelper.TABLE_NAME, null, cv);
    } // ปิดเมธอด saveScore

} // ปิดคลาส GameActivity
