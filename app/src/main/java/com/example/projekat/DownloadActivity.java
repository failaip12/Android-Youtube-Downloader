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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.projekat.javatube.Stream;
import com.example.projekat.javatube.Youtube;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadActivity extends AppCompatActivity {

    Button downloadButton;
    ProgressBar downloadProgressBar;
    ImageView thumbnailImageView;
    TextView titleTextView;
    TextView lengthTextView;
    RadioGroup qualityRadioGroup;

    String ytLink;
    String length;
    String title;
    String thumbnailURL;
    ArrayList<Stream> streams_video;
    ArrayList<Stream> streams_audio;
    ArrayList<Stream> streams;
    static Stream selectedStream;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    DownloadHandler downloadHandler;
    ActivityResultLauncher<Intent> directoryPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri treeUri = data.getData();
                                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                                if (pickedDir != null) {
                                    downloadHandler.download(treeUri);
                                }
                            }
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        // Get references to layout views
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        titleTextView = findViewById(R.id.titleTextView);
        lengthTextView = findViewById(R.id.lengthTextView);

        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        downloadButton = findViewById(R.id.parseButton);
        downloadButton.setEnabled(false);
        ytLink = getIntent().getStringExtra("link");
        downloadHandler = new DownloadHandler(this);
        executor.execute(() -> {
            try {
                Youtube yt = new Youtube(ytLink);
                length = yt.length().toString();
                title = yt.getTitle();
                thumbnailURL = yt.getThumbnailUrl();
                HashMap<String, String> filters = new HashMap<>();
                filters.put("adaptive", "true");
                filters.put("type", "video");
                filters.put("subType", "mp4");
                filters.put("onlyVideo", "true");
                streams_video = yt.streams().filter(filters).orderBy("size").getAll();
                Collections.reverse(streams_video);
                filters.clear();
                filters.put("type", "audio");
                filters.put("onlyAudio", "true");
                streams_audio = yt.streams().filter(filters).orderBy("abr").getAll();
                Collections.reverse(streams_audio);
                streams = new ArrayList<>();
                streams.addAll(streams_video);
                streams.addAll(streams_audio);
                runOnUiThread(() -> {
                    titleTextView.setText(title);
                    lengthTextView.setText(length);
                    Glide.with(this).load(thumbnailURL).into(thumbnailImageView);

                    LinearLayout qualitiesLayout = findViewById(R.id.qualitiesLayout);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    int margin = 18;
                    layoutParams.setMargins(margin, margin, margin, margin);
                    int i = 0;
                    // Create the RadioGroup for video streams
                    qualityRadioGroup = new RadioGroup(this);
                    qualityRadioGroup.setLayoutParams(layoutParams);
                    qualityRadioGroup.setOrientation(LinearLayout.VERTICAL);
                    for (Stream stream : streams_video) {
                        // Create the quality layout
                        LinearLayout qualityLayout = new LinearLayout(this);
                        qualityLayout.setLayoutParams(layoutParams);
                        qualityLayout.setOrientation(LinearLayout.HORIZONTAL);

                        // Create the radio button
                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        radioButton.setPadding(16, 0, 16, 0);
                        radioButton.setText(stream.getResolution());
                        radioButton.setTag(stream);

                        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                                uncheckOtherRadioButtons(qualitiesLayout, radioButton);
                                downloadButton.setEnabled(true);
                            }
                        });

                        TextView FPSTextView = new TextView(this);
                        FPSTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        FPSTextView.setPadding(16, 0, 16, 0);
                        FPSTextView.setText(getString(R.string.fps, String.valueOf(stream.getFps())));

                        // Create the size text view
                        TextView sizeTextView = new TextView(this);
                        sizeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        sizeTextView.setPadding(16, 0, 16, 0);
                        sizeTextView.setText(getString(R.string.size, String.valueOf((int) stream.getFileSize() / 1024)));

                        // Add the radio button and quality text view to the quality layout
                        qualityLayout.addView(radioButton);
                        qualityLayout.addView(FPSTextView);
                        qualityLayout.addView(sizeTextView);

                        // Add the quality layout to the qualitiesLayout
                        qualitiesLayout.addView(qualityLayout);
                    }
                    for (Stream stream : streams_audio) {
                        // Create the quality layout
                        LinearLayout qualityLayout = new LinearLayout(this);
                        qualityLayout.setLayoutParams(layoutParams);
                        qualityLayout.setOrientation(LinearLayout.HORIZONTAL);

                        // Create the radio button
                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        radioButton.setPadding(16, 0, 16, 0);
                        radioButton.setText(R.string.audio_only);
                        radioButton.setTag(stream);

                        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                                uncheckOtherRadioButtons(qualitiesLayout, radioButton);
                                downloadButton.setEnabled(true);
                            }
                        });

                        TextView ABRTextView = new TextView(this);
                        ABRTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        ABRTextView.setPadding(16, 0, 16, 0);
                        ABRTextView.setText(getString(R.string.audio_bitrate, String.valueOf(stream.getAbr())));

                        // Create the size text view
                        TextView sizeTextView = new TextView(this);
                        sizeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        sizeTextView.setPadding(16, 0, 16, 0);
                        sizeTextView.setText(getString(R.string.size, String.valueOf((int) stream.getFileSize() / 1024)));

                        // Add the radio button and quality text view to the quality layout
                        qualityLayout.addView(radioButton);
                        qualityLayout.addView(ABRTextView);
                        qualityLayout.addView(sizeTextView);

                        // Add the quality layout to the qualitiesLayout
                        qualitiesLayout.addView(qualityLayout);
                    }
                    qualitiesLayout.addView(qualityRadioGroup);
                    uncheckOtherRadioButtons(qualitiesLayout, null);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
        // Set click listener for download button
        downloadButton.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.putExtra("ytLink", ytLink);
                    directoryPickerLauncher.launch(intent);
                }
        );
    }

    private static String getPathFromUri(Context context, Uri uri) {
        String documentId = DocumentsContract.getTreeDocumentId(uri);
        String[] parts = documentId.split(":");
        if (parts.length > 1 && "primary".equalsIgnoreCase(parts[0])) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + parts[1] + "/";
        } else {
            return null;
        }
    }

    private static class DownloadHandler {
        private final Context context;

        DownloadHandler(Context context) {
            this.context = context;
        }

        void download(Uri treeUri) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    String savePath = getPathFromUri(context, treeUri); // Get the file path from the picked directory URI
                    selectedStream.download(context, savePath);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
                }
            });
        }
    }
    private void uncheckOtherRadioButtons(LinearLayout qualitiesLayout, RadioButton selectedButton) {
        int childCount = qualitiesLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = qualitiesLayout.getChildAt(i);
            if (childView instanceof LinearLayout) {
                LinearLayout qualityLayout = (LinearLayout) childView;
                int radioCount = qualityLayout.getChildCount();
                for (int j = 0; j < radioCount; j++) {
                    View radioView = qualityLayout.getChildAt(j);
                    if (radioView instanceof RadioButton) {
                        RadioButton radioButton = (RadioButton) radioView;
                        if (radioButton != selectedButton) {
                            radioButton.setChecked(false);
                        }
                    }
                }
            }
        }
    }
}
