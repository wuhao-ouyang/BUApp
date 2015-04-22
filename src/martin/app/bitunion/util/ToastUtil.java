package martin.app.bitunion.util;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import martin.app.bitunion.BUApp;

public class ToastUtil {

    public static void showToast(@NonNull String message) {
        if (BUApp.getInstance() != null)
            Toast.makeText(BUApp.getInstance(), message, Toast.LENGTH_SHORT).show();
        Log.i("Toast", message);
    }

    public static void showToast(int res) {
        Log.i("Toast", BUApp.getInstance().getString(res));
        if (BUApp.getInstance() != null)
            Toast.makeText(BUApp.getInstance(), res, Toast.LENGTH_SHORT).show();
    }
}
