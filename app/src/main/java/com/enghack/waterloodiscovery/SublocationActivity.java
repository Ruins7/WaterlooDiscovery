package com.enghack.waterloodiscovery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by ruins7 on 2017-02-04.
 */

public class SublocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sublocation_activity);

        Button submit = (Button) findViewById(R.id.sublocation);

        SubmitLocation sub = new SubmitLocation();
        submit.setOnClickListener(sub);

    }

    class SubmitLocation implements View.OnClickListener {

        @Override
        public void onClick(View v) {


        }
    }
}
