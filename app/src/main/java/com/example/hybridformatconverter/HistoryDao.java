package com.example.hybridformatconverter.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HistoryDao {

    @Insert
    void insert(HistoryItem item);

    @Query("SELECT * FROM history_items ORDER BY downloadedAt DESC")
    List<HistoryItem> getAll();

    @Query("DELETE FROM history_items WHERE id NOT IN " +
            "(SELECT id FROM history_items ORDER BY downloadedAt DESC LIMIT 5)")
    void trimToLastFive();

    @Query("UPDATE history_items SET fileName = :newName WHERE id = :id")
    void renameItem(int id, String newName);

    @Query("DELETE FROM history_items WHERE id = :id")
    void deleteItem(int id);
}