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
import android.view.Gravity;
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

    // Views
    private ImageView thumbnailImageView;
    private TextView titleTextView;
    private TextView lengthTextView;
    private ProgressBar downloadProgressBar;
    private Button downloadButton;
    private LinearLayout qualitiesLayout;

    // YouTube data
    private String ytLink;
    private String length;
    private String title;
    private String thumbnailURL;
    private ArrayList<Stream> streams_video;
    private ArrayList<Stream> streams_audio;
    private ArrayList<Stream> streams;
    private static Stream selectedStream;

    // Executor for background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadHandler downloadHandler;

    // Activity result launcher for directory picker
    private ActivityResultLauncher<Intent> directoryPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        initializeViews();
        extractYouTubeData();
        initializeDirectoryPickerLauncher();
        downloadButton.setEnabled(false);

        downloadHandler = new DownloadHandler(this);
        downloadButton.setOnClickListener(view -> pickDirectoryForDownload());
    }

    private void initializeViews() {
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        titleTextView = findViewById(R.id.titleTextView);
        lengthTextView = findViewById(R.id.lengthTextView);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);
        downloadButton = findViewById(R.id.downloadButton);
        qualitiesLayout = findViewById(R.id.qualitiesLayout);
    }

    private void extractYouTubeData() {
        ytLink = getIntent().getStringExtra("link");
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
                    displayYouTubeData();
                    populateQualitiesLayout();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayYouTubeData() {
        titleTextView.setText(title);
        lengthTextView.setText(length);
        Glide.with(this).load(thumbnailURL).into(thumbnailImageView);
    }

    private void populateQualitiesLayout() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = 18;
        layoutParams.setMargins(margin, margin, margin, margin);

        // Create the RadioGroup for video streams
        RadioGroup qualityRadioGroup = new RadioGroup(this);
        qualityRadioGroup.setLayoutParams(layoutParams);
        qualityRadioGroup.setOrientation(LinearLayout.VERTICAL);

        for (Stream stream : streams_video) {
            LinearLayout qualityLayout = createQualityLayout(layoutParams);
            RadioButton radioButton = createVideoRadioButton(stream);
            qualityLayout.addView(radioButton);
            qualityLayout.addView(createFPSTextView(stream));
            qualityLayout.addView(createSizeTextView(stream));
            qualitiesLayout.addView(qualityLayout);
        }

        for (Stream stream : streams_audio) {
            LinearLayout qualityLayout = createQualityLayout(layoutParams);
            RadioButton radioButton = createAudioRadioButton(stream);
            qualityLayout.addView(radioButton);
            qualityLayout.addView(createABRTextView(stream));
            qualityLayout.addView(createSizeTextView(stream));
            qualitiesLayout.addView(qualityLayout);
        }

        qualitiesLayout.addView(qualityRadioGroup);
        uncheckOtherRadioButtons(qualitiesLayout, null);
    }

    private LinearLayout createQualityLayout(LinearLayout.LayoutParams layoutParams) {
        LinearLayout qualityLayout = new LinearLayout(this);
        qualityLayout.setLayoutParams(layoutParams);
        qualityLayout.setOrientation(LinearLayout.HORIZONTAL);
        qualityLayout.setGravity(Gravity.CENTER_VERTICAL);
        return qualityLayout;
    }

    private RadioButton createVideoRadioButton(Stream stream) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setPadding(16, 0, 16, 0);
        radioButton.setText(stream.getResolution());
        radioButton.setTag(stream);
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedStream = (Stream) buttonView.getTag();
            }
        });
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                uncheckOtherRadioButtons(qualitiesLayout, radioButton);
                downloadButton.setEnabled(true);
            }
        });
        return radioButton;
    }

    private RadioButton createAudioRadioButton(Stream stream) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setPadding(16, 0, 16, 0);
        radioButton.setText(R.string.audio_only);
        radioButton.setTag(stream);
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedStream = (Stream) buttonView.getTag();
            }
        });
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                uncheckOtherRadioButtons(qualitiesLayout, radioButton);
                downloadButton.setEnabled(true);
            }
        });
        return radioButton;
    }

    private TextView createFPSTextView(Stream stream) {
        TextView fpsTextView = new TextView(this);
        fpsTextView.setPadding(16, 0, 16, 0);
        fpsTextView.setText(getString(R.string.fps, String.valueOf(stream.getFps())));
        return fpsTextView;
    }

    private TextView createSizeTextView(Stream stream) {
        TextView sizeTextView = new TextView(this);
        sizeTextView.setPadding(16, 0, 16, 0);
        sizeTextView.setText(getString(R.string.size, String.valueOf((int) stream.getFileSize() / 1024)));
        return sizeTextView;
    }

    private TextView createABRTextView(Stream stream) {
        TextView abrTextView = new TextView(this);
        abrTextView.setPadding(16, 0, 16, 0);
        abrTextView.setText(getString(R.string.audio_bitrate, String.valueOf(stream.getAbr())));
        return abrTextView;
    }

    private void pickDirectoryForDownload() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        directoryPickerLauncher.launch(intent);
    }

    private void initializeDirectoryPickerLauncher() {
        directoryPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri treeUri = data.getData();
                            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                            if (pickedDir != null) {
                                downloadHandler.download(treeUri);
                            }
                        }
                    }
                });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
