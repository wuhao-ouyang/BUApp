package martin.app.bitunion;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;

import martin.app.bitunion.model.BUUserInfo;
import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppSettings;
import martin.app.bitunion.util.BUAppUtils;

public class BUApplication extends Application {
    private static BUApplication instance;

    // 静态变量在整个应用中传递网络连接参数，包括session, username, password信息
    public static BUAppSettings settings = new BUAppSettings();

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        settings.readPreference(this);
        BUApiHelper.init(this);
    }

    public static BUApplication getInstance() {
        return instance;
    }
}
