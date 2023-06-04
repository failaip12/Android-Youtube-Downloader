package com.example.projekat.downloader.request;

import com.example.projekat.model.subtitles.SubtitlesInfo;
import com.example.projekat.model.videos.VideoInfo;

import java.util.List;

public class RequestSubtitlesInfo extends Request<RequestSubtitlesInfo, List<SubtitlesInfo>> {

    private final String videoId;

    public RequestSubtitlesInfo(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoId() {
        return videoId;
    }
}
