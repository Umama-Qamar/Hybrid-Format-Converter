package com.example.hybridformatconverter.data;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class HistoryRepository {

    private final HistoryDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistoryRepository(Context context) {
        dao = AppDatabase.getInstance(context).historyDao();
    }

    public interface HistoryCallback {
        void onResult(List<HistoryItem> items);
    }

    public interface SimpleCallback {
        void onDone(boolean success);
    }

    public void addDownload(String fileName, String filePath, String fileType) {
        executor.execute(() -> {
            HistoryItem item = new HistoryItem();
            item.fileName = fileName;
            item.filePath = filePath;
            item.fileType = fileType;
            item.downloadedAt = System.currentTimeMillis();

            dao.insert(item);
            dao.trimToLastFive();
        });
    }

    public void getHistory(HistoryCallback callback) {
        executor.execute(() -> {
            List<HistoryItem> items = dao.getAll();
            callback.onResult(items);
        });
    }

    public void renameItem(int id, String newName, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                dao.renameItem(id, newName);
                if (callback != null) callback.onDone(true);
            } catch (Exception e) {
                if (callback != null) callback.onDone(false);
            }
        });
    }

    public void deleteItem(int id, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                dao.deleteItem(id);
                if (callback != null) callback.onDone(true);
            } catch (Exception e) {
                if (callback != null) callback.onDone(false);
            }
        });
    }
}