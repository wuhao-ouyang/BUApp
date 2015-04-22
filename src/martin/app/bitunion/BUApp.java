package martin.app.bitunion;

import android.app.Application;

import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.Settings;

public class BUApp extends Application {
    private static BUApp instance;

    // App settings information, including network type, theme, preferences
    public static Settings settings = new Settings();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        settings.readPreference(this);
        BUApi.init(this);
    }

    public synchronized static BUApp getInstance() {
        return instance;
    }
}
