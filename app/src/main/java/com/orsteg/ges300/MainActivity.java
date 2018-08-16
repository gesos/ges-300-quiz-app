package com.orsteg.ges300;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public TextView score;
    public Button start, history;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        score = (TextView) findViewById(R.id.score);
        start = (Button) findViewById(R.id.start);
        history = (Button) findViewById(R.id.history);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTest();
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHistory();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("scores", Activity.MODE_PRIVATE);
        score.setText(prefs.getString("last_score", "0/0"));

    }

    private void startTest(){
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    private void viewHistory(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
