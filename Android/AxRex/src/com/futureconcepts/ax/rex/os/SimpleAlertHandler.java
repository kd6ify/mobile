package com.futureconcepts.ax.rex.os;

import com.futureconcepts.ax.rex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class SimpleAlertHandler extends Handler
{
    Activity mActivity;
    Resources mRes;

    public SimpleAlertHandler(Activity activity)
    {
        mActivity = activity;
        mRes = mActivity.getResources();
    }

    public void showAlert(int titleId, int messageId)
    {
        showAlert(mRes.getString(titleId), mRes.getString(messageId));
    }

    public void showAlert(int titleId, CharSequence message)
    {
        showAlert(mRes.getString(titleId), message);
    }

    public void showAlert(CharSequence title, int messageId)
    {
        showAlert(title, mRes.getString(messageId));
    }

    public void showAlert(final CharSequence title, final CharSequence message)
    {
        if (Looper.myLooper() == getLooper()) {
            new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        } else {
            post(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mActivity)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
            });
        }
    }
}
