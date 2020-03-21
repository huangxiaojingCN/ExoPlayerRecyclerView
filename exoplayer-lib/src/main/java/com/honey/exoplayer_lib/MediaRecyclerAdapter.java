package com.honey.exoplayer_lib;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import java.util.List;

public class MediaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RequestManager requestManager;

    public List<MediaObject> mediaObjects;

    public MediaRecyclerAdapter(List<MediaObject> mediaObjects, RequestManager requestManager) {
        this.mediaObjects = mediaObjects;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_view_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((PlayerViewHolder)holder).onBind(mediaObjects.get(position), this.requestManager);
    }

    @Override
    public int getItemCount() {
        return mediaObjects != null ? mediaObjects.size() : 0;
    }
}
