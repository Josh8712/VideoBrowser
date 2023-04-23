package com.jcomp.browser.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

public class EditDialog {
    public static AlertDialog show(Context context, String title, String placeholder, EditCallback callback) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        final EditText input = new EditText(context);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        input.setHint(placeholder);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onClick(input.getText().toString());
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
                callback.onClick(input.getText().toString());
                return true;
            }
            return false;
        });
        return dialog;
    }

    public abstract static class EditCallback {
        public abstract void onClick(String result);
    }
}
