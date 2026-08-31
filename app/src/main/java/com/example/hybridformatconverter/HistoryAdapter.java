package com.example.hybridformatconverter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hybridformatconverter.data.HistoryItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> items = new ArrayList<>();
    private ActionListener actionListener;
    public interface ActionListener {
        void onItemClick(HistoryItem item);
        void onShare(HistoryItem item);
        void onDownload(HistoryItem item);
        void onRename(HistoryItem item);
        void onDelete(HistoryItem item);}
    public void setActionListener(ActionListener listener) {
        this.actionListener = listener;}
    public void setItems(List<HistoryItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);}
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        HistoryItem item = items.get(position);
        holder.tvFileName.setText(item.fileName);
        SimpleDateFormat sdf = new SimpleDateFormat(
                "MMM d, yyyy • h:mm a",
                Locale.getDefault()
        );
        holder.tvDownloadedAt.setText(
                "Downloaded • " + sdf.format(item.downloadedAt)
        );
        holder.iconFileType.setImageResource(
                getIconForType(item.fileType)
        );
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(item);
            }
        });
        // THREE-DOT MENU
        holder.btnItemAction.setOnClickListener(v ->
                showPopupMenu(v, item)
        );
    }
    // POPUP MENU
    private void showPopupMenu(View anchor, HistoryItem item) {
        PopupMenu popup = new PopupMenu(
                anchor.getContext(),
                anchor
        );
        popup.getMenuInflater().inflate(
                R.menu.history_item_menu,
                popup.getMenu()
        );
        popup.setOnMenuItemClickListener(menuItem -> {
            if (actionListener == null) {
                return true;
            }
            int id = menuItem.getItemId();
            if (id == R.id.action_share) {
                actionListener.onShare(item);
                return true;
            } else if (id == R.id.action_download) {
                actionListener.onDownload(item);
                return true;
            } else if (id == R.id.action_rename) {
                actionListener.onRename(item);
                return true;
            } else if (id == R.id.action_delete) {
                actionListener.onDelete(item);
                return true;
            }
            return false;
        });
        popup.show();
    }
    // FILE TYPE ICON
    private int getIconForType(String fileType) {
        if (fileType == null) {
            return R.drawable.ic_document;
        }
        switch (fileType) {
            case "image":
                return R.drawable.ic_image;
            case "audio":
                return R.drawable.ic_audio;
            case "video":
                return R.drawable.ic_video;
            case "pdf":
                return R.drawable.ic_pdf;
            default:
                return R.drawable.ic_document;
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        TextView tvDownloadedAt;
        ImageView iconFileType;
        ImageButton btnItemAction;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(
                    R.id.tvFileName
            );
            tvDownloadedAt = itemView.findViewById(
                    R.id.tvDownloadedAt
            );
            iconFileType = itemView.findViewById(
                    R.id.iconFileType
            );
            btnItemAction = itemView.findViewById(
                    R.id.btnItemAction
            );
        }
    }
}