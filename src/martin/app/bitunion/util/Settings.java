package martin.app.bitunion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

public class Settings {
    public static final int POSTS_PER_PAGE = 40; // 每页显示帖子数量
    public static final int THREADS_PER_PAGE = 40; // 每页显示帖子数量
    
    public int titletextsize;
    public int contenttextsize;

    public int netType;

    public boolean showSignature;
    public boolean showImage;
    public boolean useReferAt;
    public boolean sendStat;

    public void writePreference(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putInt("net_type", netType);
        editor.putInt("title_text_size", titletextsize);
        editor.putInt("content_text_size", contenttextsize);
        editor.putBoolean("show_signature", showSignature);
        editor.putBoolean("show_image", showImage);
        editor.putBoolean("reference_at", useReferAt);
        editor.putBoolean("send_user_statistics", sendStat);
        editor.apply();
    }

    public void readPreference(Context context) {
        float dpi = context.getResources().getDisplayMetrics().densityDpi;
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        netType = config.getInt("net_type", Constants.OUTNET);
        titletextsize = config.getInt("title_text_size", (dpi > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        contenttextsize = config.getInt("content_text_size", (dpi > DisplayMetrics.DENSITY_HIGH) ? 14 : 12);
        showSignature = config.getBoolean("show_signature", true);
        showImage = config.getBoolean("show_image", true);
        useReferAt = config.getBoolean("reference_at", false);
        sendStat = config.getBoolean("send_user_statistics", true);
    }
}
