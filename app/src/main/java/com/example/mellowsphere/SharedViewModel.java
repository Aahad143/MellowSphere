package com.example.mellowsphere;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<ViewPager2> viewPager2 = new MutableLiveData<>();
    private MutableLiveData<AlbumModelForRecycler> selectedAlbum = new MutableLiveData<>();

    public void onAlbumItemClick(AlbumModelForRecycler album) {
        selectedAlbum.setValue(album);
    }

    public void setViewPager2(ViewPager2 viewPager2) {
        this.viewPager2.setValue(viewPager2);
    }

    public LiveData<ViewPager2> getViewPager2() {
        return viewPager2;
    }

    public LiveData<AlbumModelForRecycler> getSelectedAlbum() {
        return selectedAlbum;
    }
}


