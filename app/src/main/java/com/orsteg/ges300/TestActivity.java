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

    public Button end, next, previous;
    public ListView options;
    public TextView index, question;


    public Options optionAdapter;
    public AdapterView.OnItemClickListener listener;
    public ArrayList<Question> questions;
    public ArrayList<Integer> rndInts;
    public ArrayList<Integer> states;

    public int position;

    public Random rnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        setTitle("GES 300 Test");

        end = (Button) findViewById(R.id.finish);
        next = (Button) findViewById(R.id.next);
        previous = (Button) findViewById(R.id.previous);
        options = (ListView) findViewById(R.id.options);
        index = (TextView) findViewById(R.id.index);
        question = (TextView) findViewById(R.id.question);

        prefs = getSharedPreferences("scores", Activity.MODE_PRIVATE);
        questionCount = 0;
        correctCount = 0;

        position = -1;
        states = new ArrayList<>();

        if (savedInstanceState != null) {

            rndInts = savedInstanceState.getIntegerArrayList("rndInts");
            position = savedInstanceState.getInt("position");
            questionCount = savedInstanceState.getInt("qCount");
            correctCount = savedInstanceState.getInt("cCount");
            states = savedInstanceState.getIntegerArrayList("states");
        }
        rnd = new Random();

        questions = new ArrayList<>();
        optionAdapter = new Options(this, -1);

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTest();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questions.size() > 0)
                if(questions.get(position).showAnswer){
                    nextQuestion();
                } else {
                    checkAnswer();

                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position > 0) previousQuestion();
            }
        });


        listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                states.remove(position);

                states.add(position, i);

                questions.get(position).selection = i;

                optionAdapter.notifyDataSetChanged();
            }
        };


        getQuestions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntegerArrayList("rndInts", rndInts);
        outState.putInt("position", position - 1);
        outState.putInt("qCount", questionCount);
        outState.putInt("cCount", correctCount);
        outState.putIntegerArrayList("states", states);
    }

    public void getQuestions(){
        final LoaderDialog dialog = new LoaderDialog(this, true);

        dialog.show();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream questionSheet = getResources().openRawResource(R.raw.sheet);

                boolean state = rndInts != null;
                if (!state) rndInts = new ArrayList<>();

                int r = 0;
                int line = 0;
                StringBuilder ex = new StringBuilder();
                Question q = new Question();

                int p = 0;

                try {
                    while ((r = questionSheet.read()) != -1) {

                        if ((char) r == '\n' && (char) p != '\n') {

                            int l = line % 5;
                            if (l == 0){
                                q.question = ex.toString().replaceFirst("(\\s*)(\\d+)(\\.)(\\s*)", "");
                            } else if (l >= 1 && l <= 4) {

                                String s = ex.toString();
                                boolean a = s.matches("(\\s*)(\\*)([abcd]+)(.*)");

                                String opt = s.replaceFirst("(\\s*)([*abcd]+)(\\.)(\\s*)", "");

                                q.options.add(opt);

                                if (a) q.answer = q.options.size() - 1;


                                if (l == 4) {


                                    int ind2;

                                    if (!state) {
                                        ind2 = rnd.nextInt(questions.size()+1);
                                        rndInts.add(ind2);
                                    }
                                    else ind2 = rndInts.get(questions.size());

                                    questions.add(ind2, q);

                                    q = new Question();
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

                if (state) {
                    for (int i = 0; i < questionCount; i++) {
                        questions.get(i).showAnswer = true;
                        questions.get(i).selection = states.get(i);
                    }

                    if (states.size() > questionCount) {
                        questions.get(states.size()-1).selection = states.get(states.size()-1);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                        if (questions.size() > 0) {
                            optionAdapter.index = 0;
                            options.setAdapter(optionAdapter);
                        }
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

        if (questions.get(position).answer == questions.get(position).selection) {
            correctCount++;
        }
    }

    public void previousQuestion() {
        position--;


        options.setOnItemClickListener(null);
        next.setText("Next");


        index.setText("(" + (position + 1) + ")  of  (378)");

        question.setText(questions.get(position).question);
        optionAdapter.setOptions(position);

    }

    public void nextQuestion(){

        if (position < questions.size()-1) {

            position ++;

            if (!questions.get(position).showAnswer){
                options.setOnItemClickListener(listener);
                next.setText("Continue");
            } else {
                options.setOnItemClickListener(null);
                next.setText("Next");
            }

            index.setText("(" + (position + 1) + ")  of  (378)");

            question.setText(questions.get(position).question);
            optionAdapter.setOptions(position);

            if (states.size() == position) states.add(0);

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
        public int index;

        public Options(Context context, int index) {
            this.index = index;
            this.context = context;
        }

        public void setOptions(int index) {
            this.index = index;
            notifyDataSetChanged();
        }

        public void showAnswer(){
            questions.get(index).showAnswer = true;
            notifyDataSetChanged();
        }

        public Question getQ() {
            return questions.get(index);
        }

        @Override
        public int getCount() {
            return getQ().options.size();
        }

        @Override
        public String getItem(int i) {
            return getQ().options.get(i);
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

            if (!getQ().showAnswer){
            if (i == getQ().selection) {
                img.setImageResource(android.R.drawable.checkbox_on_background);
            }} else {
                if (getQ().selection == i) {
                    img.setImageResource(R.drawable.ic_close_black_24dp);
                    view.setBackgroundColor(context.getResources().getColor(R.color.myRed));
                }
                if (getQ().answer == i) {
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
        public int answer, selection;
        public boolean showAnswer;

        public Question(){
            question = "";
            options = new ArrayList<>();
            answer = -1;
            selection = 0;
            showAnswer = false;
        }
    }
}
