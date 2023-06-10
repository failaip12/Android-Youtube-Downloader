package com.example.projekat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.projekat.javatube.Stream;
import com.example.projekat.javatube.Youtube;

import com.arthenica.mobileffmpeg.Config;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.projekat.databinding.ActivityDownloadBinding;
public class DownloadActivity extends AppCompatActivity {

    // View Binding
    private ActivityDownloadBinding binding;

    // YouTube data
    private String ytLink;
    private String length;
    private String title;
    private String thumbnailURL;

    //Streams
    private ArrayList<Stream> streams_video;
    private ArrayList<Stream> streams_audio;
    private Stream best_audio_stream;
    private static Stream selectedStream;

    // Executor for background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DownloadHandler downloadHandler;

    // Activity result launcher for directory picker
    private ActivityResultLauncher<Intent> directoryPickerLauncher;
    //Progress Bar
    private Handler progressHandler;
    private Runnable progressRunnable;
    //Combine audio and video
    private String savePathVideo;
    private String savePathAudio;
    private String savePathCombined;
    private boolean convert;
    private boolean download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        convert = false;
        download = false;
        extractYouTubeData();
        initializeDirectoryPickerLauncher();
        binding.downloadButton.setEnabled(false);

        downloadHandler = new DownloadHandler(this);
        binding.downloadButton.setOnClickListener(view -> pickDirectoryForDownload());

        progressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                // Update progress text\
                if (selectedStream != null && !convert && download) {
                    binding.downloadProgressBar.setProgress((int) Stream.getProgress());
                    binding.downloadButton.setEnabled(false);
                }
                if(convert) {
                    binding.downloadButton.setText(R.string.converting);
                    binding.downloadButton.setEnabled(false);
                }
                if(!convert && !download && selectedStream != null) {
                    binding.downloadButton.setText(R.string.download);
                    binding.downloadButton.setEnabled(true);
                }
                // Call the runnable again after a delay
                progressHandler.postDelayed(this, 100); // Update every 1 second
            }
        };

        // Start updating progress
        progressHandler.post(progressRunnable);
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
                filters.put("mineType", "video/mp4");
                streams_video = yt.streams().filter(filters).orderBy("size").getAll();
                Collections.reverse(streams_video);
                filters.clear();
                filters.put("onlyAudio", "true");
                streams_audio = yt.streams().filter(filters).orderBy("abr").getAll();
                Collections.reverse(streams_audio);
                best_audio_stream = yt.streams().getOnlyAudio();
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
        binding.titleTextView.setText(title);
        if(Integer.parseInt(length) < 60) {
            binding.lengthTextView.setText(length);
        }
        else {
            String minutes = String.valueOf(Integer.parseInt(length) / 60);
            String seconds = String.valueOf(Integer.parseInt(length) % 60);
            if(Integer.parseInt(seconds)<10)
                seconds = "0" + seconds;
            binding.lengthTextView.setText(getString(R.string.video_length, minutes, seconds));
        }
        Glide.with(this).load(thumbnailURL).into(binding.thumbnailImageView);
    }

    private void populateQualitiesLayout() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = 18;
        layoutParams.setMargins(margin, margin, margin, margin);

        for (Stream stream : streams_video) {
            LinearLayout qualityLayout = createQualityLayout(layoutParams);
            RadioButton radioButton = createVideoRadioButton(stream);
            qualityLayout.addView(radioButton);
            qualityLayout.addView(createFPSTextView(stream));
            qualityLayout.addView(createSizeTextView(stream));
            binding.qualitiesLayout.addView(qualityLayout);
        }

        for (Stream stream : streams_audio) {
            LinearLayout qualityLayout = createQualityLayout(layoutParams);
            RadioButton radioButton = createAudioRadioButton(stream);
            qualityLayout.addView(radioButton);
            qualityLayout.addView(createABRTextView(stream));
            qualityLayout.addView(createSizeTextView(stream));
            binding.qualitiesLayout.addView(qualityLayout);
        }
        uncheckOtherRadioButtons(binding.qualitiesLayout, null);
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
                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                uncheckOtherRadioButtons(binding.qualitiesLayout, radioButton);
                binding.downloadButton.setEnabled(true);
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
                selectedStream = (Stream) buttonView.getTag(); // Get the selected stream from the tag
                uncheckOtherRadioButtons(binding.qualitiesLayout, radioButton);
                binding.downloadButton.setEnabled(true);
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
        String filesizeKB = String.valueOf((int) stream.getFileSize() / 1024 + best_audio_stream.getFileSize() / 1024);
        TextView sizeTextView = new TextView(this);
        sizeTextView.setPadding(16, 0, 16, 0);
        sizeTextView.setText(getString(R.string.sizeKB,filesizeKB));
        if(Integer.parseInt(filesizeKB) > 10000)
            sizeTextView.setText(getString(R.string.sizeMB, String.valueOf(Integer.parseInt(filesizeKB) / 1024)));
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
    private static String getPathFromUri(Uri uri) {
        String documentId = DocumentsContract.getTreeDocumentId(uri);
        String[] parts = documentId.split(":");
        if (parts.length > 1 && "primary".equalsIgnoreCase(parts[0])) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + parts[1] + "/";
        } else {
            return null;
        }
    }
    private class DownloadHandler {
        private final Context context;
        DownloadHandler(Context context) {
            this.context = context;
        }
        void download(Uri treeUri) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    String savePath = getPathFromUri(treeUri); // Get the file path from the picked directory URI
                    download = true;
                    selectedStream.download(context, savePath);
                    if(streams_video.contains(selectedStream)) {
                        savePathVideo = savePath + selectedStream.safeFileName(selectedStream.getTitle()) + selectedStream.getFileSize() + "." + selectedStream.getSubType();
                        best_audio_stream.download(context, savePath);
                        download = false;
                        savePathAudio = savePath + best_audio_stream.safeFileName(best_audio_stream.getTitle()) + best_audio_stream.getFileSize() + "." + best_audio_stream.getSubType();
                        convert = true;
                        savePathCombined = savePath + selectedStream.safeFileName(selectedStream.getTitle()) + "." + selectedStream.getSubType();
                        convertUsingFFMpeg(savePathVideo, savePathAudio, savePathCombined);
                    }
                    download = false;
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

    private void convertUsingFFMpeg(String videoPath, String audioPath, String combinedPath) {
        int videoLength = MediaPlayer.create(DownloadActivity.this, Uri.parse(videoPath)).getDuration();
        Config.enableStatisticsCallback(newStatistics -> {
            float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / videoLength;
            float progressFinal = progress * 100;
            binding.downloadProgressBar.setProgress((int) progressFinal);
        });

        FFmpeg.executeAsync("-i " + videoPath  + " -i " + audioPath + " -c:v copy -c:a aac " + combinedPath, (executionId, returnCode) -> {
            if (returnCode == RETURN_CODE_SUCCESS) {
                Toast.makeText(DownloadActivity.this, "All done, now you can watch your video.", Toast.LENGTH_SHORT).show();
                deleteTempFile(videoPath);
                deleteTempFile(audioPath);
                convert = false;
            } else if (returnCode == RETURN_CODE_CANCEL) {
                convert = false;
            } else {
                Toast.makeText(DownloadActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void deleteTempFile(String filePath) {
        File file = new File(filePath);
        boolean deleted = file.delete();
        if (deleted) {
            Log.i(Config.TAG, "File deleted: " + filePath);
        } else {
            Log.i(Config.TAG, "Failed to delete file: " + filePath);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(progressRunnable);
        executor.shutdownNow();
    }
}
