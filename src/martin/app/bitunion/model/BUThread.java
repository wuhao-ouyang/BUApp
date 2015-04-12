package martin.app.bitunion.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.Utils;

public class BUThread extends BUContent implements Parcelable {

    private int tid;
    private String author;
    private String authorid;
    private String subject;
    private int dateline;
    private String lastpost;
    private String lastposter;
    private int views;
    private int replies;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tid);
        dest.writeString(author);
        dest.writeString(authorid);
        dest.writeString(subject);
        dest.writeInt(dateline);
        dest.writeString(lastpost);
        dest.writeString(lastposter);
        dest.writeInt(views);
        dest.writeInt(replies);
    }

    BUThread(Parcel in) {
        tid = in.readInt();
        author = in.readString();
        authorid = in.readString();
        subject = in.readString();
        dateline = in.readInt();
        lastpost = in.readString();
        lastposter = in.readString();
        views = in.readInt();
        replies = in.readInt();
    }

    public static final Parcelable.Creator<BUThread> CREATOR = new Parcelable.Creator<BUThread>() {

        @Override
        public BUThread createFromParcel(Parcel source) {
            return new BUThread(source);
        }

        @Override
        public BUThread[] newArray(int size) {
            return new BUThread[size];
        }
    };

    public BUThread(JSONObject object) throws JSONException{
        try {
            tid = object.getInt("tid");
            author = URLDecoder.decode(object.getString("author"), "utf-8");
            authorid = object.getString("authorid");
            subject = URLDecoder.decode(object.getString("subject"), "utf-8");
            dateline = object.getInt("dateline");
            lastpost = object.getString("lastpost");
            lastposter = object.getString("lastposter");
            views = object.getInt("views");
            replies = object.getInt("replies");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//		Pattern p = Pattern.compile("<[^>]+>");
//		Matcher m = p.matcher(subject);
//		StringBuffer sb = new StringBuffer();
//		while(m.find())
//			m.appendReplacement(sb, "");
//		m.appendTail(sb);
//		subject = sb.toString();
        subject = subject.replaceAll("<[^>]+>", "");
        subject = Utils.replaceHtmlChar(subject);
    }

    public int getTid() {
        return tid;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorid() {
        return authorid;
    }

    public String getSubject() {
        return subject;
    }

    public String getDateline() {
        Date date = new Date(dateline * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public String getViewsDisplay() {
        int n = views;
        if (n > 9999){
            return Integer.toString(n / 10000) + "." + Integer.toString(n % 10000 / 1000) + "万";
        } else
            return Integer.toString(views);
    }

    public String getRepliesDisplay() {
        int n = replies;
        if (n > 9999){
            return Integer.toString(n / 1000) + "." + Integer.toString(n % 1000 / 100) + "万";
        } else
            return Integer.toString(replies);
    }

    public int getReplies() {
        return replies;
    }
}
