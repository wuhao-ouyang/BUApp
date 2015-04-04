package martin.app.bitunion.fragment;

import martin.app.bitunion.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {

    public interface ConfirmDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);

        public void onDialogPositiveClick(DialogFragment dialog,
                                          String replymessage);
    }

    ConfirmDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        final String message = getArguments().getString("message");
        final String replymessage = getArguments().getString("reply");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.AlertDialog);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (replymessage == null || replymessage.isEmpty())
                            mListener
                                    .onDialogPositiveClick(ConfirmDialogFragment.this);
                        else
                            mListener.onDialogPositiveClick(
                                    ConfirmDialogFragment.this, replymessage);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // mListener.onDialogNegativeClick(LogoutAlertDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the
            // host
            mListener = (ConfirmDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
