package com.example.mellowsphere;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class AudioTrackListViewAdapter extends BaseAdapter {
    List<AudioTrackModel> audioList;
    Context context;

    public AudioTrackListViewAdapter(Context context, List<AudioTrackModel> audioList) {
        this.audioList = audioList;
        this.context = context;
    }

    public List<AudioTrackModel> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<AudioTrackModel> audioList) {
        this.audioList = audioList;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return audioList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.audio_listview_layout, parent, false);
        TextView trackname = convertView.findViewById(R.id.trackname);
        TextView artistname = convertView.findViewById(R.id.artistName);
        ImageView download = convertView.findViewById(R.id.download);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trackname.setSelected(true);
            }
        }, 2000);

        trackname.setText(audioList.get(position).getTitle());
        artistname.setText(audioList.get(position).getArtist());


        return convertView;
    }
}
