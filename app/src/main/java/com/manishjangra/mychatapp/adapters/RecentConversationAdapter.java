package com.manishjangra.mychatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manishjangra.mychatapp.databinding.ItemContainerRecentConversationUserBinding;
import com.manishjangra.mychatapp.listeners.ConversationListener;
import com.manishjangra.mychatapp.model.ConversationMessage;
import com.manishjangra.mychatapp.model.User;
import com.manishjangra.mychatapp.utilities.Constants;

import java.util.ArrayList;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>{
    private final ArrayList<ConversationMessage> recentConversationMessageArrayList;
    private final ConversationListener conversationListener;

    public RecentConversationAdapter(ArrayList<ConversationMessage> recentConversationMessageArrayList, ConversationListener conversationListener) {
        this.recentConversationMessageArrayList = recentConversationMessageArrayList;
        this.conversationListener = conversationListener;
    }


    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(recentConversationMessageArrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return recentConversationMessageArrayList.size();
    }



    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversationUserBinding binding;

        ConversationViewHolder(ItemContainerRecentConversationUserBinding itemContainerRecentConversationUserBinding){
            super(itemContainerRecentConversationUserBinding.getRoot());
            binding = itemContainerRecentConversationUserBinding;

        }

        void setData(ConversationMessage conversationMessage){
            binding.userProfileImageView.setImageBitmap(getConversationImage(conversationMessage.conversationImage));
            binding.userNameText.setText(conversationMessage.conversationName);
            binding.userRecentMessageText.setText(conversationMessage.message);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = new User();
                    user.setId(conversationMessage.conversationId);
                    user.setName(conversationMessage.conversationName);
                    user.setImage(conversationMessage.conversationImage);
                    conversationListener.onConversationClicked(user);
                }
            });

        }

    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
