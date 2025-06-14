package org.tvheadend.tvhguide;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class VideoPlayerActivity extends Activity {
    
    private PlayerView playerView;
    private ExoPlayer player;
    private String streamUrl;
    private String channelName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Remove title bar and make fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Force landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        setContentView(R.layout.activity_video_player);
        
        // Get stream URL from intent
        Intent intent = getIntent();
        streamUrl = intent.getStringExtra("stream_url");
        channelName = intent.getStringExtra("channel_name");
        
        if (streamUrl == null || streamUrl.isEmpty()) {
            Toast.makeText(this, "No stream URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        playerView = findViewById(R.id.player_view);
        initializePlayer();
    }
    
    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            
            // Set up player listener
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        finish();
                    }
                }
                
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(VideoPlayerActivity.this, 
                        "Playback error: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            
            // Create media item and start playback
            MediaItem mediaItem = MediaItem.fromUri(streamUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
    
    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
    
    @Override
    public void onBackPressed() {
        releasePlayer();
        super.onBackPressed();
    }
}
