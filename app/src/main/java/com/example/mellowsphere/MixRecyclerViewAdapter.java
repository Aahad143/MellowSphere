package com.example.mellowsphere;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MixRecyclerViewAdapter extends RecyclerView.Adapter<MixRecyclerViewAdapter.ViewHolder> {

    static Context context;
    private List<AudioTrackModel> audioList;
    static String album1;
    boolean isPremium;
    //private List<AudioTrackModel> audioList;

    public MixRecyclerViewAdapter(List<AudioTrackModel> audioList, Context context, boolean isPremium) {
        this.audioList = audioList;
        this.context = context;
        this.isPremium = isPremium;
    }

    @NonNull
    @Override
    public MixRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_listview_layout, parent, false);

        // Create a ViewHolder and pass the CardView as the itemView
        return new ViewHolder((ConstraintLayout) view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data for the current position
        AudioTrackModel audio = audioList.get(position);

        // Set data to views in the ViewHolder
        holder.title.setText(audio.getTitle());
        holder.artistName.setText(audio.getArtist());
        album1 = audio.getAlbum();

        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                holder.title.setSelected(true);
            }
        }, 2000);

        // Access the root layout (ConstraintLayout)
        ConstraintLayout rootLayout = (ConstraintLayout) holder.itemView;

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioList != null) {
                    Intent i = new Intent(context, AudioPlayer.class);
                    i.putExtra("TrackName", audio.getTitle());
                    i.putExtra("ArtistName", audio.getArtist());
                    i.putExtra("AlbumName", album1);
                    i.putExtra("TrackList", new ArrayList<>(audioList));
                    i.putExtra("IsPremium", isPremium);
                    context.startActivity(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of your dataset
        return audioList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView albumImage;
        TextView title;
        TextView artistName;
        List<AudioTrackModel> audioList;

        public ViewHolder(ConstraintLayout cl) {
            super(cl);
            title = cl.findViewById(R.id.trackname);
            artistName = cl.findViewById(R.id.artistName);
            audioList = null;
        }
    }
}
