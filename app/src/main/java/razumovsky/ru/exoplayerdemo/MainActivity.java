package razumovsky.ru.exoplayerdemo;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String INFO_TAG = "#EXTINF";


    private static final Uri PLAYLIST_URI = Uri.parse("http://iptvsensei.ru/wp-content/uploads/2015/02/Sportivnye-kanaly.m3u");

    @Override
    public boolean releaseInstance() {
        return super.releaseInstance();
    }

    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private SimpleExoPlayerView simpleExoPlayerView;

    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;

    private Handler handler = new Handler();

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

        new PlaylistLoadTask().execute("");
    }

    private final class PlaylistLoadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            DataSource.Factory mediaDataSourceFactory =
                    ((DemoApplication) getApplication()).buildDataSourceFactory(BANDWIDTH_METER);

            MediaSource mediaSource = customParse(mediaDataSourceFactory);

            player.prepare(mediaSource);

            return null;
        }

        private MediaSource customParse(DataSource.Factory mediaDataSourceFactory) {
            try {
                List<String> channels = new ArrayList<>();

                URL url = new URL(PLAYLIST_URI.toString());
                InputStream inputStream = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        // Do nothing.
                    } else if (line.startsWith(INFO_TAG)) {
                        do {
                            line = reader.readLine();
                        } while (line.isEmpty());
                        if (line.startsWith("http") && line.endsWith(".m3u8")) {
                            channels.add(line);
                        }
                    }
                }
                return createPlaylistMediaSource(channels, mediaDataSourceFactory);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }


    private MediaSource createPlaylistMediaSource(List<String> channels, DataSource.Factory mediaDataSourceFactory) {
        MediaSource[] mediaSources = new MediaSource[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            String url = channels.get(i);
            MediaSource source = new HlsMediaSource(
                    Uri.parse(url), mediaDataSourceFactory, handler, null);
            mediaSources[i] = source;
        }
        return new ConcatenatingMediaSource(mediaSources);
    }

        private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }
}
