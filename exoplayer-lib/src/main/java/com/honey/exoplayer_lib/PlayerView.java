package com.honey.exoplayer_lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerView extends FrameLayout {

    private Context context;

    private FrameLayout flFullScreenContainer;

    private ExoPlayerRecyclerView exoPlayerRecyclerView;

    private ExoPlayerRecyclerView.OnFullScreenListener fullScreenListener = new ExoPlayerRecyclerView.OnFullScreenListener() {

        @Override
        public void onFullScreen(boolean fullScreen) {
            if (fullScreen) {
                hideActionBar();
            } else {
                showActionBar();
            }
        }
    };


    public PlayerView(Context context) {
        this(context, null);
    }

    public PlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.exoplayer_view, this, true);
        exoPlayerRecyclerView = rootView.findViewById(R.id.exoplayer_recyclerview);
        flFullScreenContainer = rootView.findViewById(R.id.fl_fullscreen_container);

        exoPlayerRecyclerView.setOnFullScreenListener(fullScreenListener);
    }

    public void requestFullScreen(boolean fullScreen) {
        exoPlayerRecyclerView.requestFullScreen(fullScreen);
        exoPlayerRecyclerView.addFullScreenView(flFullScreenContainer);
    }

    public ExoPlayerRecyclerView getExoPlayerRecyclerView() {
        return exoPlayerRecyclerView;
    }

    public FrameLayout getFullScreenContainer() {
        return flFullScreenContainer;
    }

    public void setAutoPlay(boolean autoPlay) {
        exoPlayerRecyclerView.setAutoPlay(autoPlay);
    }

    private void hideActionBar() {
        if (context instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    private void showActionBar() {
        if (context instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    }
}
