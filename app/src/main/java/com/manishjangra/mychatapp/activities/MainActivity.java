package com.manishjangra.mychatapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.databinding.ActivityMainBinding;
import com.manishjangra.mychatapp.databinding.CustomAppBarLayoutBinding;
import com.manishjangra.mychatapp.fragments.HomeFragment;
import com.manishjangra.mychatapp.fragments.MyAccountFragment;
import com.manishjangra.mychatapp.fragments.ConversationFragment;
import com.manishjangra.mychatapp.fragments.UsersFragment;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;
import com.manishjangra.mychatapp.viewmodel.UserViewModel;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        customAppBarLayoutBinding = CustomAppBarLayoutBinding.bind(findViewById(R.id.main_app_bar_layout));
//  --> wrong approach      customAppBarLayoutBinding = CustomAppBarLayoutBinding.inflate(getLayoutInflater());
//        setSupportActionBar(customAppBarLayoutBinding.customToolbar);
        setSupportActionBar(binding.customToolbar);

        binding.bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);

        preferenceManager = new PreferenceManager(getApplicationContext());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        loadUserDetailsFromPreferences();
        getToken();
        setListeners();
        defaultFragment();
    }

    private void loadUserDetailsFromPreferences(){
        String name = preferenceManager.getString(Constants.KEY_NAME);
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        String image = preferenceManager.getString(Constants.KEY_IMAGE);

        User user = new User(name, email, image);
        userViewModel.setUser(user);

    }

    private void setListeners(){

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home_menu){
                    fragLoader(new HomeFragment(), true, "Home");
                } else if (itemId == R.id.nav_updates_menu){
                    fragLoader(new UsersFragment(), true, "New Chat");
                } else {
                    fragLoader(new MyAccountFragment(), true, "My Account");
                }

                return true;
            }
        });

    }

    private void defaultFragment(){
        fragLoader(new HomeFragment(), false, "Home");
    }

    private void fragLoader(Fragment fragment, boolean flag, String title){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.replace(R.id.container_frame_layout, fragment);
        }else {
            fragmentTransaction.add(R.id.container_frame_layout, fragment);
        }
        fragmentTransaction.commit();
        setTitle(title);
    }


    @Override
    public void setTitle(CharSequence title){
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        showToast("Token Updated Successfully");
//                    }
//                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Unable to update token");
                    }
                });

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void hideAppBarAndNavigationBar() {
        binding.customAppBarLayout.setVisibility(View.GONE);
        binding.bottomNavigationView.setVisibility(View.GONE);
    }

    public void showAppBarAndNavigationBar() {
        binding.customAppBarLayout.setVisibility(View.VISIBLE);
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Check if the back stack is empty after the pop
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showAppBarAndNavigationBar();
        }
    }
}