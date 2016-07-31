package com.kara4k.traynotify;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class CreateDelayedNote extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);

        final CheckButton test = (CheckButton) findViewById(R.id.test);
        test.setCustomOnClickListener(new CheckButton.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                if (test.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Checked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "unChecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
