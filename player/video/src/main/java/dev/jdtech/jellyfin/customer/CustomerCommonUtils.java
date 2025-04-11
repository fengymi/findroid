package dev.jdtech.jellyfin.customer;

import android.content.Context;
import android.widget.Toast;

import timber.log.Timber;

public class CustomerCommonUtils {
    public static void show(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String message, int time) {
        if (context == null) {
            Timber.e("CustomerCommonUtils.show context must not null");
            return;
        }
        Toast.makeText(context, message, time).show();
    }
}
