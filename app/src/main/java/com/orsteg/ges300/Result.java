package com.orsteg.ges300;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by goodhope on 8/15/18.
 */

public class Result extends Dialog {

    private String score;
    private Context context;
    public Result(@NonNull Context context, String score) {
        super(context);

        this.score = score;
        this.context = context;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.result_layout);

        Button ok = (Button) findViewById(R.id.ok);
        TextView txt = (TextView) findViewById(R.id.score);

        txt.setText(score);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();
                ((EndTestListener) context).end();
            }
        });
    }

    public interface EndTestListener {
        void end();
    }
}
