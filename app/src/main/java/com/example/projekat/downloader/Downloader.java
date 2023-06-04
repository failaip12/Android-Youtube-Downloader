package com.example.projekat.downloader;

import com.example.projekat.downloader.request.RequestVideoFileDownload;
import com.example.projekat.downloader.request.RequestVideoStreamDownload;
import com.example.projekat.downloader.request.RequestWebpage;
import com.example.projekat.downloader.response.Response;

import java.io.File;

public interface Downloader {

    Response<String> downloadWebpage(RequestWebpage request);

    Response<File> downloadVideoAsFile(RequestVideoFileDownload request);

    Response<Void> downloadVideoAsStream(RequestVideoStreamDownload request);

}
