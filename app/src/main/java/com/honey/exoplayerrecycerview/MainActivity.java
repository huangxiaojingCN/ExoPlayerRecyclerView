package com.honey.exoplayerrecycerview;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.honey.exoplayer_lib.ExoPlayerRecyclerView;
import com.honey.exoplayer_lib.MediaObject;
import com.honey.exoplayer_lib.MediaRecyclerAdapter;
import com.honey.exoplayer_lib.ExoPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExoPlayerRecyclerView mRecyclerView;

    private List<MediaObject> mediaObjects = new ArrayList();

    private ExoPlayerView playerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initData();

        playerView = findViewById(R.id.player_view);
        playerView.requestFullScreen(true);
        playerView.setAutoPlay(true);

        mRecyclerView = playerView.getExoPlayerRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        MediaRecyclerAdapter adapter = new MediaRecyclerAdapter(mediaObjects, initGlide());
        mRecyclerView.setAdapter(adapter);
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions();
        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            MediaObject mediaObject = new MediaObject();
            if (i == 0 || i == 2 || i == 5) {
                mediaObject.setMediaUrl("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3");
                mediaObject.setMediaCoverImgUrl("https://images.amcnetworks.com/amc.com/wp-content/uploads/2015/04/cast_bb_700x1000_walter-white-lg.jpg");
            }

            if (i == 1 || i == 3 || i == 7 || i == 10) {
                mediaObject.setMediaUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                mediaObject.setMediaCoverImgUrl("https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Jesse_Pinkman2.jpg/220px-Jesse_Pinkman2.jpg");
            }

            if (i == 4 || i == 6 || i == 9) {
                mediaObject.setMediaUrl("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3");
                mediaObject.setMediaCoverImgUrl("https://s-i.huffpost.com/gen/1317262/images/o-ANNA-GUNN-facebook.jpg");
            }

            mediaObjects.add(mediaObject);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.onPausePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        if (playerView != null) {
            playerView.releasePlayer();
        }
        super.onDestroy();
    }
}
