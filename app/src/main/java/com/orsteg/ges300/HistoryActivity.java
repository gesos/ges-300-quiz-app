package com.orsteg.ges300;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.orsteg.harold.dialogs.LoaderDialog;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements Result.EndTestListener {


    public History h;
    public Context con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setTitle("Test History");

        final ListView list = (ListView) findViewById(R.id.history);

        con = this;

        final LoaderDialog dialog = new LoaderDialog(this, true);

        dialog.show();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("scores", Activity.MODE_PRIVATE);

                int c = prefs.getInt("history_c", 0);

                ArrayList<String> s = new ArrayList<>();

                for (int i =0; i<c; i++) {
                    s.add(prefs.getString("score" + (i + 1), "0/0"));
                }

                h = new History(con, s);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        dialog.dismiss();
                        list.setEmptyView(findViewById(R.id.empty));
                        list.setAdapter(h);
                    }
                });

            }
        })).start();

    }

    @Override
    public void end() {
        finish();
    }

    public class History extends BaseAdapter {

        public Context context;
        public ArrayList<String> history;

        public History(Context context, ArrayList<String> history) {
            this.history = history;
            this.context = context;
        }

        @Override
        public int getCount() {
            return history.size();
        }

        @Override
        public String getItem(int i) {
            return history.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            Holder holder;
            if (view == null){
                view = LayoutInflater.from(context).inflate(R.layout.history_item, viewGroup, false);

                holder = new Holder();
                holder.value = (TextView) view.findViewById(R.id.txt);

                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }

            holder.value.setText("(" + (i + 1) + ")   " + getItem(i));

            return view;
        }

        public class Holder {
            TextView value;
        }
    }

}
