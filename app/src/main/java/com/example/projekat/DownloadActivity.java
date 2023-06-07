package com.example.projekat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.projekat.javatube.Stream;
import com.example.projekat.javatube.Youtube;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadActivity extends AppCompatActivity {

    ExecutorService executor = Executors.newSingleThreadExecutor();
    ActivityResultLauncher<Intent> directoryPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri treeUri = data.getData();
                                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                                if (pickedDir != null) {
                                    ExecutorService executor = Executors.newSingleThreadExecutor();
                                    executor.execute(() -> {
                                        try {
                                            String ytlink = "ytLinkText.getText().toString()";
                                            if (ytlink != null) {
                                                Youtube yt = new Youtube(ytlink);
                                                Context context = DownloadActivity.this;
                                                String savePath = getPathFromUri(context, treeUri); // Get the file path from the picked directory URI
                                                yt.streams().getHighestResolution().download(context, savePath);
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    executor.shutdown();
                                }
                            }
                        }
                    });
    Button downloadButton;
    String ytLink;
    String length;
    String title;
    String thumbnailURL;
    Stream FHDQuality;
    Stream HDQuality;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        // Get references to layout views
        ImageView thumbnailImageView = findViewById(R.id.thumbnailImageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView lengthTextView = findViewById(R.id.lengthTextView);

        RadioButton quality1RadioButton = findViewById(R.id.quality1RadioButton);
        TextView quality1TextView = findViewById(R.id.quality1TextView);
        TextView quality1SizeTextView = findViewById(R.id.quality1SizeTextView);

        RadioButton quality2RadioButton = findViewById(R.id.quality2RadioButton);
        TextView quality2TextView = findViewById(R.id.quality2TextView);
        TextView quality2SizeTextView = findViewById(R.id.quality2SizeTextView);

        downloadButton = findViewById(R.id.parseButton);
        ytLink = getIntent().getStringExtra("link");
        executor.execute(() -> {
            try {
                Youtube yt = new Youtube(ytLink);
                length = yt.length().toString();
                title = yt.getTitle();
                thumbnailURL = yt.getThumbnailUrl();
                HashMap<String, String> filters = new HashMap<>();
                filters.put("res", "1080p");
                filters.put("onlyVideo","true");
                FHDQuality = yt.streams().filter(filters).getFirst();
                HashMap<String, String> filters1 = new HashMap<>();
                filters1.put("res", "720p");
                filters1.put("onlyVideo","true");
                HDQuality = yt.streams().filter(filters1).getFirst();
                System.out.println(FHDQuality.getResolution());
                System.out.println(HDQuality.getResolution());
                runOnUiThread(() -> {
                    titleTextView.setText(title);
                    lengthTextView.setText(length);
                    Glide.with(this).load(thumbnailURL).into(thumbnailImageView);

                    quality1TextView.setText(FHDQuality.getResolution());
                    quality1SizeTextView.setText(String.valueOf((int) FHDQuality.getFileSize()));

                    quality2TextView.setText(HDQuality.getResolution());
                    quality2SizeTextView.setText(String.valueOf((int) HDQuality.getFileSize()));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();


        // Enable download button when a radio button is checked
        quality1RadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            downloadButton.setEnabled(isChecked);
        });

        quality2RadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            downloadButton.setEnabled(isChecked);
        });

        // Set click listener for download button
        downloadButton.setOnClickListener(view -> {
            String ytLink = "ytLinkText.getText().toString()";
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("ytLink",ytLink);
            directoryPickerLauncher.launch(intent);
        });
    }

    private String getPathFromUri(Context context, Uri uri) {
        String documentId = DocumentsContract.getTreeDocumentId(uri);
        String[] parts = documentId.split(":");
        if (parts.length > 1 && "primary".equalsIgnoreCase(parts[0])) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + parts[1] + "/";
        } else {
            return null;
        }
    }
}