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

    // 静态变量在整个应用中传递网络连接参数，包括session, username, password信息
    public static BUAppSettings settings = new BUAppSettings();
    private static BUApplication instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        readConfig();
        BUApiHelper.init(this);
    }

    public static BUApplication getInstance() {
        return instance;
    }

    private void readConfig() {
        float dpi = getResources().getDisplayMetrics().densityDpi;
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        BUApplication.settings.mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
        BUApplication.settings.titletextsize = config.getInt("titletextsize", (dpi > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        BUApplication.settings.contenttextsize = config.getInt("contenttextsize", (dpi > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        BUApplication.settings.showsigature = config.getBoolean("showsigature", true);
        BUApplication.settings.showimage = config.getBoolean("showimage", true);
        BUApplication.settings.referenceat = config.getBoolean("referenceat", false);
    }
}
