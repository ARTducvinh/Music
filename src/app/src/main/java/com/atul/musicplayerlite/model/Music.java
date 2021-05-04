package com.atul.musicplayerlite.model;

import org.jetbrains.annotations.NotNull;

public class Music {
    public String artist;
    public String title;
    public String displayName;
    public String album;
    public String relativePath;
    public String launchedBy;

    public int year;
    public int track;
    public int startFrom;
    public int dateAdded;

    public long id;
    public long duration;
    public long albumId;

    public Music(String artist, String title, String displayName, String album, String relativePath, String launchedBy,
                 int year, int track, int startFrom, int dateAdded,
                 long id, long duration, long albumId) {
        this.artist = artist;
        this.title = title;
        this.displayName = displayName;
        this.album = album;
        this.relativePath = relativePath;
        this.launchedBy = launchedBy;
        this.year = year;
        this.track = track;
        this.startFrom = startFrom;
        this.dateAdded = dateAdded;
        this.id = id;
        this.duration = duration;
        this.albumId = albumId;
    }

    @NotNull
    @Override
    public String toString() {
        return "Music{" +
                "artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", displayName='" + displayName + '\'' +
                ", album='" + album + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", launchedBy='" + launchedBy + '\'' +
                ", year=" + year +
                ", track=" + track +
                ", startFrom=" + startFrom +
                ", dateAdded=" + dateAdded +
                ", id=" + id +
                ", duration=" + duration +
                ", albumId=" + albumId +
                '}';
    }
}