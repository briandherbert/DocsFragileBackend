package com.burningaltar.docsfragilebackend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by bherbert on 9/29/17.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Hello world");
        setContentView(tv);
    }
}
