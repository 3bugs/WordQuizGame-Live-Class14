package com.example.wordquizgameliveclass14;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mPlayGameButton, mHighScoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ออบเจ็คสีชมพู
        mPlayGameButton = (Button) findViewById(R.id.play_game_button);
        // ออบเจ็คสีฟ้า
        //MyListener listener = new MyListener();
        // ผูกออบเจ็คสีชมพูกับสีฟ้าเข้าด้วยกัน
        mPlayGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseDifficultyDialog();
            }
        });

        mHighScoreButton = (Button) findViewById(R.id.high_score_button);
        mHighScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HighScoreActivity.class);
                startActivity(i);
            }
        });

    } // ปิดเมธอด onCreate

    private void showChooseDifficultyDialog() {
        String[] diffLabels = new String[]{"ง่าย", "ปานกลาง", "ยาก"};

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("เลือกระดับความยาก");
        dialog.setItems(diffLabels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "คุณเลือก " + which);
                Intent i = new Intent(MainActivity.this, GameActivity.class);
                i.putExtra("diff", which);
                startActivity(i);
            }
        });
        dialog.show();
    }


} // ปิดคลาส MainActivity
