package com.atul.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.atul.musicplayer.activities.PlayerDialog;
import com.atul.musicplayer.activities.QueueDialog;
import com.atul.musicplayer.adapter.MainPagerAdapter;
import com.atul.musicplayer.helper.MusicLibraryHelper;
import com.atul.musicplayer.helper.ThemeHelper;
import com.atul.musicplayer.listener.MusicSelectListener;
import com.atul.musicplayer.model.Music;
import com.atul.musicplayer.player.PlayerBuilder;
import com.atul.musicplayer.player.PlayerListener;
import com.atul.musicplayer.player.PlayerManager;
import com.atul.musicplayer.viewmodel.MainViewModel;
import com.atul.musicplayer.viewmodel.MainViewModelFactory;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements MusicSelectListener, PlayerListener, View.OnClickListener {

    private RelativeLayout playerView;
    private ImageView albumArt;
    private TextView songName;
    private TextView songDetails;
    private ImageButton play_pause;
    private LinearProgressIndicator progressIndicator;
    private PlayerDialog playerDialog;
    private QueueDialog queueDialog;

    private PlayerBuilder playerBuilder;
    private PlayerManager playerManager;
    private boolean albumState;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeHelper.getTheme(MPPreferences.getTheme(getApplicationContext())));
        AppCompatDelegate.setDefaultNightMode(MPPreferences.getThemeMode(getApplicationContext()));
        setContentView(R.layout.activity_main);
        MPConstants.musicSelectListener = this;

        viewModel = new ViewModelProvider(this, new MainViewModelFactory()).get(MainViewModel.class);
        fetchMusicList();

        if (hasReadStoragePermission(MainActivity.this))
            setUpUiElements();
        else
            manageStoragePermission(MainActivity.this);

        albumState = MPPreferences.getAlbumRequest(this);
        MPConstants.musicSelectListener = this;

        MaterialCardView playerLayout = findViewById(R.id.player_layout);
        albumArt = findViewById(R.id.albumArt);
        progressIndicator = findViewById(R.id.song_progress);
        playerView = findViewById(R.id.player_view);
        songName = findViewById(R.id.song_title);
        songDetails = findViewById(R.id.song_details);
        play_pause = findViewById(R.id.control_play_pause);
        ImageButton queue = findViewById(R.id.control_queue);

        play_pause.setOnClickListener(this);
        playerLayout.setOnClickListener(this);
        queue.setOnClickListener(this);
    }

    private void setPlayerView() {
        if (playerManager != null && playerManager.isPlaying()) {
            playerView.setVisibility(View.VISIBLE);
            onMusicSet(playerManager.getCurrentMusic());
        }
    }

    private void fetchMusicList() {
        new Handler().post(() -> {
            List<Music> musicList = MusicLibraryHelper.fetchMusicLibrary(MainActivity.this);
            viewModel.setSongsList(musicList);
        });
    }

    public void setUpUiElements() {
        playerBuilder = new PlayerBuilder(MainActivity.this, this);
        MainPagerAdapter sectionsPagerAdapter = new MainPagerAdapter(
                getSupportFragmentManager(), this);

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        for (int i = 0; i < tabs.getTabCount(); i++) {
            TabLayout.Tab tab = tabs.getTabAt(i);
            if (tab != null) {
                tab.setIcon(MPConstants.TAB_ICONS[i]);
            }
        }
    }

    public void manageStoragePermission(Activity context) {
        if (!hasReadStoragePermission(context)) {
            // required a dialog?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Requesting permission")
                        .setMessage("Enable storage permission for accessing the media files.")
                        .setPositiveButton("Accept", (dialog, which) -> askReadStoragePermission(context)).show();
            } else
                askReadStoragePermission(context);
        }
    }

    public boolean hasReadStoragePermission(Activity context) {
        return (
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        );
    }

    public void askReadStoragePermission(Activity context) {
        ActivityCompat.requestPermissions(
                context,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MPConstants.PERMISSION_READ_STORAGE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ThemeHelper.applySettings(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // TODO:: Fix this, the ideal behaviour is that when the app closes
        //      the service is stopped from the foreground, but still lives
        //      and on coming back to app, it works.

        // TODO:: Current error is that the service unbinds, when we start the app
        //       from notification or by launcher it works ok, but after selecting the
        //      theme(changing the theme) or clicking the navigation button on toolbar
        //      the service dies too, sometimes it dies also by clicking on the
        //      notification.
//        playerBuilder.unBindService();

        if (playerDialog != null)
            playerDialog.dismiss();

        if (queueDialog != null)
            queueDialog.dismiss();
    }

    @Override
    public void playQueue(List<Music> musicList, boolean shuffle) {
        if (shuffle) {
            Collections.shuffle(musicList);
        }

        if (musicList.size() > 0) {
            playerManager.setMusicList(musicList);
            setPlayerView();
        }
    }

    @Override
    public void addToQueue(List<Music> musicList) {
        if (musicList.size() > 0) {
            if (playerManager != null && playerManager.isPlaying())
                playerManager.addMusicQueue(musicList);
            else if (playerManager != null)
                playerManager.setMusicList(musicList);

            setPlayerView();
        }
    }

    @Override
    public void onPrepared() {
        playerManager = playerBuilder.getPlayerManager();
        setPlayerView();
    }

    @Override
    public void onStateChanged(int state) {
        if (state == State.PLAYING)
            play_pause.setImageResource(R.drawable.ic_controls_pause);
        else
            play_pause.setImageResource(R.drawable.ic_controls_play);
    }

    @Override
    public void onPositionChanged(int position) {
        progressIndicator.setProgress(position);
    }

    @Override
    public void onMusicSet(Music music) {
        songName.setText(music.title);
        songDetails.setText(
                String.format(Locale.getDefault(), "%s • %s",
                        music.artist, music.album));
        playerView.setVisibility(View.VISIBLE);

        if (albumState)
            Glide.with(getApplicationContext())
                    .load(music.albumArt)
                    .centerCrop()
                    .into(albumArt);

        if (playerManager != null && playerManager.isPlaying())
            play_pause.setImageResource(R.drawable.ic_controls_pause);
        else
            play_pause.setImageResource(R.drawable.ic_controls_play);
    }

    @Override
    public void onPlaybackCompleted() {
    }

    @Override
    public void onRelease() {
        playerView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.control_play_pause)
            playerManager.playPause();

        else if (id == R.id.control_queue)
            setUpQueueDialog();

        else if (id == R.id.player_layout)
            setUpPlayerDialog();
    }

    private void setUpPlayerDialog() {
        playerDialog = new PlayerDialog(this, playerManager);
        playerDialog.show();
    }

    private void setUpQueueDialog() {
        queueDialog = new QueueDialog(this, playerManager.getPlayerQueue());
        queueDialog.show();
    }
}