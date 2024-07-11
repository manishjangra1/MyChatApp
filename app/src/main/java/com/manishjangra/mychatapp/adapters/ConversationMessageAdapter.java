package com.manishjangra.mychatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manishjangra.mychatapp.databinding.ItemContainerReceivedMessageBinding;
import com.manishjangra.mychatapp.databinding.ItemContainerSentMessageBinding;
import com.manishjangra.mychatapp.model.ConversationMessage;

import java.util.ArrayList;

public class ConversationMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ConversationMessage> conversationMessageArrayList;
    private final Bitmap receiverProfileImage;
    private String senderId;

    private final int VIEW_TYPE_SENT = 1;
    private final int VIEW_TYPE_RECEIVED =2;


    public ConversationMessageAdapter(ArrayList<ConversationMessage> conversationMessageArrayList, Bitmap receiverProfileImage, String senderId) {
        this.conversationMessageArrayList = conversationMessageArrayList;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext())
                            , parent, false));
        }
        else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext())
            ,parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(conversationMessageArrayList.get(position));
        }
        else {
            ((ReceivedMessageViewHolder)holder).setData(conversationMessageArrayList.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return conversationMessageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (conversationMessageArrayList.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ConversationMessage conversationMessage){
            binding.sentMessage.setText(conversationMessage.message);
            binding.sentMessageDateTime.setText(conversationMessage.dateTime);


        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        void setData(ConversationMessage conversationMessage, Bitmap receiverProfileImage){
            binding.receivedMessage.setText(conversationMessage.message);
            binding.receivedMessageDateTime.setText(conversationMessage.dateTime);
            binding.chatUserImageProfile.setImageBitmap(receiverProfileImage);

        }
    }
}
