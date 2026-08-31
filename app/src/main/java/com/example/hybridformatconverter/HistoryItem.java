package com.example.hybridformatconverter.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "history_items")
public class HistoryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String fileName;

    @NonNull
    public String filePath;

    public String fileType;

    public long downloadedAt;
}