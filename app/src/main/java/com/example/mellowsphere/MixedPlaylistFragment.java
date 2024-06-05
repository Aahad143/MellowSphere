package com.example.mellowsphere;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MixedPlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MixedPlaylistFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MixedPlaylistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MixedPlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MixedPlaylistFragment newInstance(String param1, String param2) {
        MixedPlaylistFragment fragment = new MixedPlaylistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    List<AudioTrackModel> audioList = new ArrayList<>();
    ListView listView;
    AudioTrackListViewAdapter audioTrackListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mixed_playlist, container, false);

        listView = v.findViewById(R.id.mixedlistview);

//        retrieveAllTracksFromAlbum("Madvillainy");
//        retrieveAllTracksFromAlbum("Safety On The Moon");
//        retrieveAllTracksFromAlbum("NCS");


        if (audioList.size() != 0) {
            Toast.makeText(getContext(), ""+audioList.size(), Toast.LENGTH_LONG).show();

            Collections.shuffle(audioList);

            audioTrackListViewAdapter = new AudioTrackListViewAdapter(getContext(), audioList);

            listView.setAdapter(audioTrackListViewAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AudioTrackModel selectedTrack = audioTrackListViewAdapter.getAudioList().get(position);

                    if (selectedTrack.getTitle() == null) {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent i = new Intent(getContext(), AudioPlayer.class);
                        i.putExtra("TrackName", selectedTrack.getTitle());
                        i.putExtra("ArtistName", selectedTrack.getArtist());
                        i.putExtra("AlbumName", selectedTrack.getAlbum());
                        i.putExtra("TrackList", new ArrayList<>(audioList));
                        startActivity(i);
                    }
                }
            });
        }
        return v;
    }
}