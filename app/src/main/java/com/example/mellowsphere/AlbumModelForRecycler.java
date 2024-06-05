package com.example.mellowsphere;

public class AlbumModelForRecycler {
    private int imageResource; // Change the type to String
    private String albumName;
    private String artistName;

    public AlbumModelForRecycler(int imageResource, String albumName, String artistName) {
        this.imageResource = imageResource;
        this.albumName = albumName;
        this.artistName = artistName;
    }

    public int getCover() {
        return imageResource;
    }

    public void setCoverUrl(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
