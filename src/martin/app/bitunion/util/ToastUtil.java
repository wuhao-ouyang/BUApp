package martin.app.bitunion.util;

import android.support.annotation.NonNull;
import android.widget.Toast;

import martin.app.bitunion.BUApp;

public class ToastUtil {

    public static void showToast(@NonNull String message) {
        if (BUApp.getInstance() != null)
            Toast.makeText(BUApp.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int res) {
        if (BUApp.getInstance() != null)
            Toast.makeText(BUApp.getInstance(), res, Toast.LENGTH_SHORT).show();
    }
}
