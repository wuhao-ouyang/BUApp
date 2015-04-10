package martin.app.bitunion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import martin.app.bitunion.BUApplication;

public class BUAppSettings {
    public int titletextsize;
    public int contenttextsize;
    public String titlebackground;
    public String textbackground;
    public String listbackgrounddark;
    public String listbackgroundlight;

    public int mNetType;

    public boolean showsigature;
    public boolean showimage;
    public boolean referenceat;

    public void writePreference(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putInt("nettype", mNetType);
        editor.putInt("titletextsize", titletextsize);
        editor.putInt("contenttextsize", contenttextsize);
        editor.putBoolean("showsigature", showsigature);
        editor.putBoolean("showimage", showimage);
        editor.putBoolean("referenceat", referenceat);
        editor.apply();
    }

    public void readPreference(Context context) {
        float dpi = context.getResources().getDisplayMetrics().densityDpi;
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
        titletextsize = config.getInt("titletextsize", (dpi > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        contenttextsize = config.getInt("contenttextsize", (dpi > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        showsigature = config.getBoolean("showsigature", true);
        showimage = config.getBoolean("showimage", true);
        referenceat = config.getBoolean("referenceat", false);
    }
}
