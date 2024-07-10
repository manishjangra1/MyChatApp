package com.manishjangra.mychatapp.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.databinding.FragmentHomeBinding;

import java.util.Objects;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();
        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onResume() {

        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Home");
    }


    private void setListeners(){


    }
}