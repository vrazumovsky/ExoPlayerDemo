package razumovsky.ru.exoplayerdemo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

public class MainActivity extends AppCompatActivity {

    private static final Uri PLAYLIST_URI = Uri.parse("http://testlivestream.rfn.ru/live/smil:m24.smil/chunklist_b1600000.m3u8");

    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private SimpleExoPlayerView simpleExoPlayerView;

    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.requestFocus();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player == null) {
            initPlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }


    private void initPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(new Handler(), videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl());
        simpleExoPlayerView.setPlayer(player);
        player.setPlayWhenReady(true);

        DataSource.Factory mediaDataSourceFactory =
                ((DemoApplication) getApplication()).buildDataSourceFactory(BANDWIDTH_METER);

        MediaSource mediaSource =
                new HlsMediaSource(PLAYLIST_URI, mediaDataSourceFactory, new Handler(), null);

        player.prepare(mediaSource);

    }


    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }
}
