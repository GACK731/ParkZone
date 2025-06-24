package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;


import androidx.appcompat.app.AppCompatActivity;

public class CountdownActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView minutesText, secondsText;
    private MaterialButton startButton, resetButton;

    private Handler handler = new Handler();
    private int seconds = 0;
    private boolean isRunning = false;

    private Runnable stopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            int mins = seconds / 60;
            int secs = seconds % 60;

            minutesText.setText(String.format("%02d", mins));
            secondsText.setText(String.format("%02d", secs));

            if(isRunning){
                seconds++;
                handler.postDelayed(this,1000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countdown);

        btnBack = findViewById(R.id.btnBack);
        minutesText = findViewById(R.id.minutesText);
        secondsText = findViewById(R.id.secondsText);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        //back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(CountdownActivity.this, NavigationMenuActivity.class));
            finish();
        });

        //start button
        startButton.setOnClickListener(v ->{
            if(!isRunning){
                isRunning = true;
                handler.post(stopwatchRunnable);
                startButton.setText("Stop");
            }else{
                isRunning = false;
                handler.removeCallbacks(stopwatchRunnable);
                startButton.setText("Start");
            }
        });

        // Reset stopwatch
        resetButton.setOnClickListener(v -> {
            isRunning = false;
            handler.removeCallbacks(stopwatchRunnable);
            seconds = 0;
            minutesText.setText("00");
            secondsText.setText("00");
            startButton.setText("Start");
        });

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(stopwatchRunnable); // avoid memory leaks
    }


}