package com.manishjangra.mychatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.manishjangra.mychatapp.activities.SignInActivity;
import com.manishjangra.mychatapp.databinding.FragmentMyAccountBinding;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;
import com.manishjangra.mychatapp.viewmodel.UserViewModel;

import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

public class MyAccountFragment extends Fragment {
    private FragmentMyAccountBinding binding;
    private PreferenceManager preferenceManager;
    private UserViewModel userViewModel;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyAccountBinding.inflate(inflater, container, false);

        View view = binding.getRoot();
//        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        // Inflate the layout for this fragment
        loadUserDetails();
        setListeners();
        return view;
    }


    private void loadUserDetails(){
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {

            binding.myAccountTextName.setText(preferenceManager.getString(Constants.KEY_NAME));
            byte[] bytes = android.util.Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.myAccountImageView.setImageBitmap(bitmap);
            binding.myAccountTextEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

            Glide.with(this)
                    .load(bitmap)
                    .into(binding.myAccountImageView);
        });

    }

    private void signOut(){
        showToast("Signing Out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        preferenceManager.clear();
                        startActivity(new Intent(requireActivity().getApplicationContext(), SignInActivity.class));
                        requireActivity().finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Unable to Sign Out");
                    }
                });


    }

    private void setListeners(){

        binding.signOutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(requireActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle("My Profile");
    }
}