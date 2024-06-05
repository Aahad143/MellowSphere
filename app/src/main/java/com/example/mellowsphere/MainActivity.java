package com.example.mellowsphere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
;

import com.example.mellowsphere.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    List<AudioTrackModel> audioList = new ArrayList<>();
    List<AudioTrackModel> favouritesList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    ActivityMainBinding binding;
    FrameLayout frameLayout;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean isUserPremium;
    String ispremium;

    boolean stopthis = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.exists()) {
                            String isPremium = documentSnapshot.getString("isPremium");

                            if (isPremium.equals("f")) {
                                isUserPremium = false;
                                ispremium = "f";
                            }
                            else {
                                isUserPremium = true;
                                ispremium = "t";
                            }
                        }
                    }
                }
            });
        }

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            bottomNavFragmentReplacement(AllTabItemFragment.newInstance(isUserPremium));
        }

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);


        frameLayout = findViewById(R.id.frameLayout);

        drawerLayout = findViewById(R.id.drawerlayout);
        toolbar = findViewById(R.id.titlebar);
        navigationView = findViewById(R.id.sidebar_nav);

        Handler handler = new Handler();
        int delayMillis = 1000; // set your desired delay in milliseconds

        // Get the Drawable from the resource ID
        Drawable expectedDrawable = ContextCompat.getDrawable(this, R.drawable.custom_background_9);
        final Fragment[] currentFragment = {new Fragment()};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ispremium != null && stopthis) {
                    bottomNavFragmentReplacement(AllTabItemFragment.newInstance(isUserPremium));
                    stopthis = false;
                }

                Menu menu = navigationView.getMenu();
                Menu menu2 = binding.bottomNavigationView.getMenu();

                currentFragment[0] = getSupportFragmentManager().findFragmentById(R.id.frameLayout);

                if (isUserPremium && (drawerLayout.getBackground() != expectedDrawable) && currentFragment[0] instanceof AllTabItemFragment) {
                    drawerLayout.setBackgroundResource(R.drawable.custom_background_9);
                }

                if (user == null) {
                    MenuItem menuItem = menu.findItem(R.id.logout);
                    menuItem.setVisible(false);

                    MenuItem menuItem1 = menu2.findItem(R.id.favourites);
                    menuItem1.setVisible(false);
                }
                else {
                    MenuItem menuItem2 = menu.findItem(R.id.register);
                    menuItem2.setVisible(false);
                }

                if (isUserPremium) {
                    MenuItem menuItem = menu.findItem(R.id.packages);
                    menuItem.setVisible(false);
                }

                retrievFavouriteTracksFromAlbum();

                // Execute the code again after the specified delay
                handler.postDelayed(this, delayMillis);
            }
        };

        // Start executing the code with the initial delay
        handler.postDelayed(runnable, delayMillis);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // Set custom width and height for the icon
        int iconWidth = (int) getResources().getDimension(R.dimen.menu_icon_width);
        int iconHeight = (int) getResources().getDimension(R.dimen.menu_icon_height);

        toggle.getDrawerArrowDrawable().setBarLength(iconWidth);
        toggle.getDrawerArrowDrawable().setBarThickness(iconWidth / 8f);
        toggle.getDrawerArrowDrawable().setGapSize(iconHeight / 8f);

        toggle.getDrawerArrowDrawable().setColorFilter(getResources().getColor(R.color.menuiconColor), PorterDuff.Mode.SRC_ATOP);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.music) {
                switchtoFragment(drawerLayout, R.drawable.custom_background_1, toggle, R.color.menuiconColor, AllTabItemFragment.newInstance(isUserPremium), true);
            }
            else if (item.getItemId() == R.id.favourites) {
                switchtoFragment(drawerLayout, R.drawable.custom_background_4, toggle, R.color.favourites_icon_color, FavouritesFragment.newInstance(favouritesList, isUserPremium), false);
            }
            else if (item.getItemId() == R.id.profile) {
                switchtoFragment(drawerLayout, R.drawable.custom_background_5, toggle, R.color.menuiconColorProfile, new ProfileFragment(), false);
            }
            return true;
        });
        navigationView.bringToFront();
    }

    @Override
    public void onBackPressed() {
        // Close the drawer if it's open, otherwise, perform the default back button behavior
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Get the current fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);

            // Check if it's the AudioListViewFragment
            if (!(currentFragment instanceof AllTabItemFragment)) {

                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                // Switch to another fragment or perform other actions
                binding.bottomNavigationView.setSelectedItemId(R.id.music);
                switchtoFragment(drawerLayout, R.drawable.custom_background_1, toggle, R.color.menuiconColor, AllTabItemFragment.newInstance(isUserPremium), true);
            } else {
                // Handle the back press as usual
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemID = item.getItemId();

        if (itemID == R.id.logout) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        else if (itemID == R.id.register) {
            Intent i = new Intent(getApplicationContext(), Register.class);
            startActivity(i);
        }
        else if (itemID == R.id.premium) {
            if (user == null) {
                Toast.makeText(this, "Please create an account or log in", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
            else {
                Intent i = new Intent(this, PremiumRegister.class);
                startActivity(i);
            }
        }
        return true;
    }
    private void switchtoFragment(DrawerLayout drawerLayoutx, int backgroundID, ActionBarDrawerToggle toggle, int colorID, Fragment fragment, boolean home) {
        if (!home) {
            bottomNavFragmentReplacement(fragment);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawerLayoutx.setBackgroundResource(backgroundID);
                    toggle.getDrawerArrowDrawable().setColorFilter(getResources().getColor(colorID), PorterDuff.Mode.SRC_ATOP);
                }
            }, 00);
        }
        else {
            drawerLayoutx.setBackgroundResource(backgroundID);
            toggle.getDrawerArrowDrawable().setColorFilter(getResources().getColor(colorID), PorterDuff.Mode.SRC_ATOP);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomNavFragmentReplacement(fragment);
                }
            }, 00);
        }
    }
    private void bottomNavFragmentReplacement(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment with the new one
        fragmentTransaction.replace(R.id.frameLayout, fragment);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void retrievFavouriteTracksFromAlbum() {

        if (user != null) {
            db.collection("alltracks"). whereArrayContains("favouritedby", user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<AudioTrackModel> tempList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String albumName = document.getString("album");
                            String artistName = document.getString("artist");
                            String genre = document.getString("genre");
                            String title = document.getString("title");

                            // Convert Firestore document to AlbumModelForRecycler
                            AudioTrackModel audio = new AudioTrackModel(title, artistName, genre, albumName);

                            tempList.add(audio);

                        }
                        if (!(favouritesList == tempList)) {
                            favouritesList = tempList;
                        }
                    }
                }
            });
        }
    }

    public void traverseThroughCollection(String collection) {
        db.collection(collection.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // For each document, update the "favouritedby" field
                        String documentId = document.getId();
                        DocumentReference documentRef = db.collection(collection.toLowerCase()).document(documentId);

                        // Assuming you want to set "favouritedby" to some specific value
                        // You can modify this based on your requirements
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("favouritedby", null);

                        batch.update(documentRef, updates);
                    }

                    // Commit the batched write
                    batch.commit();
                }
            }
        });
    }
}

