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
import com.manishjangra.mychatapp.fragments.UsersFragment;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;
import com.manishjangra.mychatapp.viewmodel.UserViewModel;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                    fragLoader(new HomeFragment(), true);
                } else if (itemId == R.id.nav_updates_menu){
                    fragLoader(new UsersFragment(), true);
                } else {
                    fragLoader(new MyAccountFragment(), true);
                }

                return true;
            }
        });

    }

    private void defaultFragment(){
        fragLoader(new HomeFragment(), false);
    }

    private void fragLoader(Fragment fragment, boolean flag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.replace(R.id.container_frame_layout, fragment);
        }else {
            fragmentTransaction.add(R.id.container_frame_layout, fragment);
        }
        fragmentTransaction.commit();
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

}