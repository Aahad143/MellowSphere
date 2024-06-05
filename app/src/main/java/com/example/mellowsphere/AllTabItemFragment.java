package com.example.mellowsphere;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
 * Use the {@link AllTabItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTabItemFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllTabItemFragment() {
        // Required empty public constructor
    }

    boolean isPremium;

    public static AllTabItemFragment newInstance(boolean isPremium) {
        AllTabItemFragment fragment = new AllTabItemFragment();
        Bundle args = new Bundle();
        args.putBoolean("isPremium", isPremium);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllTabItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllTabItemFragment newInstance(String param1, String param2) {
        AllTabItemFragment fragment = new AllTabItemFragment();
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
            isPremium = getArguments().getBoolean("isPremium", false);
        }
    }
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CustomRecyclerView albumRecyclerView;
    private CustomRecyclerView allTrackRecyclerView;
    private SharedViewModel sharedViewModel;
    AlbumRecyclerViewAdapter albumAdapter;
    MixRecyclerViewAdapter mixRecyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tab_item, container, false);

        allTrackRecyclerView = v.findViewById(R.id.mix);

        albumRecyclerView = v.findViewById(R.id.recyclerView);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getViewPager2().observe(getViewLifecycleOwner(), viewPager2 -> {
            if (viewPager2 != null) {
                albumRecyclerView.setViewPager2(viewPager2);

                // Disable ViewPager2 scrolling when RecyclerView is being touched
                albumRecyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                viewPager2.setUserInputEnabled(false);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                viewPager2.setUserInputEnabled(true);
                                break;
                        }
                        return false;
                    }
                });
            }
        });

        albumRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        allTrackRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        fetchDataFromFirestore();

        retrieveAllTracksFromAlbum("alltracks");

        return v;
    }

    private void retrieveAllTracksFromAlbum(String album) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(album.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<AudioTrackModel> audioList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String albumName = document.getString("album");
                        String artistName = document.getString("artist");
                        String genre = document.getString("genre");
                        String title = document.getString("title");

                        // Convert Firestore document to AlbumModelForRecycler
                        AudioTrackModel audio = new AudioTrackModel(title, artistName, genre, albumName);
                        audioList.add(audio);
                    }

                    Collections.shuffle(audioList);

                    mixRecyclerViewAdapter = new MixRecyclerViewAdapter(audioList, getContext(), isPremium);

                    allTrackRecyclerView.setAdapter(mixRecyclerViewAdapter);

                    // Notify the adapter that the data has changed
                    mixRecyclerViewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error in fetching tracks from " + album, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("albums")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<AlbumModelForRecycler> albumList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve only the "album" and "artist" fields
                            String albumName = document.getString("albumName");
                            String artistName = document.getString("artistName");
                            int cover = R.drawable.audiowavesups;

                            // Convert Firestore document to AlbumModelForRecycler
                            AlbumModelForRecycler album = new AlbumModelForRecycler(cover, albumName, artistName);
                            albumList.add(album);
                        }

                        Collections.shuffle(albumList);

                        // Create an instance of the AlbumRecyclerViewAdapter and pass the albumList
                        albumAdapter = new AlbumRecyclerViewAdapter(albumList, getContext(), isPremium);

                        // Set the adapter to the recyclerView
                        albumRecyclerView.setAdapter(albumAdapter);

                        // Notify the adapter that the data has changed
                        albumAdapter.notifyDataSetChanged();
                    } else {
                        // Handle failure
                    }
                }
            });
    }
}