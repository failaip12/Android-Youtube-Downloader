package com.example.projekat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projekat.javatube.Youtube;

public class MainActivity extends AppCompatActivity {
    Button parseButton;
    EditText ytLinkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parseButton = findViewById(R.id.parseButton);
        ytLinkText = findViewById(R.id.ytInput);
        parseButton.setOnClickListener(view -> {
            String ytLink = ytLinkText.getText().toString();
            try {
                Youtube yt = new Youtube(ytLink);
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                intent.putExtra("link",ytLink);
                startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Invalid link.", Toast.LENGTH_LONG).show();
            }
        });
    }
}





