package com.orsteg.ges300;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orsteg.harold.dialogs.LoaderDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class TestActivity extends AppCompatActivity implements Result.EndTestListener {

    private SharedPreferences prefs;
    public int questionCount, correctCount;

    public Button end, next;
    public ListView options;
    public TextView index, question;

    public boolean resultState;

    public Options optionAdapter;
    public AdapterView.OnItemClickListener listener;
    public ArrayList<Question> questions;

    public Random rnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        setTitle("GES 300 Test");

        end = (Button) findViewById(R.id.finish);
        next = (Button) findViewById(R.id.next);
        options = (ListView) findViewById(R.id.options);
        index = (TextView) findViewById(R.id.index);
        question = (TextView) findViewById(R.id.question);

        prefs = getSharedPreferences("scores", Activity.MODE_PRIVATE);
        questionCount = 0;
        correctCount = 0;
        resultState = false;

        rnd = new Random();

        questions = new ArrayList<>();
        optionAdapter = new Options(this, new ArrayList<String>(), -1);

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTest();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(optionAdapter.showAnswer){
                    nextQuestion();
                } else {
                    checkAnswer();

                }
            }
        });


        listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                optionAdapter.selection = i;
                optionAdapter.notifyDataSetChanged();
            }
        };

        options.setAdapter(optionAdapter);

        getQuestions();
    }

    public void getQuestions(){
        final LoaderDialog dialog = new LoaderDialog(this, true);

        dialog.show();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream questionSheet = getResources().openRawResource(R.raw.sheet);

                int r = 0;
                int line = 0;
                StringBuilder ex = new StringBuilder();
                Question q = new Question();
                String ans = "";
                int p = 0;

                try {
                    while ((r = questionSheet.read()) != -1) {

                        if ((char) r == '\n' && (char) p != '\n') {

                            int l = line % 5;
                            if (l == 0){
                                q.question = ex.toString().replaceFirst("(\\d+)(\\.)(\\s+)", "");
                            } else if (l >= 1 && l <= 4) {

                                String s = ex.toString();
                                boolean a = s.indexOf('*') == 0;

                                String opt = s.replaceFirst("([*abcd]+)(\\.)(\\s+)", "");
                                int ind = rnd.nextInt(q.options.size()+1);

                                q.options.add(ind, opt);

                                if (a) ans = opt;


                                if (l == 4) {
                                    q.answer = q.options.indexOf(ans);

                                    int ind2 = rnd.nextInt(questions.size()+1);

                                    questions.add(ind2, q);

                                    q = new Question();
                                    ans = "";
                                }
                            }

                            line++;
                            ex = new StringBuilder();
                            p = r;
                            continue;
                        }

                        if ((char) r != '\n') ex.append((char) r);
                        p = r;

                    }
                } catch (IOException e) {
                    Log.d("Error", e.getMessage());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        nextQuestion();
                    }
                });

            }
        })).start();

    }

    public void checkAnswer(){
        next.setText("Next");
        options.setOnItemClickListener(null);
        optionAdapter.showAnswer();


        questionCount++;

        if (optionAdapter.answer == optionAdapter.selection) {
            correctCount++;
        }
    }

    public void nextQuestion(){

        if (questions.size() > 0) {

            next.setText("Continue");
            options.setOnItemClickListener(listener);

            index.setText("(" + (questionCount + 1) + ")  of  (378)");

            int i = rnd.nextInt(questions.size());

            Question q = questions.get(i);

            question.setText(q.question);
            optionAdapter.setOptions(q.options, q.answer);

            questions.remove(i);
        } else if (questionCount > 0){
            endTest();
        }
    }

    public void endTest(){

        int i = prefs.getInt("history_c", 0);

        prefs.edit().putString("last_score", "" + correctCount + "/" + questionCount)
                .putString("score" + (i + 1), "" + correctCount + "/" + questionCount)
                .putInt("history_c", i + 1).commit();

        Result res = new Result(this, "" + correctCount + "/" + questionCount);
        res.show();
    }

    @Override
    public void end() {
        finish();
    }

    public class Options extends BaseAdapter{

        public String[] indexes = {"(A) ", "(B) ", "(C) ", "(D) "};
        public Context context;
        public ArrayList<String> options;
        public int answer, selection;
        public boolean showAnswer;

        public Options(Context context, ArrayList<String> options, int answer) {
            this.options = options;
            this.answer = answer;
            this.context = context;
            showAnswer = false;

            selection = 0;
        }

        public void setOptions(ArrayList<String> options, int answer) {
            this.options = options;
            this.answer = answer;
            showAnswer = false;

            selection = 0;
            notifyDataSetChanged();
        }

        public void showAnswer(){
            showAnswer = true;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return options.size();
        }

        @Override
        public String getItem(int i) {
            return options.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = LayoutInflater.from(context).inflate(R.layout.option_item, viewGroup, false);
            TextView value = (TextView) view.findViewById(R.id.value);
            ImageView img = (ImageView) view.findViewById(R.id.state);

            value.setText(indexes[i] + getItem(i));

            if (!showAnswer){
            if (i == selection) {
                img.setImageResource(android.R.drawable.checkbox_on_background);
            }} else {
                if (selection == i) {
                    img.setImageResource(R.drawable.ic_close_black_24dp);
                    view.setBackgroundColor(context.getResources().getColor(R.color.myRed));
                }
                if (answer == i) {
                    img.setImageResource(R.drawable.ic_check_black_24dp);
                    view.setBackgroundColor(context.getResources().getColor(R.color.myGreen));
                }
            }

            return view;
        }
    }


    public class Question {
        public String question;
        public ArrayList<String> options;
        public int answer;

        public Question(){
            question = "";
            options = new ArrayList<>();
            answer = -1;
        }
        public Question(String question, ArrayList<String> options, int answer) {

            this.question = question;
            this.options = options;
            this.answer = answer;

        }
    }
}
