package com.manishjangra.mychatapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.activities.BaseActivity;
import com.manishjangra.mychatapp.activities.ConversationActivity;
import com.manishjangra.mychatapp.adapters.RecentConversationAdapter;
import com.manishjangra.mychatapp.databinding.FragmentHomeBinding;
import com.manishjangra.mychatapp.listeners.ConversationListener;
import com.manishjangra.mychatapp.model.ConversationMessage;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    private PreferenceManager preferenceManager;
    private ArrayList<ConversationMessage> conversationMessageArrayList;
    private RecentConversationAdapter recentConversationAdapter;
    private FirebaseFirestore database;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        preferenceManager = new PreferenceManager(requireContext());
        init();
        setListeners();
        listenConversation();
        // Inflate the layout for this fragment
        return view;
    }

    private void init(){
        conversationMessageArrayList = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversationMessageArrayList, this::onConversationClicked );
        binding.recentConversationsRecyclerView.setAdapter(recentConversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void onConversationClicked(User user) {
        Intent intent = new Intent(getContext(), ConversationActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }


    private void setListeners(){


    }

    private void listenConversation(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ConversationMessage conversationMessage = new ConversationMessage();
                    conversationMessage.senderId = senderId;
                    conversationMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        conversationMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        conversationMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        conversationMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else{
                        conversationMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        conversationMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        conversationMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    conversationMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    conversationMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversationMessageArrayList.add(conversationMessage);

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i<conversationMessageArrayList.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversationMessageArrayList.get(i).senderId.equals(senderId) && conversationMessageArrayList.get(i).receiverId.equals(receiverId)){
                            conversationMessageArrayList.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversationMessageArrayList.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }

                    }
                    
                }
            }
            conversationMessageArrayList.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationAdapter.notifyDataSetChanged();
            binding.recentConversationsRecyclerView.smoothScrollToPosition(0);
            binding.recentConversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.recentConversationsProgressBar.setVisibility(View.GONE);
        }

    };
}