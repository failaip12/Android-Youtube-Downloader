package com.example.projekat.parser;

import java.util.List;

import com.example.projekat.downloader.request.*;
import com.example.projekat.downloader.response.Response;
import com.example.projekat.model.playlist.PlaylistInfo;
import com.example.projekat.model.search.SearchResult;
import com.example.projekat.model.subtitles.SubtitlesInfo;
import com.example.projekat.model.videos.VideoInfo;

public interface Parser {

    /* Video */

    Response<VideoInfo> parseVideo(RequestVideoInfo request);

    /* Playlist */

    Response<PlaylistInfo> parsePlaylist(RequestPlaylistInfo request);

    /* Channel uploads */

    Response<PlaylistInfo> parseChannelsUploads(RequestChannelUploads request);

    /* Subtitles */

    Response<List<SubtitlesInfo>> parseSubtitlesInfo(RequestSubtitlesInfo request);

    /* Search */

    Response<SearchResult> parseSearchResult(RequestSearchResult request);

    Response<SearchResult> parseSearchContinuation(RequestSearchContinuation request);

    Response<SearchResult> parseSearcheable(RequestSearchable request);

}
