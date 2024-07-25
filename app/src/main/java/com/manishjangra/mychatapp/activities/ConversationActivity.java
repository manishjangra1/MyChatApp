package com.manishjangra.mychatapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.manishjangra.mychatapp.R;
import com.manishjangra.mychatapp.adapters.ConversationMessageAdapter;
import com.manishjangra.mychatapp.databinding.ActivityConversationBinding;
import com.manishjangra.mychatapp.model.ConversationMessage;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.network.ApiClient;
import com.manishjangra.mychatapp.network.ApiService;
import com.manishjangra.mychatapp.utilities.Constants;
import com.manishjangra.mychatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationActivity extends BaseActivity {
    ActivityConversationBinding binding;

    private User receiverUser;
    //private View decorView;
    private ArrayList<ConversationMessage> conversationMessageArrayList;
    private ConversationMessageAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    public ConversationActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conversation);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceiverUserDetails();
        setListeners();
        init();
        listenMessage();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        conversationMessageArrayList = new ArrayList<>();
        adapter = new ConversationMessageAdapter(conversationMessageArrayList, getBitmapFromEncodedString(receiverUser.getImage()), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.conversationRecyclerView.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        String messageText = binding.inputMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            // You can show a toast message or just return
            // Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        if (conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(message);
        binding.inputMessage.setText(null);

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.getId())
                .addSnapshotListener(ConversationActivity.this, (value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
            }
            if (isReceiverAvailable){
                binding.receiverUserAvailability.setVisibility(View.VISIBLE);
            }else{
                binding.receiverUserAvailability.setVisibility(View.GONE);
            }
        });
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
        if (conversationId == null){
            checkForConversation();
        }
    };

    private void loadReceiverUserDetails(){
        receiverUser = (User)getIntent().getSerializableExtra(Constants.KEY_USER);
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

    private void addConversation(HashMap<String, Object> conversations){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversations)
                .addOnSuccessListener(
                        documentReference -> conversationId = documentReference.getId()
                );
    }

    private void updateConversation(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversation(){
        if (conversationMessageArrayList.size() != 0){
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.getId() );
        }
        checkForConversationRemotely(receiverUser.getId(), preferenceManager.getString(Constants.KEY_USER_ID));
    }

    private void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}