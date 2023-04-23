package com.jcomp.browser.menu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jcomp.browser.R;
import com.jcomp.browser.tools.HelperFunc;

public class Popup {
    public final static int browserID = 104;
    public final static int copyID = 105;
    public final static int shareID = 106;

    public static void browserCallback(Context context, String url) {
        Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
        i.setData(Uri.parse(url));
        context.startActivity(Intent.createChooser(i, context.getString(R.string.select_open_tool)));
    }

    public static void copyToClipboard(Context context, String url) {
        HelperFunc.copyToClipboard(context, url);
        HelperFunc.showToast(context, R.string.copied);
    }

    public static void shareCallback(Context context, String title, String url) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(send, context.getString(R.string.share_to)));
    }
}
