package com.example.mellowsphere;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AudioTrackModel implements Parcelable {
    private String title;
    private String artist;
    private String genre;
    private String album;

    public AudioTrackModel(String title, String artist, String genre, String album) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.album = album;
    }

    protected AudioTrackModel(Parcel in) {
        title = in.readString();
        artist = in.readString();
        genre = in.readString();
        album = in.readString();
    }

    public static final Creator<AudioTrackModel> CREATOR = new Creator<AudioTrackModel>() {
        @Override
        public AudioTrackModel createFromParcel(Parcel in) {
            return new AudioTrackModel(in);
        }

        @Override
        public AudioTrackModel[] newArray(int size) {
            return new AudioTrackModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(genre);
        dest.writeString(album);
    }
}
