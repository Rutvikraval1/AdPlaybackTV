package com.example.playback_tv.tv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.model.FileItem;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private List<FileItem> fileItems;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(FileItem fileItem);
    }

    public FileAdapter(List<FileItem> fileItems, OnFileClickListener listener) {
        this.fileItems = fileItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileItems.get(position);
        holder.bind(fileItem, listener);
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameText;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.file_name_text);
        }

        public void bind(FileItem fileItem, OnFileClickListener listener) {
            fileNameText.setText(fileItem.getDisplayName());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFileClick(fileItem);
                }
            });
            
            // Set focus handling for TV
            itemView.setFocusable(true);
            itemView.setClickable(true);
        }
    }
}