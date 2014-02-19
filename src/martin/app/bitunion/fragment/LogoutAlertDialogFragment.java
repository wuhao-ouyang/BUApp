package martin.app.bitunion.fragment;

import martin.app.bitunion.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LogoutAlertDialogFragment extends DialogFragment {
	
	public interface LogoutAlertDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
	
	LogoutAlertDialogListener mListener;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setMessage("确定要登出吗？")
               .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mListener.onDialogPositiveClick(LogoutAlertDialogFragment.this);
                   }
               })
               .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
//                	   mListener.onDialogNegativeClick(LogoutAlertDialogFragment.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
	}
}
