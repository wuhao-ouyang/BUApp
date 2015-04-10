package martin.app.bitunion.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.model.BUThread;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class BUAppUtils {

    public static final int BITNET = 0;
    public static final int OUTNET = 1;
    public static final int MAIN_REQ = 1;
    public static final int DISPLAY_REQ = 3;
    public static final int DISPLAY_RESULT = 4;

    public static final int EXIT_WAIT_TIME = 2000;

    public static final int POSTS_PER_PAGE = 40; // 每页显示帖子数量
    public static final int THREADS_PER_PAGE = 40; // 每页显示帖子数量

    public static final String QUOTE_HEAD = "<br><br><center><table border='0' width='90%' cellspacing='0' cellpadding='0'><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    public static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    public static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    public enum Result {
        SUCCESS, // 返回数据成功，result字段为success
        FAILURE, // 返回数据失败，result字段为failure
        NETWRONG, // 没有返回数据
        UNKNOWN;
    };

    public static InputStream getImageVewInputStream(String imagepath)
            throws IOException {
        if (imagepath.contains("bitunion.org"))
            if (imagepath.contains(".php?"))
                return null;
        if (!BUApplication.settings.showimage)
            return null;

        InputStream inputStream = null;
        URL url = new URL(imagepath);
        if (url != null) {
            HttpURLConnection httpConnection = (HttpURLConnection) url
                    .openConnection();
            httpConnection.setConnectTimeout(5000); // 设置连接超时
            httpConnection.setRequestMethod("GET");
            if (httpConnection.getResponseCode() == 200) {
                inputStream = httpConnection.getInputStream();
            }
        }
        return inputStream;
    }

    /**
     * 将px转换为dip
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String replaceHtmlChar(String str) {
        String htmlstring = str;
        htmlstring = htmlstring.replace("&amp;", "&");
        htmlstring = htmlstring.replace("&nbsp;", " ");
        htmlstring = htmlstring.replace("&lt;", "<");
        htmlstring = htmlstring.replace("&gt;", ">");
        return htmlstring;
    }

    public static String hashImgUrl(String imgUrl) throws NoSuchAlgorithmException{
        String imgKey = null;
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(imgUrl.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        imgKey = bigInt.toString(16);
        while (imgKey.length() < 32)
            imgKey = "0" + imgKey;
        return imgKey;
    }

    public static String readTextFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        String result = null;
        if (is == null)
            return null;
        while ((len = is.read(data)) != -1) {
            outputStream.write(data, 0, len);
        }
        result = new String(outputStream.toByteArray());
        return result;

    }

}
