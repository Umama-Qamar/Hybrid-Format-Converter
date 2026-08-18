package com.example.hybridformatconverter;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
 * Reusable Rate App popup dialog with 5-star rating.
 *
 * Usage (e.g. from SettingsActivity):
 *   RateAppDialogHelper.show(this);
 */
public class RateAppDialogHelper {

    public static void show(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rate_app);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageButton[] stars = new ImageButton[5];
        stars[0] = dialog.findViewById(R.id.star1);
        stars[1] = dialog.findViewById(R.id.star2);
        stars[2] = dialog.findViewById(R.id.star3);
        stars[3] = dialog.findViewById(R.id.star4);
        stars[4] = dialog.findViewById(R.id.star5);

        MaterialButton btnSubmit = dialog.findViewById(R.id.btnSubmit);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        final int[] selectedRating = {0};

        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1; // 1 to 5
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex;
                updateStars(stars, starIndex);
            });
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            if (selectedRating[0] == 0) {
                Toast.makeText(context, "Please select a star rating", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnCancel.setEnabled(false);
            btnSubmit.setText("Submitting...");

            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("rating", selectedRating[0]);
            ratingData.put("timestamp", FieldValue.serverTimestamp());
            ratingData.put("appVersion", "1.0.0");
            ratingData.put("device", Build.MANUFACTURER + " " + Build.MODEL);
            ratingData.put("androidVersion", Build.VERSION.RELEASE);

            db.collection("ratings")
                    .add(ratingData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(context, "Thanks for rating us!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, "Failed to submit rating. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private static void updateStars(ImageButton[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
    }
}