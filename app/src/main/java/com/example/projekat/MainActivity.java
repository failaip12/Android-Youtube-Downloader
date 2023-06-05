package com.example.projekat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;

import com.example.projekat.javatube.Youtube;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Button downloadButton;

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
                                            Youtube yt = new Youtube("https://www.youtube.com/watch?v=VouHJ3lTFCA");
                                            Context context = MainActivity.this;
                                            String savePath = getPathFromUri(context, treeUri); // Get the file path from the picked directory URI
                                            yt.streams().getHighestResolution().download(context, savePath);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    executor.shutdown();
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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





