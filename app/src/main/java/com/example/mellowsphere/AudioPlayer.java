package com.example.mellowsphere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AudioPlayer extends AppCompatActivity {
    Handler seekBarHandler;
    boolean islooped = false;
    Button playOrPause, btnForward, btnBack, loopBtn;
    String url, track, artist, album, localFilePath;
    List<AudioTrackModel> audioTrackModelList = new ArrayList<>();
    ImageView download;
    TextView trackname, artistname, numberOfFavs, trackDuration, currentTime;
    StorageReference storageReference;
    SeekBar seekBar;
    Button favourite;
    boolean isFavourited = false;
    private AudioPlayerService mediaPlayerService;
    boolean isUserPremium = false;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            // Now you have a reference to your service

            init();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlayerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPlaybackService();

        setContentView(R.layout.activity_audio_player);


    }

    public void init(){
        seekBarHandler = new Handler();


        playOrPause = findViewById(R.id.playorpause);
        playOrPause.setBackgroundResource(R.drawable.ic_pause);

        btnBack = findViewById(R.id.btnback);
        btnForward = findViewById(R.id.btnforward);

        favourite = findViewById(R.id.favouriteaudioplayer);

        loopBtn = findViewById(R.id.loopbtn);

        seekBar = findViewById(R.id.seekBarTime);
        seekBar.setProgress(0);

        download = findViewById(R.id.download);

        trackname = findViewById(R.id.trackname);
        artistname = findViewById(R.id.artistname);
        trackDuration = findViewById(R.id.tvDuration);
        currentTime = findViewById(R.id.currDuration);
        numberOfFavs = findViewById(R.id.numberoffavs);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            track = b.getString("TrackName");
            artist = b.getString("ArtistName");
            album = b.getString("AlbumName");
            url = b.getString("AudioUrl");
            isUserPremium = b.getBoolean("IsPremium");
            audioTrackModelList = b.getParcelableArrayList("TrackList");

            trackname.setText(track);
            artistname.setText(artist);
        } else {
            finish();
        }

        // Using a handler to delay the start of the scrolling
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            trackname.setSelected(true);
            artistname.setSelected(true);

            if (isAudioFileDownloaded(track+".mp3")){
                Toast.makeText(this, "Playing offline", Toast.LENGTH_SHORT).show();


                playAudio(localFilePath);
                download.setVisibility(View.GONE);

                if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                    playOrPause.setOnClickListener(v -> {
                        if (mediaPlayerService.isPlaying()) {
                            mediaPlayerService.pauseAudio();
                            playOrPause.setBackgroundResource(R.drawable.ic_play);
                        }
                        else {
                            playAudio(localFilePath);
                            playOrPause.setBackgroundResource(R.drawable.ic_pause);
                        }
                    });
                }
            }
        }, 2000);

        seekBarHandler.postDelayed(updateSeekBar, 100);

        storageReference = FirebaseStorage.getInstance().getReference().child(album.toLowerCase()+"/"+track+".mp3");

        storageReference.getDownloadUrl().addOnSuccessListener((OnSuccessListener<Uri>) uri -> {
            String link = uri.toString();

            // Download the audio file using DownloadManager
            if (!isAudioFileDownloaded(track+".mp3")){
                Toast.makeText(AudioPlayer.this, "Playing online", Toast.LENGTH_SHORT).show();
                playAudio(link);
                download.setVisibility(View.VISIBLE);
            }
            else {
                link = localFilePath;
            }

            download.setOnClickListener(v -> {
                if (user == null) {
                    Intent i = new Intent(getApplicationContext(), Register.class);
                    startActivity(i);
                    Toast.makeText(AudioPlayer.this, "Login required", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isUserPremium)  {
                        downloadAudio(uri.toString(), track+".mp3");
                        download.setVisibility(View.GONE);
                    }
                    else {
                        displayPremiumOption();
                    }
                }
            });

            String finalLink = link;
            playOrPause.setOnClickListener(v -> {
                if (mediaPlayerService.isPlaying()) {
                    mediaPlayerService.pauseAudio();
                    playOrPause.setBackgroundResource(R.drawable.ic_play);
                }
                else {
                    playAudio(finalLink);
                    playOrPause.setBackgroundResource(R.drawable.ic_pause);
                }
            });

        });

        btnBack.setOnClickListener(v -> {
            for (int i = 0; i < audioTrackModelList.size(); i++) {
                if (audioTrackModelList.get(i).getTitle().equals(track)){
                    try {
                        Intent i2 = new Intent(getApplicationContext(), AudioPlayer.class);

                        i2.putExtra("TrackName", audioTrackModelList.get(i-1).getTitle());
                        i2.putExtra("ArtistName", audioTrackModelList.get(i-1).getArtist());
                        i2.putExtra("AlbumName", audioTrackModelList.get(i-1).getAlbum());
                        i2.putExtra("TrackList", new ArrayList<>(audioTrackModelList));
                        i2.putExtra("IsPremium", isUserPremium);

                        finish();

                        startActivity(i2);
                    }
                    catch (IndexOutOfBoundsException e) {
                        Intent i2 = new Intent(getApplicationContext(), AudioPlayer.class);

                        i2.putExtra("TrackName", audioTrackModelList.get(audioTrackModelList.size()-1).getTitle());
                        i2.putExtra("ArtistName", audioTrackModelList.get(audioTrackModelList.size()-1).getArtist());
                        i2.putExtra("AlbumName", audioTrackModelList.get(audioTrackModelList.size()-1).getAlbum());
                        i2.putExtra("TrackList", new ArrayList<>(audioTrackModelList));
                        i2.putExtra("IsPremium", isUserPremium);

                        finish();

                        startActivity(i2);
                    }
                }
            }
        });

        btnForward.setOnClickListener(v -> {
            for (int i = 0; i < audioTrackModelList.size(); i++) {
                if (audioTrackModelList.get(i).getTitle().equals(track)){
                    try {
                        Intent i2 = new Intent(getApplicationContext(), AudioPlayer.class);

                        i2.putExtra("TrackName", audioTrackModelList.get(i+1).getTitle());
                        i2.putExtra("ArtistName", audioTrackModelList.get(i+1).getArtist());
                        i2.putExtra("AlbumName", audioTrackModelList.get(i+1).getAlbum());
                        i2.putExtra("TrackList", new ArrayList<>(audioTrackModelList));
                        i2.putExtra("IsPremium", isUserPremium);

                        startActivity(i2);

                        finish();
                    }
                    catch (IndexOutOfBoundsException e) {
                        Intent i2 = new Intent(getApplicationContext(), AudioPlayer.class);

                        i2.putExtra("TrackName", audioTrackModelList.get(0).getTitle());
                        i2.putExtra("ArtistName", audioTrackModelList.get(0).getArtist());
                        i2.putExtra("AlbumName", audioTrackModelList.get(0).getAlbum());
                        i2.putExtra("TrackList", new ArrayList<>(audioTrackModelList));
                        i2.putExtra("IsPremium", isUserPremium);

                        startActivity(i2);

                        finish();
                    }
                }
            }
        });

        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                islooped = !islooped;
                mediaPlayerService.setLoop(islooped);
                if (islooped) {
                    setBackgroundTint(loopBtn, R.color.loop);
                }
                else {
                    setBackgroundTint(loopBtn, R.color.white);
                }
            }
        });

        if (user != null) {
            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFavourited = !isFavourited;
                    if (isFavourited) {
                        setBackgroundTint(favourite, R.color.favourite);

                        // Query the document for the track
                        db.collection("alltracks").whereEqualTo("title", track).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            // Retrieve the existing favouritedby field
                                            ArrayList<String> favouritedBy = (ArrayList<String>) document.get("favouritedby");

                                            // Check if it's null, and initialize it if necessary
                                            if (favouritedBy == null) {
                                                favouritedBy = new ArrayList<>();
                                            }

                                            // Check if the user ID is not already in the list before adding it
                                            if (!favouritedBy.contains(user.getUid())) {
                                                favouritedBy.add(user.getUid());

                                                // Update the document with the modified favouritedby field
                                                ArrayList<String> finalFavouritedBy = favouritedBy;
                                                db.collection("alltracks").document(document.getId())
                                                    .update("favouritedby", favouritedBy)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Update successful
                                                            String Str;

                                                            if (finalFavouritedBy.size() > 1) {
                                                                Str = finalFavouritedBy.size()+" users love this track";
                                                            }
                                                            else {
                                                                Str = "You are the only one who loves this track";
                                                            }

                                                            numberOfFavs.setText(Str);
                                                            Toast.makeText(getApplicationContext(), "Track favourited.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            }
                                        }
                                    }
                                }
                            });
                    }
                    else {
                        setBackgroundTint(favourite, R.color.white);

                        // Query the document for the track
                        db.collection("alltracks").whereEqualTo("title", track).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            // Retrieve the existing favouritedby field
                                            ArrayList<String> favouritedBy = (ArrayList<String>) document.get("favouritedby");

                                            // Check if it's not null and remove the user ID
                                            if (favouritedBy != null) {
                                                favouritedBy.remove(user.getUid());

                                                // Update the document with the modified favouritedby field
                                                db.collection("alltracks").document(document.getId())
                                                    .update("favouritedby", favouritedBy)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Update successful
                                                            numberOfFavs.setText("");
                                                            Toast.makeText(getApplicationContext(), "Track unfavourited", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            }
                                        }
                                    }
                                }
                            });
                    }
                }
            });
        }


    }

    public void checkIfFavourited() {
        if (user != null && (favourite.getVisibility() == View.GONE)) {
            db.collection("alltracks").whereEqualTo("title", track).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve the existing favouritedby field
                                ArrayList<String> favouritedBy = (ArrayList<String>) document.get("favouritedby");

                                favourite.setVisibility(View.VISIBLE);
                                if (favouritedBy != null) {
                                    if (favouritedBy.contains(user.getUid())) {
                                        isFavourited = true;
                                        setBackgroundTint(favourite, R.color.favourite);
                                        // Update successful
                                        // Update successful
                                        String Str;

                                        if (favouritedBy.size() > 1) {
                                            Str = favouritedBy.size()+" users love this track";

                                        }
                                        else {
                                            Str = "You are the only one who loves this track";
                                        }

                                        numberOfFavs.setText(Str);

                                    }
                                    else {
                                        isFavourited = false;
                                        setBackgroundTint(favourite, R.color.white);
                                        numberOfFavs.setText("");
                                    }
                                }
                            }
                        }
                    }
                });
        }
    }

    public void setBackgroundTint(Button btn, int colorid) {
        // Create a new color state list for the desired tint color
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled }, // enabled
                // Add more states if needed, e.g., disabled, pressed, etc.
        };

        int[] colors = new int[] {
                // Color for the enabled state
                ContextCompat.getColor(getApplicationContext(), colorid),
                // Add more colors corresponding to the states above
        };

        ColorStateList newTintList = new ColorStateList(states, colors);

        btn.setBackgroundTintList(newTintList);
    }

    public static String formatDuration(int durationInMillis) {
        int seconds = (durationInMillis / 1000) % 60;
        int minutes = ((durationInMillis / (1000 * 60)) % 60);

        return String.format(Locale.getDefault(), "%02d:%02d",minutes, seconds);
    }

    private void initPlaybackService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void displayPremiumOption() {
        Toast.makeText(AudioPlayer.this, "Get premium for offline streaming", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, PremiumRegister.class);
        startActivity(i);
    }

    private void playAudio(String link) {
        mediaPlayerService.playAudio(link);
    }

    // Method to check if the file is already downloaded
    private boolean isAudioFileDownloaded(String fileName) {
        if (!permission()) {
            requestPermission();
            return false;
        }
        else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            if (file.exists()) {
                localFilePath = file.getAbsolutePath();
                return true;
            }
        }
        return false;
    }

    private boolean permission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        else
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int result = ContextCompat.checkSelfPermission(AudioPlayer.this, permission );
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        else
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(AudioPlayer.this, new String[]{permission}, 123);
    }

    private void downloadAudio(String url, String fileName) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);

        // Set the destination directory and filename for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Optional: Set other parameters for the download request
        // For example, you can set the notification visibility
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Enqueue the download request
        downloadManager.enqueue(request);

        Toast.makeText(this, "Downloading "+fileName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayerService.getMediaPlayer() != null && mediaPlayerService.isPlaying()) {
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayerService.seek(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                seekBar.setMax(mediaPlayerService.getMediaPlayer().getDuration());

                int currentPosition = mediaPlayerService.getMediaPlayer().getCurrentPosition();
                seekBar.setProgress(currentPosition);

                int durationInMillis = mediaPlayerService.getMediaPlayer().getDuration();
                String formattedDuration = formatDuration(durationInMillis);

                trackDuration.setText(formattedDuration);

                String formatCurrPos = formatDuration(currentPosition);
                currentTime.setText(formatCurrPos);
            }

            checkIfFavourited();

            // Repeat the update after a short delay
            seekBarHandler.postDelayed(this, 100);
        }
    };

    public void back(View view) {
        finish();
    }
}