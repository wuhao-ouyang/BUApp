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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class Utils {
    public static final int EXIT_WAIT_TIME = 2000;

    public static final String QUOTE_HEAD = "<br><br><center><table border='0' width='90%' cellspacing='0' cellpadding='0'><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    public static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    public static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    ;

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

    /**
     * Get readable string of file size (e.g. 24B, 245k, 4.5M)
     * @return display string
     */
    public static String getFileSizeString(long fileSize) {
        // TODO
        return null;
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
