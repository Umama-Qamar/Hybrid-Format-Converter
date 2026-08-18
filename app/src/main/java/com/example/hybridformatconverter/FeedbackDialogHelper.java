package com.example.hybridformatconverter;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Reusable Feedback popup dialog with a 200-word limit.
 *
 * Usage (e.g. from SettingsActivity):
 *   FeedbackDialogHelper.show(this);
 */
public class FeedbackDialogHelper {

    private static final int MAX_WORDS = 200;

    public static void show(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etFeedback = dialog.findViewById(R.id.etFeedback);
        TextView tvWordCount = dialog.findViewById(R.id.tvWordCount);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btnSubmit);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Live word counter + enforce 200-word limit
        etFeedback.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            private String lastValidText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEditing) return;

                String text = editable.toString();
                int wordCount = countWords(text);

                if (wordCount > MAX_WORDS) {
                    isEditing = true;
                    etFeedback.setText(lastValidText);
                    etFeedback.setSelection(lastValidText.length());
                    isEditing = false;
                    Toast.makeText(context, "Maximum 200 words allowed", Toast.LENGTH_SHORT).show();
                } else {
                    lastValidText = text;
                }

                tvWordCount.setText(countWords(etFeedback.getText().toString()) + "/" + MAX_WORDS + " words");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String feedbackText = etFeedback.getText().toString().trim();

            if (TextUtils.isEmpty(feedbackText)) {
                Toast.makeText(context, "Please write your feedback before submitting", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnCancel.setEnabled(false);
            btnSubmit.setText("Submitting...");

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("message", feedbackText);
            feedbackData.put("timestamp", FieldValue.serverTimestamp());
            feedbackData.put("appVersion", "1.0.0");
            feedbackData.put("device", Build.MANUFACTURER + " " + Build.MODEL);
            feedbackData.put("androidVersion", Build.VERSION.RELEASE);

            db.collection("feedback")
                    .add(feedbackData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            btnCancel.setEnabled(true);
                            btnSubmit.setText("Submit");
                            Toast.makeText(context, "Failed to send feedback. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private static int countWords(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return trimmed.split("\\s+").length;
    }
}