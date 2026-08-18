package com.example.hybridformatconverter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

/**
 * Reusable permission dialog shown before any conversion / compression /
 * text extraction / PDF tool action.
 *
 * Usage:
 *   PermissionDialogHelper.checkAndShow(this, "JPG to SVG", new PermissionDialogHelper.Callback() {
 *       @Override public void onProceed() {
 *           // do the actual conversion / action here
 *       }
 *       @Override public void onDenied() {
 *           // user pressed "Don't allow" - do nothing, stay on screen
 *       }
 *   });
 */
public class PermissionDialogHelper {

    private static final String PREFS_NAME = "permission_prefs";
    private static final String KEY_ALLOWED_ALWAYS = "allowed_always";

    public interface Callback {
        void onProceed();
        void onDenied();
    }

    /**
     * Checks if the user already granted permanent access.
     * If yes -> skips the dialog and proceeds directly.
     * If no  -> shows the permission dialog.
     *
     * @param context      Activity context
     * @param actionLabel  e.g. "JPG to SVG", "Compress PNG", "Extract Text from PDF"
     * @param callback     callback for proceed / denied
     */
    public static void checkAndShow(Context context, String actionLabel, Callback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean allowedAlways = prefs.getBoolean(KEY_ALLOWED_ALWAYS, false);

        if (allowedAlways) {
            // Already granted permanent permission - skip dialog
            callback.onProceed();
            return;
        }

        showDialog(context, actionLabel, callback);
    }

    private static void showDialog(Context context, String actionLabel, Callback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_permission);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMessage = dialog.findViewById(R.id.tvDynamicMessage);
        tvMessage.setText("If you want to " + actionLabel + ", allow permission to access files.");

        MaterialButton btnAllow = dialog.findViewById(R.id.btnAllowAccess);
        MaterialButton btnOnlyThisTime = dialog.findViewById(R.id.btnOnlyThisTime);
        MaterialButton btnDontAllow = dialog.findViewById(R.id.btnDontAllow);

        btnAllow.setOnClickListener(v -> {
            // Save permanent permission - won't ask again
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_ALLOWED_ALWAYS, true).apply();
            dialog.dismiss();
            callback.onProceed();
        });

        btnOnlyThisTime.setOnClickListener(v -> {
            // Proceed now but don't save - will ask again next time
            dialog.dismiss();
            callback.onProceed();
        });

        btnDontAllow.setOnClickListener(v -> {
            dialog.dismiss();
            callback.onDenied();
        });

        dialog.show();
    }
}