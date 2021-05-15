package com.lukaskorinek.pocketracetimer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.lukaskorinek.pocketracetimer.R;

public class LoadingDialog {

        Activity activity;
        AlertDialog dialog;

        public LoadingDialog(Activity activity) {
            this.activity = activity;
        }

        public void startLoadingAnimation() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_loading, null));
            builder.setCancelable(true);

            dialog = builder.create();
            dialog.show();
        }

        public void dismissLoadingAnimation() {
            dialog.dismiss();
        }

}
