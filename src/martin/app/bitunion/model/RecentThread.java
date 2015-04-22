package martin.app.bitunion.model;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class RecentThread extends BUContent {

    public int tid;
    public int fid;
    public String title;
    public String forum;
    public int replies;
    public String author;
    public String lastTime;
    public String lastAuthor;
    public String lastReply;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tid);
        dest.writeInt(fid);
        dest.writeString(title);
        dest.writeString(forum);
        dest.writeInt(replies);
        dest.writeString(author);
        dest.writeString(lastTime);
        dest.writeString(lastAuthor);
        dest.writeString(lastReply);
    }

    public static final Creator<RecentThread> CREATOR = new Creator<RecentThread>() {
        @Override
        public RecentThread createFromParcel(Parcel source) {
            return new RecentThread(source);
        }

        @Override
        public RecentThread[] newArray(int size) {
            return new RecentThread[size];
        }
    };

    RecentThread(Parcel in) {
        tid = in.readInt();
        fid = in.readInt();
        title = in.readString();
        forum = in.readString();
        replies = in.readInt();
        author = in.readString();
        lastTime = in.readString();
        lastAuthor = in.readString();
        lastReply = in.readString();
    }

    public RecentThread(JSONObject json) throws JSONException {
        try {
            tid = json.getInt("tid");
            fid = json.getInt("fid");
            title = URLDecoder.decode(json.getString("pname"), "utf-8");
            forum = URLDecoder.decode(json.getString("fname"), "utf-8");
            replies = json.getInt("tid_sum");
            author = URLDecoder.decode(json.getString("author"), "utf-8");
            JSONObject last = json.getJSONObject("lastreply");
            lastAuthor = URLDecoder.decode(last.getString("who"), "utf-8");
            lastReply = URLDecoder.decode(last.getString("what"), "utf-8");
            lastTime = URLDecoder.decode(last.getString("when"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
