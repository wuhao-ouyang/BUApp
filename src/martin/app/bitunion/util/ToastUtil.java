package martin.app.bitunion.util;

import android.support.annotation.NonNull;
import android.widget.Toast;

import martin.app.bitunion.BUApplication;

public class ToastUtil {

    public static void showToast(@NonNull String message) {
        if (BUApplication.getInstance() != null)
            Toast.makeText(BUApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int res) {
        if (BUApplication.getInstance() != null)
            Toast.makeText(BUApplication.getInstance(), res, Toast.LENGTH_SHORT).show();
    }
}
