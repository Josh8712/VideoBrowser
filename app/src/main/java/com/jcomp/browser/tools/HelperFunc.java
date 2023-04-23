package com.jcomp.browser.tools;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class HelperFunc {
    static Toast toast;

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String fixURLWithUrl(String full, String url) {
        if (!url.startsWith("http")) {
            try {
                URL urlClass = new URL(full);
                if (url.startsWith("../")) {
                    String path = urlClass.getPath();
                    int count = 0;
                    int idx = path.lastIndexOf("/");
                    while (idx >= 0 && count < 2 && path.length() > 0) {
                        path = path.substring(0, idx);
                        idx = path.lastIndexOf("/");
                        count += 1;
                    }
                    full = "https://" + urlClass.getHost() + path + "/";
                    url = url.substring(3);
                }
                urlClass = new URL(full);
                if (url.startsWith("//"))
                    url = "https:" + url;
                else if (url.startsWith("/"))
                    url = "https://" + urlClass.getHost() + url;
                else {
                    int idx = urlClass.getPath().lastIndexOf("/");
                    if (idx == -1) {
                        url = "https://" + urlClass.getHost() + "/" + url;
                    } else {
                        url = "https://" + urlClass.getHost() + urlClass.getPath().substring(0, idx + 1) + url;
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static String getDomainName(String s) {
        try {
            String host = new URL(s).getHost();
            String[] seg = host.split("\\.");
            int choice = seg.length - 2;
            if (choice < 0)
                choice = 1;
            return seg[choice];
        } catch (Exception exception) {
        }
        return s;
    }

    public static void showToast(Context context, CharSequence message, int length) {
        closeToast();
        Toast _toast = Toast.makeText(context, message, length);
        toast = _toast;
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (toast == _toast)
                    toast = null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        toast.show();
    }

    public static void showToast(Context context, int message, int length) {
        showToast(context, context.getText(message), length);
    }

    public static void showToast(Context context, int message) {
        showToast(context, context.getText(message), Toast.LENGTH_SHORT);
    }

    private static void closeToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    public static void copyToClipboard(Context context, String content) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("url", content);
        clipboard.setPrimaryClip(clip);
    }


}
