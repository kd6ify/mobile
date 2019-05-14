package com.futureconcepts.drake.ui.os;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.constants.ContactListListenerConstants;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.utils.ErrorResUtils;

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

    protected void promptDisconnectedEvent(Message msg)
    {
        ImErrorInfo error = (ImErrorInfo) msg.obj;
        String promptMsg;
        if (error != null) {
            promptMsg = mActivity.getString(R.string.signed_out_prompt_with_error,
                    "AntaresX", ErrorResUtils.getErrorRes(mRes, error.getCode()));
        } else {
            promptMsg = mActivity.getString(R.string.signed_out_prompt, "AntaresX");
        }
        Toast.makeText(mActivity, promptMsg, Toast.LENGTH_SHORT).show();
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

    public void showServiceErrorAlert()
    {
        showAlert(R.string.error, R.string.service_error);
    }

    public void showContactError(int errorType, ImErrorInfo error, String listName, Contact contact)
    {
        int id = 0;
        switch (errorType) {
        case ContactListListenerConstants.ERROR_LOADING_LIST:
            id = R.string.load_contact_list_failed;
            break;

        case ContactListListenerConstants.ERROR_CREATING_LIST:
            id = R.string.add_list_failed;
            break;

        case ContactListListenerConstants.ERROR_BLOCKING_CONTACT:
            id = R.string.block_contact_failed;
            break;

        case ContactListListenerConstants.ERROR_UNBLOCKING_CONTACT:
            id = R.string.unblock_contact_failed;
            break;
        }

        String errorInfo = ErrorResUtils.getErrorRes(mRes, error.getCode());
        if (id != 0) {
            errorInfo = mRes.getText(id) + "\n" + errorInfo;
        }
        showAlert(R.string.error, errorInfo);
    }
}
