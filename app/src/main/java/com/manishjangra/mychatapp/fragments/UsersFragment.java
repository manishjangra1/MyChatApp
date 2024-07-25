package com.manishjangra.mychatapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.activities.BaseActivity;
import com.manishjangra.mychatapp.activities.ConversationActivity;
import com.manishjangra.mychatapp.activities.MainActivity;
import com.manishjangra.mychatapp.adapters.UsersAdapter;
import com.manishjangra.mychatapp.databinding.ActivityMainBinding;
import com.manishjangra.mychatapp.databinding.FragmentUsersBinding;
import com.manishjangra.mychatapp.listeners.UserListener;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;

import java.util.ArrayList;

public class UsersFragment extends Fragment implements UserListener{
    private FragmentUsersBinding binding;
    private PreferenceManager preferenceManager;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(requireContext());
        getUsers();
        setListeners();
        return binding.getRoot();
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_users, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void setListeners(){

    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        loading(false);
                        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                        if (task.isSuccessful() && task.getResult() != null) {
                            ArrayList<User> userArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                    continue;
                                }
                                User user = new User();
                                user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                                user.setEmail(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                                user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                                user.setToken(queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                                user.setId(queryDocumentSnapshot.getId());
                                userArrayList.add(user);
                            }
                            if (userArrayList.size() > 0){
                                UsersAdapter usersAdapter = new UsersAdapter(userArrayList, UsersFragment.this);
                                binding.usersListRecyclerView.setAdapter(usersAdapter);
                                binding.usersListRecyclerView.setVisibility(View.VISIBLE);
                            }
                            else{
                                showErrorMessage();
                            }

                        }
                        else {
                            showErrorMessage();
                        }
                    }
                });
    }

    private void loading(boolean isLoading){
        if (isLoading){
            binding.usersLoadingProgressBar.setVisibility(View.VISIBLE);
        }else {
            binding.usersLoadingProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showErrorMessage(){
        binding.textErrorMessageUsersNotAvailable.setText(String.format("%s", "No users available"));
        binding.textErrorMessageUsersNotAvailable.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {

        Intent intent = new Intent(requireActivity(), ConversationActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
//        FragmentManager fragmentManager = getParentFragmentManager();
//
//        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation_view);
//        if (bottomNav != null){
//            bottomNav.setVisibility(View.GONE);
//        }

//        ((MainActivity) requireActivity()).hideAppBarAndNavigationBar();
//        fragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
//                .replace(R.id.container_frame_layout, ConversationActivity.newInstance(user))
//                .addToBackStack(null)
//                .commit();
    }
}