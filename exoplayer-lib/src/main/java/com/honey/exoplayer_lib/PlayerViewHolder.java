package com.honey.exoplayer_lib;

import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

public class PlayerViewHolder extends RecyclerView.ViewHolder {

    public FrameLayout flMediaContainer;

    public ImageView ivMediaCoverImg;

    public ImageView ivPlayImg;

    public View parent;

    public ProgressBar progressBar;

    public RequestManager requestManager;

    public PlayerViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        flMediaContainer = itemView.findViewById(R.id.fl_media_container);
        ivPlayImg = itemView.findViewById(R.id.iv_play_img);
        ivMediaCoverImg = itemView.findViewById(R.id.iv_media_coverImg);
        progressBar = itemView.findViewById(R.id.pb_progress_bar);
    }

    void onBind(MediaObject mediaObject, RequestManager requestManager) {
        this.requestManager = requestManager;
        parent.setTag(this);
        String mediaCoverImgUrl = mediaObject.getMediaCoverImgUrl();
        if (mediaCoverImgUrl != null) {
            requestManager
                    .load(Uri.parse(mediaCoverImgUrl))
                    .into(ivMediaCoverImg);
        }
    }
}
