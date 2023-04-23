package com.jcomp.browser.parser.searcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.jcomp.browser.R;
import com.jcomp.browser.main.MainActivity;

import java.text.MessageFormat;

public class Searcher {
    String queryFormat;

    public Searcher(String queryFormat) {
        this.queryFormat = queryFormat;
    }

    private void onClick(EditText input, MainActivity activity, DialogInterface dialog, int graph_ID) {
        String query = input.getText().toString().trim();
        if (query.isEmpty())
            return;
        String url = getURL(query);
        activity.popBack();
        activity.subNavigate(query, url, graph_ID);
        dialog.dismiss();
    }

    public void show(MainActivity activity, int graph_ID) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.search));
        final EditText input = new EditText(activity);

        FrameLayout container = new FrameLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Searcher.this.onClick(input, activity, dialog, graph_ID);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.show();
        input.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == android.view.KeyEvent.ACTION_UP && keyEvent.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER) {
                onClick(input, activity, dialog, graph_ID);
                return true;
            }
            return false;
        });
    }

    private String getURL(String query) {
        return MessageFormat.format(queryFormat, query);
    }
}
