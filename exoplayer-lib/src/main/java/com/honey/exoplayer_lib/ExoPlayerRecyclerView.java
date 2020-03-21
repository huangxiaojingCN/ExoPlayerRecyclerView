package com.honey.exoplayer_lib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExoPlayerRecyclerView extends RecyclerView {

    private static final boolean DEBUG = true;

    public static final String TAG = "ExoPlayerRecyclerView";

    private Context context;

    private int videoSurfaceDefaultHeight;

    private int screenDefaultHeight;

    private PlayerView videoSurfaceView;

    private SimpleExoPlayer videoPlayer;

    private View viewHolderParent;

    private List<MediaObject> mediaObjects = new ArrayList<>();

    private int playPosition;

    private boolean isVideoViewAdded;

    private ImageView ivPlayImg;

    private ImageView ivMediaCoverImg;

    private ProgressBar progressBar;

    private RequestManager requestManager;

    private FrameLayout flMediaContainer;

    private boolean isAutoPlay = true;

    private PlayOnClickListener mPlayOnClickListener;

    private ImageView lastPlayImg;

    private ProgressBar lastProgressBar;

    private ImageView lastMediaCoverImg;

    private boolean mExoPlayerFullscreen = false;

    private ImageView mFullScreenIcon;

    private boolean fullScreen;

    private FrameLayout flFullScreenContainer;

    private OnFullScreenListener onFullScreenListener;

    public void requestFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        mFullScreenIcon = videoSurfaceView.findViewById(R.id.iv_resize_img);
        if (fullScreen) {
            mFullScreenIcon.setVisibility(View.VISIBLE);
        } else {
            mFullScreenIcon.setVisibility(View.GONE);
        }
        mFullScreenIcon.setOnClickListener(fullScreenClickListener);
    }

    public void addFullScreenView(FrameLayout flFullScreenContainer) {
        this.flFullScreenContainer = flFullScreenContainer;
        ImageView ivBackImg = flFullScreenContainer.findViewById(R.id.iv_back);
        ivBackImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                exitFullScreen();
            }
        });
    }

    private View.OnClickListener fullScreenClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!mExoPlayerFullscreen) {
                if (onFullScreenListener != null) {
                    onFullScreenListener.onFullScreen(true);
                }
                enterFullScreen();
            } else {
                if (onFullScreenListener != null) {
                    onFullScreenListener.onFullScreen(false);
                }
                exitFullScreen();
            }
        }
    };

    private class PlayOnClickListener implements View.OnClickListener {

        private PlayerViewHolder holder;

        public PlayOnClickListener(PlayerViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View view) {
            flMediaContainer = holder.flMediaContainer;
            ivPlayImg = holder.ivPlayImg;
            viewHolderParent = holder.itemView;
            ivMediaCoverImg = holder.ivMediaCoverImg;
            requestManager = holder.requestManager;
            progressBar = holder.progressBar;

            if (lastPlayImg != null && lastPlayImg != ivPlayImg) {
                lastPlayImg.setVisibility(View.VISIBLE);
            }

            if (lastMediaCoverImg != null && lastMediaCoverImg != ivMediaCoverImg) {
                lastMediaCoverImg.setVisibility(View.VISIBLE);
            }

            if (lastProgressBar != null && lastProgressBar != progressBar) {
                lastProgressBar.setVisibility(View.GONE);
            }

            lastPlayImg = ivPlayImg;
            lastProgressBar = progressBar;
            lastMediaCoverImg = ivMediaCoverImg;

            ivPlayImg.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            playVideo(holder.getAdapterPosition());
        }
    }

    private void enterFullScreen() {
        if (flFullScreenContainer != null) {
            ViewGroup parent = (ViewGroup) videoSurfaceView.getParent();
            if (parent == null) {
                return;
            }

            int index = parent.indexOfChild(videoSurfaceView);
            if (index >= 0) {
                parent.removeView(videoSurfaceView);
            }

            flFullScreenContainer.addView(videoSurfaceView, 0, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            flFullScreenContainer.setVisibility(View.VISIBLE);
            mExoPlayerFullscreen = true;
            videoSurfaceView.hideController();
            hideSystemUi();
            if (context instanceof Activity) {
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    private void exitFullScreen() {
        if (flFullScreenContainer != null) {
            flFullScreenContainer.removeView(videoSurfaceView);
            flFullScreenContainer.setVisibility(View.GONE);
            flMediaContainer.addView(videoSurfaceView);
            mExoPlayerFullscreen = false;
            videoSurfaceView.hideController();
            showSystemUi();
            if (context instanceof Activity) {
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public ExoPlayerRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        this.context = context;
        Display display
                = Objects.requireNonNull((WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE)))
                .getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;
        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter
                .Builder(this.context).build();
        AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        videoPlayer = ExoPlayerFactory.newSimpleInstance(this.context, trackSelector);
        videoSurfaceView.setUseController(true);
        videoSurfaceView.setControllerAutoShow(false);
        videoSurfaceView.setControllerHideOnTouch(true);
        videoSurfaceView.setPlayer(videoPlayer);

        addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


                // The RecyclerView is not currently scrolling.
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    // scroll end
                    if (!canScrollVertically(RecyclerView.VERTICAL)) {
                        scrollPlay(true);
                    } else {
                        scrollPlay(false);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {

            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                PlayerViewHolder holder = (PlayerViewHolder) view.getTag();
                ImageView ivPlayImg = holder.ivPlayImg;
                mPlayOnClickListener = new PlayOnClickListener(holder);
                ivPlayImg.setOnClickListener(mPlayOnClickListener);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view) && !mExoPlayerFullscreen) {
                    resetVideoView();
                }
            }
        });

        videoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        Log.d(TAG, "onPlayerStateChanged: Buffering video...");
                        if (progressBar != null) {
                            progressBar.setVisibility(View.VISIBLE);
                            videoSurfaceView.hideController();
                        }
                        break;
                    case ExoPlayer.STATE_ENDED:
                        Log.d(TAG, "onPlayerStateChanged: state ended...");
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (ivPlayImg != null) {
                            ivPlayImg.setVisibility(View.VISIBLE);
                        }

                        if (ivMediaCoverImg != null) {
                            ivMediaCoverImg.setVisibility(View.VISIBLE);
                        }
                        videoPlayer.seekTo(0);
                        break;
                    case ExoPlayer.STATE_READY:
                        Log.d(TAG, "onPlayerStateChanged: ready to play...");
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        
                        if (!isVideoViewAdded) {
                            addVideoView();
                        }
                        break;
                    default: break;
                }
            }
        });
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if (adapter != null) {
            if (!(adapter instanceof MediaRecyclerAdapter)) {
                throw new IllegalArgumentException("The RecyclerView Adapter must be instance of MediaRecyclerAdapter.");
            }
            mediaObjects = ((MediaRecyclerAdapter)adapter).mediaObjects;
        }
        super.setAdapter(adapter);
    }

    private void playVideo(int position) {
        if (position < 0 || position >= mediaObjects.size()) {
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "current play position: " + position);
        }

        if (videoPlayer.isPlaying()) {
            onPausePlayer();
        }

        videoSurfaceView.setVisibility(View.INVISIBLE);
        removeVideoView(videoSurfaceView);

        MediaObject mediaObject = mediaObjects.get(position);
        String url = mediaObject.getMediaUrl();

        if (url != null) {
            String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
            ProgressiveMediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.prepare(mediaSource, false, false);
        }
    }

    public void scrollPlay(boolean isEndOfList) {

        if (!isAutoPlay) {
            return;
        }

        int targetPosition;

        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager)
                    Objects.requireNonNull((getLayoutManager()))).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager)
                    Objects.requireNonNull(getLayoutManager())).findLastVisibleItemPosition();

            if ((endPosition - startPosition) > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }

        } else {
            targetPosition = mediaObjects.size() - 1;
        }

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        playPosition = targetPosition;
        // no video surface Attached
        if (videoSurfaceView == null) {
            return;
        }

        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager)
                Objects.requireNonNull(getLayoutManager())).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }

        PlayerViewHolder holder = (PlayerViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }

        flMediaContainer = holder.flMediaContainer;
        ivPlayImg = holder.ivPlayImg;
        viewHolderParent = holder.itemView;
        ivMediaCoverImg = holder.ivMediaCoverImg;
        requestManager = holder.requestManager;
        progressBar = holder.progressBar;

        if (lastPlayImg != null && lastPlayImg != ivPlayImg) {
            lastPlayImg.setVisibility(View.VISIBLE);
        }

        if (lastMediaCoverImg != null && lastMediaCoverImg != ivMediaCoverImg) {
            lastMediaCoverImg.setVisibility(View.VISIBLE);
        }

        if (lastProgressBar != null && lastProgressBar != progressBar) {
            lastProgressBar.setVisibility(View.GONE);
        }

        lastPlayImg = ivPlayImg;
        lastProgressBar = progressBar;
        lastMediaCoverImg = ivMediaCoverImg;

        ivPlayImg.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        videoSurfaceView.setPlayer(videoPlayer);

        MediaObject mediaObject = mediaObjects.get(currentPosition);
        String url = mediaObject.getMediaUrl();

        if (url != null) {
            String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
            ProgressiveMediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.prepare(mediaSource, false, false);
        }
    }

    public void setAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager)
                Objects.requireNonNull(getLayoutManager())).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    private void addVideoView() {
        flMediaContainer.addView(videoSurfaceView, 0);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setAlpha(1);
        videoSurfaceView.setVisibility(View.VISIBLE);
        ivMediaCoverImg.setVisibility(View.GONE);
        ivPlayImg.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void removeVideoView(PlayerView videoSurfaceView) {
        ViewGroup parent = (ViewGroup) videoSurfaceView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoSurfaceView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
        }
    }

    private void resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            ivPlayImg.setVisibility(View.VISIBLE);
            ivMediaCoverImg.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            ivPlayImg.setOnClickListener(null);
            mPlayOnClickListener = null;
        }
    }

    private void hideSystemUi() {
        videoSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void showSystemUi() {
        videoSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    public void releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }

        if (videoSurfaceView != null) {
            mFullScreenIcon.setOnClickListener(null);
            fullScreenClickListener = null;
        }

        viewHolderParent = null;
    }

    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
        }
    }

    public void play() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
    }

    public void setMediaObjects(List<MediaObject> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    public void setOnFullScreenListener(OnFullScreenListener l) {
        this.onFullScreenListener = l;
    }

    public interface OnFullScreenListener {

        void onFullScreen(boolean fullScreen);
    }
}
