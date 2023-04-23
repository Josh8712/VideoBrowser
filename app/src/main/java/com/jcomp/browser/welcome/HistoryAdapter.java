package com.jcomp.browser.welcome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.history.HistoryBookmark;
import com.jcomp.browser.history.HistoryDownload;
import com.jcomp.browser.history.HistorySetting;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.menu.Popup;
import com.jcomp.browser.widget.EditDialog;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    List<History> mHistoryList;
    Callback mCallback;
    View focusCache = null;
    RecyclerView recyclerView;

    public HistoryAdapter(Callback callback) {
        mHistoryList = new ArrayList<>();
        mCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = mHistoryList.get(position);
        holder.title.setText(history.getDisplayName());
        holder.icon.setVisibility(View.GONE);
        holder.placeholder.setVisibility(View.GONE);
        history.setIcon(holder.icon, holder.placeholder);
        holder.itemView.setOnClickListener(view -> mCallback.open(history));
        holder.itemView.setOnLongClickListener(view -> {
            openPopup(view, history, position);
            holder.title.setSelected(true);
            if (focusCache != null)
                focusCache.setSelected(false);
            focusCache = holder.title;
            return true;
        });
    }

    private void openPopup(View view, History history, int position) {
        PopupMenu menu = new PopupMenu(view.getContext(), view);
        menu.getMenu().add(Menu.NONE, 1, 1, R.string.open);
        if (history.isRemovable()) {
            menu.getMenu().add(Menu.NONE, 3, 1, R.string.edit_name);
            menu.getMenu().add(Menu.NONE, Popup.browserID, 1, R.string.open_in_browser);
            menu.getMenu().add(Menu.NONE, Popup.copyID, 1, R.string.copy_url);
            menu.getMenu().add(Menu.NONE, Popup.shareID, 1, R.string.share_to);
            menu.getMenu().add(Menu.NONE, 2, 2, R.string.delete);
        }
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    mCallback.open(history);
                    break;
                case 2:
                    mHistoryList.remove(history);
                    notifyItemRemoved(position);
                    new Thread(() -> {
                        AppDatabase.getInstance(view.getContext()).historyDoa().delete(history);
                    }).start();
                    break;
                case 3:
                    EditDialog.show(view.getContext(), view.getContext().getString(R.string.name), history.getTitle(),
                            new EditDialog.EditCallback() {
                                @Override
                                public void onClick(String result) {
                                    history.setCustomName(result);
                                    notifyItemChanged(position);
                                    new Thread(() -> {
                                        AppDatabase.getInstance(view.getContext()).historyDoa().update(history);
                                    }).start();
                                }
                            });
                    break;
                case Popup.browserID:
                    Popup.browserCallback(view.getContext(), history.getUrl());
                    break;
                case Popup.copyID:
                    Popup.copyToClipboard(view.getContext(), history.getUrl());
                    break;
                case Popup.shareID:
                    Popup.shareCallback(view.getContext(), history.getTitle(), history.getUrl());
                    break;
            }
            return true;
        });
        menu.show();
    }

    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void setData(List<History> historyList, Context context) {
        historyList.sort((o1, o2) -> -(int) (o1.getTimestamp() - o2.getTimestamp()));
        historyList.add(0, new HistorySetting(context));
        historyList.add(0, new HistoryDownload(context));
        historyList.add(0, new HistoryBookmark(context));
        mHistoryList.clear();
        mHistoryList.addAll(historyList);
        notifyDataSetChanged();
    }

    public interface Callback {
        void open(History history);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placeholder;
        TextView title;
        ImageView icon;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            icon = view.findViewById(R.id.icon);
            placeholder = view.findViewById(R.id.icon_text);
        }
    }
}
