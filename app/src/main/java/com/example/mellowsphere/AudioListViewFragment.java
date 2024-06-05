package com.example.mellowsphere;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioListViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private List<AudioTrackModel> audioList = new ArrayList<>();
    boolean isPremium;


    public AudioListViewFragment() {
        // Required empty public constructor
    }

    public AudioListViewFragment(List<AudioTrackModel> audioList, boolean isPremium) {
        this.audioList = audioList;
        this.isPremium = isPremium;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AudioListViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioListViewFragment newInstance(String param1, String param2) {
        AudioListViewFragment fragment = new AudioListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioListViewFragment newInstance(List<AudioTrackModel> audioList, boolean isPremium) {
        AudioListViewFragment fragment = new AudioListViewFragment();
        Bundle args = new Bundle();
        args.putBoolean("isPremium", isPremium);
        args.putParcelableArrayList("audioList", new ArrayList<>(audioList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            isPremium = getArguments().getBoolean("isPremium", false);
            audioList = getArguments().getParcelableArrayList("audioList");
        }
    }

    ListView listView;
    AudioTrackListViewAdapter audioTrackListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_audio_list_view, container, false);

        listView = v.findViewById(R.id.listView);

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
                    i.putExtra("IsPremium", isPremium);
                    startActivity(i);
                }
            }
        });

        return v;
    }
}