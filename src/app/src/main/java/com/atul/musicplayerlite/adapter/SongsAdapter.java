package com.atul.musicplayerlite.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atul.musicplayerlite.R;
import com.atul.musicplayerlite.listener.SongSelectListener;
import com.atul.musicplayerlite.model.Music;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

    private final List<Music> musicList;
    public SongSelectListener listener;

    public SongsAdapter(SongSelectListener listener, List<Music> musics){
        this.listener = listener;
        this.musicList = musics;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_songs, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.songName.setText(musicList.get(position).displayName);
        holder.albumName.setText(musicList.get(position).album);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView songName;
        private final TextView albumName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.song_name);
            albumName = itemView.findViewById(R.id.song_album);

            itemView.findViewById(R.id.root_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.playQueue(musicList.subList(getAdapterPosition(), musicList.size()));
                }
            });
        }
    }
}