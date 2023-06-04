package com.example.projekat.downloader.request;

import com.example.projekat.model.playlist.PlaylistInfo;
import com.example.projekat.model.subtitles.SubtitlesInfo;

import java.util.List;

public class RequestPlaylistInfo extends Request<RequestPlaylistInfo, PlaylistInfo> {

    private final String playlistId;

    public RequestPlaylistInfo(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistId() {
        return playlistId;
    }
}
