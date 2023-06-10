package com.example.projekat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projekat.databinding.ActivityMainBinding;
import com.example.projekat.javatube.Youtube;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.parseButton.setOnClickListener(view -> {
            String ytLink = binding.ytInput.getText().toString();
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





