package com.manishjangra.mychatapp.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.adapters.ConversationMessageAdapter;
import com.manishjangra.mychatapp.databinding.FragmentConversationBinding;
import com.manishjangra.mychatapp.model.ConversationMessage;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ConversationFragment extends Fragment {
    FragmentConversationBinding binding;
    private User receiverUser;
    private View decorView;
    private ArrayList<ConversationMessage> conversationMessageArrayList;
    private ConversationMessageAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    public ConversationFragment() {
        // Required empty public constructor
    }


    // Factory method to create new instance of ConversationFragment with arguments used with parcelable interface
    public static ConversationFragment newInstance(User user) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConversationBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment


        // Use getArguments() to retrieve user details working with parcelable interface
        if (getArguments() != null) {
            receiverUser = getArguments().getParcelable(Constants.KEY_USER);
            if (receiverUser != null) {
                // Load receiver user details into UI or perform other operations
                loadReceiverUserDetails();

            }
        }
        //Hiding System UI
        decorView = requireActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        setListeners();
        init();
        listenMessage();
//        return inflater.inflate(R.layout.fragment_conversation, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                }

                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                if (isEnabled()){
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void init(){
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        conversationMessageArrayList = new ArrayList<>();
        adapter = new ConversationMessageAdapter(conversationMessageArrayList, getBitmapFromEncodedString(receiverUser.getImage()), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.conversationRecyclerView.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(message);
        binding.inputMessage.setText(null);

    }

    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if (error != null){
            return;
        }
        if (value != null){
            int count = conversationMessageArrayList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ConversationMessage conversationMessage = new ConversationMessage();
                    conversationMessage.senderId =documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    conversationMessage.receiverId =documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    conversationMessage.message =documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    conversationMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    conversationMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversationMessageArrayList.add(conversationMessage);
                }
            }
            conversationMessageArrayList.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                adapter.notifyDataSetChanged();
                binding.conversationRecyclerView.smoothScrollToPosition(conversationMessageArrayList.size());
            }
            else {
                adapter.notifyItemRangeInserted(conversationMessageArrayList.size(), conversationMessageArrayList.size());
                binding.conversationRecyclerView.smoothScrollToPosition(conversationMessageArrayList.size() - 1);
            }
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.conversationPageProgressBar.setVisibility(View.GONE);
    };

    private void loadReceiverUserDetails(){
        binding.chatUserNameText.setText(receiverUser.getName());
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setListeners(){
        binding.sendMessageButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(date);
    }

}