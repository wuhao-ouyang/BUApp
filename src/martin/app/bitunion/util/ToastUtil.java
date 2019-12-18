package martin.app.bitunion.util;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import martin.app.bitunion.BUApp;

public class ToastUtil {
    private static Toast toast;

    public static void showToast(@NonNull String message) {
        if (BUApp.getInstance() != null) {
            if (toast == null)
                toast = Toast.makeText(BUApp.getInstance(), "", Toast.LENGTH_SHORT);
            toast.setText(message);
            toast.show();
        }
        Log.i("Toast", message);
    }

    public static void showToast(int res) {
        if (BUApp.getInstance() != null) {
            if (toast == null)
                toast = Toast.makeText(BUApp.getInstance(), "", Toast.LENGTH_SHORT);
            toast.setText(res);
            toast.show();
        }
        Log.i("Toast", BUApp.getInstance().getString(res));
    }
}
