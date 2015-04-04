package martin.app.bitunion.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

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
    public static final int MAIN_RESULT = 2;
    public static final int DISPLAY_REQ = 3;
    public static final int DISPLAY_RESULT = 4;

    public static final int EXIT_WAIT_TIME = 2000;

    public static final int POSTS_PER_PAGE = 40; // 每页显示帖子数量
    public static final int THREADS_PER_PAGE = 40; // 每页显示帖子数量

    public static final String QUOTE_HEAD = "<br><br><center><table border='0' width='90%' cellspacing='0' cellpadding='0'><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    public static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    public static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    public static final String NETWRONG = "网络错误";
    public static final String LOGINFAIL = "登录失败";
    public static final String POSTFAILURE = "发送失败";
    public static final String POSTSUCCESS = "发送成功，刷新查看回复";
    public static final String POSTEXECUTING = "消息发送中...";
    public static final String USERNAME = "用户";
    public static final String LOGINSUCCESS = "登录成功";
    public static final String CLIENTMESSAGETAG = "\n\n发送自 [url=https://play.google.com/store/apps/details?id=martin.app.bitunion][b]BUApp Android[/b][/url]";

    public static final int REQ_LOGGING = 0;
    public static final int REQ_FORUM = 1;
    public static final int REQ_THREAD = 2;
    public static final int REQ_PROFILE = 3;
    public static final int REQ_POST = 4;
    public static final int NEWPOST = 5;
    public static final int NEWTHREAD = 6;
    public static final int REQ_FID_TID_SUM = 7;

    public enum Result {
        SUCCESS, // 返回数据成功，result字段为success
        FAILURE, // 返回数据失败，result字段为failure
        SUCCESS_EMPTY, // 返回数据成功，但字段没有数据
        NETWRONG, // 没有返回数据
        UNKNOWN;
    };

    public static String getUrl(int net, int urlType){
        String ROOTURL, BASEURL;

        ROOTURL = net == BITNET ? "http://www.bitunion.org" : "http://out.bitunion.org";
        BASEURL = ROOTURL + "/open_api";
        if (urlType == REQ_LOGGING)
            return BASEURL + "/bu_logging.php";
        if (urlType == REQ_FORUM)
            return BASEURL + "/bu_forum.php";
        if (urlType == REQ_THREAD)
            return BASEURL + "/bu_thread.php";
        if (urlType == REQ_PROFILE)
            return BASEURL + "/bu_profile.php";
        if (urlType == REQ_POST)
            return BASEURL + "/bu_post.php";
        if (urlType == REQ_FID_TID_SUM)
            return BASEURL + "/bu_fid_tid.php";
        if (urlType == NEWPOST)
            return BASEURL + "/bu_newpost.php";
        if (urlType == NEWTHREAD)
            return BASEURL + "/bu_newpost.php";
        Log.e("BUAppUtils", "getUrl Error!");
        return "";

    }

    public static JSONArray mergeJSONArray(JSONArray array1, JSONArray array2)
            throws JSONException {
        JSONArray array = new JSONArray(array1.toString());
        if (array2.length() > 0 && array2 != null)
            for (int i = 0; i < array2.length(); i++) {
                array.put(array2.getJSONObject(i));
            }
        return array;
    }

    public static ArrayList<BUThread> jsonToThreadlist(JSONArray array) {
        ArrayList<BUThread> list = new ArrayList<BUThread>();
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUThread(array.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e("JSONError", "Error>>\n" + array.toString());
                e.printStackTrace();
            }
        // Log.v("page", "array parsed");
        return list;
    }

    public static ArrayList<BUPost> jsonToPostlist(JSONArray array, int page) {
        ArrayList<BUPost> list = new ArrayList<BUPost>();
        int offset = page * POSTS_PER_PAGE + 1;
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUPost(array.getJSONObject(i), i + offset));
            } catch (JSONException e) {
                Log.e("JSONError", "Error>>\n" + array.toString());
                e.printStackTrace();
            }
        // Log.v("page", "array parsed");
        return list;
    }

    public static InputStream getImageVewInputStream(String imagepath)
            throws IOException {
        if (imagepath.contains("bitunion.org"))
            if (imagepath.contains(".php?"))
                return null;
        if (!MainActivity.settings.showimage)
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
     * 将dip转换为px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px转换为dip
     *
     * @param context
     * @param dipValue
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

    public static class HtmlImageGetter implements Html.ImageGetter {

        private static HashMap<String, Drawable> imgCache = new HashMap<String, Drawable>();
        private TextView htmlTextView;
        private Drawable defaultDrawable;
        private Context mContext;

        public HtmlImageGetter(Context c, TextView view) {
            mContext = c;
            htmlTextView = view;
            defaultDrawable = mContext.getResources().getDrawable(
                    R.drawable.ic_action_picture);
        }

        @Override
        public Drawable getDrawable(String imgUrl) {
            // Get MD5 of imgUrl
            String imgKey = null;
            try {
                imgKey = hashImgUrl(imgUrl);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.e("HtmlImageGetter", "Hash error>>" + imgUrl);
            }

            // Check if image is in cache
            if (imgCache.get(imgKey) != null)
                return imgCache.get(imgKey);

            Log.v("ImageGetter", "img not cached");
            imgUrl = imgUrl.replaceAll("(http://)?((out.|kiss.|www.)?bitunion.org|btun.yi.org|10.1.10.253)", MainActivity.settings.ROOTURL);
            imgUrl = imgUrl.replace("..", MainActivity.settings.ROOTURL);

            Log.v("ImageGetter", "img Url>>" + imgUrl);
            URLDrawable urlDrawable = new URLDrawable(defaultDrawable);
            new AsyncThread(urlDrawable).execute(imgKey, imgUrl);
            return urlDrawable;
        }

        private class AsyncThread extends AsyncTask<String, Integer, Drawable> {
            private String imgKey;
            private URLDrawable drawable;

            public AsyncThread(URLDrawable drawable) {
                this.drawable = drawable;
            }

            @Override
            protected Drawable doInBackground(String... strings) {
                imgKey = strings[0];
                InputStream inps = null;
                try {
                    inps = BUAppUtils.getImageVewInputStream(strings[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                if (inps == null)
                    return null;
                Drawable drawable = Drawable.createFromStream(inps, imgKey);
                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable result) {
                if (result == null)
                    return;
                imgCache.put(imgKey, drawable);
                drawable.setDrawable(result);
                htmlTextView.setText(htmlTextView.getText());
            }
        }

        public class URLDrawable extends BitmapDrawable {

            private Drawable drawable;

            @SuppressWarnings("deprecation")
            public URLDrawable(Drawable defaultDraw) {
                setDrawable(defaultDraw);
            }

            private void setDrawable(Drawable ndrawable) {
                drawable = ndrawable;
//				float dpi = MainActivity.PIXDENSITY;
                float scalingFactor = (float) htmlTextView.getMeasuredWidth()
                        / drawable.getIntrinsicWidth();
                if (drawable.getIntrinsicWidth() < 100) {
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    return;
                }
                drawable.setBounds(0, 0, htmlTextView.getMeasuredWidth(),
                        (int) (drawable.getIntrinsicHeight() * scalingFactor));
                setBounds(0, 0, htmlTextView.getMeasuredWidth(),
                        (int) (drawable.getIntrinsicHeight() * scalingFactor));
            }

            @Override
            public void draw(Canvas canvas) {
                drawable.draw(canvas);
            }
        }
    }

}
