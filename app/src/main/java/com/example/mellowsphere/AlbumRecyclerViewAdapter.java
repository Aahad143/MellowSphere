package com.example.mellowsphere;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder> {

    static Context context;
    private List<AlbumModelForRecycler> albumList;
    static boolean isPremium;
    //private List<AudioTrackModel> audioList;

    public AlbumRecyclerViewAdapter(List<AlbumModelForRecycler> albumList, Context context, boolean isPremium) {
        this.albumList = albumList;
        this.context = context;
        this.isPremium = isPremium;
    }

    @NonNull
    @Override
    public AlbumRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_recyclerview, parent, false);

        // Create a ViewHolder and pass the CardView as the itemView
        return new ViewHolder((CardView) view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data for the current position
        AlbumModelForRecycler album = albumList.get(position);
//        CardView cv = holder.cv;
//        ImageView albumImage = cv.findViewById(R.id.albumImage);
//        TextView albumName= cv.findViewById(R.id.albumName);
//        TextView artistName = cv.findViewById(R.id.artistName);

        // Set data to views in the ViewHolder
        holder.albumImage.setImageResource(album.getCover());
        holder.albumName.setText(album.getAlbumName());
        holder.artistName.setText(album.getArtistName());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(album.getAlbumName().toLowerCase()).get().
            addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<AudioTrackModel> audioTrackModelList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String albumName = document.getString("album");
                        String artistName = document.getString("artist");
                        String genre = document.getString("genre");
                        String title = document.getString("title");

                        // Convert Firestore document to AlbumModelForRecycler
                        AudioTrackModel audio = new AudioTrackModel(title, artistName, genre, albumName);
                        audioTrackModelList.add(audio);

                        holder.audioList = audioTrackModelList;
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of your dataset
        return albumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        TextView artistName;
        CardView cv;
        List<AudioTrackModel> audioList;

        public ViewHolder(CardView cardView) {
            super(cardView);
            albumImage = cardView.findViewById(R.id.albumImage);
            albumName = cardView.findViewById(R.id.albumName);
            artistName = cardView.findViewById(R.id.artistName);
            audioList = new ArrayList<>();


            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (audioList != null){
                        ((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, AudioListViewFragment.newInstance(audioList, isPremium)).commit();
                    }
                }
            });
        }
    }

}
