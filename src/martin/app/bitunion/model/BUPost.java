package martin.app.bitunion.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;

public class BUPost extends BUContent implements Parcelable {
    private static final String TAG = BUPost.class.getSimpleName();

    private int pid;
    private int fid;
    private int tid;
    private int aid;
    private String icon;
    private String author;
    private int authorid;
    private String subject;
    private String dateline;
    private String message;
    private String usesig;
    private String attachment;
    private int uid;
    private String username;
    private String avatar;

    private List<BUQuote> quotes = new ArrayList<BUQuote>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pid);
        dest.writeInt(fid);
        dest.writeInt(tid);
        dest.writeInt(aid);
        dest.writeString(icon);
        dest.writeString(author);
        dest.writeInt(authorid);
        dest.writeString(subject);
        dest.writeString(dateline);
        dest.writeString(message);
        dest.writeString(usesig);
        dest.writeString(attachment);
        dest.writeInt(uid);
        dest.writeString(username);
        dest.writeString(avatar);
        if (quotes != null && quotes.size() > 0) {
            BUQuote[] arr = new BUQuote[quotes.size()];
            quotes.toArray(arr);
            dest.writeByte((byte) 0x01);
            dest.writeParcelableArray(arr, 0);
        } else {
            dest.writeByte((byte) 0x00);
        }
    }

    BUPost(Parcel in) {
        pid = in.readInt();
        fid = in.readInt();
        tid = in.readInt();
        aid = in.readInt();
        icon = in.readString();
        author = in.readString();
        authorid = in.readInt();
        subject = in.readString();
        dateline = in.readString();
        message = in.readString();
        usesig = in.readString();
        attachment = in.readString();
        uid = in.readInt();
        username = in.readString();
        avatar = in.readString();
        boolean hasQuotes = in.readByte() != 0x00;
        if (hasQuotes) {
            quotes = Arrays.asList((BUQuote[]) in.readParcelableArray(BUQuote.class.getClassLoader()));
        }
    }

    public static final Parcelable.Creator<BUPost> CREATOR = new Parcelable.Creator<BUPost>() {
        @Override
        public BUPost createFromParcel(Parcel source) {
            return new BUPost(source);
        }

        @Override
        public BUPost[] newArray(int size) {
            return new BUPost[size];
        }
    };

    public BUPost(JSONObject jsonObject) {
        try {
            pid = jsonObject.getInt("pid");
            fid = jsonObject.getInt("fid");
            tid = jsonObject.getInt("tid");
            aid = jsonObject.getInt("aid");
            icon = jsonObject.getString("icon");
            author = URLDecoder.decode(jsonObject.getString("author"), "utf-8");
            authorid = jsonObject.getInt("authorid");
            subject = URLDecoder.decode(jsonObject.getString("subject"), "utf-8");
            dateline = jsonObject.getString("dateline");
            message = URLDecoder.decode(jsonObject.getString("message"), "utf-8");
            usesig = jsonObject.getString("usesig");
            attachment = URLDecoder.decode(jsonObject.getString("attachment"), "utf-8");
            uid = jsonObject.getInt("uid");
            username = URLDecoder.decode(jsonObject.getString("username"), "utf-8");
            avatar = URLDecoder.decode(jsonObject.getString("avatar"), "utf-8");
        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse post object!!!\n" + jsonObject, e);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Failed to parse post object!!!\n" + jsonObject, e);
        }
        // Parse subject
        if (subject != "null" && subject != null && !subject.isEmpty()) {
            subject = subject.replaceAll("<[^>]+>", "");
            subject = BUAppUtils.replaceHtmlChar(subject);
        } else
            subject = null;

        parseQuote();
        parseMessage();
    }

    public void parseQuote() {
        message = message.replace("\"", "'");
        Pattern p = Pattern.compile(BUAppUtils.QUOTE_REGEX);
        Matcher m = p.matcher(message);
        while (m.find()) {
            BUQuote q = new BUQuote(m.group(1));
            quotes.add(q);
            message = message.replace(m.group(0),
                    "<table width='90%' style='border:1px dashed #698fc7;font-size:"
                            + BUApplication.settings.contenttextsize
                            + "px;margin:5px;'><tr><td>" + q.toString()
                            + "</td></tr></table>");
//			message = message.replace(m.group(0), "");
//			Log.v("BUpost", "quote>>" + q.toString());
            m = p.matcher(message);
        }
    }

    public void parseMessage() {
        parseAt();
        parseImage();
        message = message.replace("[复制到剪贴板]", "");
    }

    public void parseAt() {
        // 找到@标记，并将超链接去掉
        Pattern p = Pattern
                .compile("<font color='Blue'>@<a href='/[^>]+'>([^\\s]+?)</a></font>");
        Matcher m = p.matcher(message);
        while (m.find()) {
            message = message.replace(m.group(0), "<font color='Blue'>@<u>"
                    + m.group(1) + "</u></font>");
        }
    }

    public void parseImage() {
//		message = message.replace("src='..", "scr='" + BUApplication.settings.ROOTURL);
        // 处理图片标签
        Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
        Matcher m = p.matcher(message);
        while (m.find()) {
            String path = "<img src='" + parseLocalImage(m.group(1)) + "'>";
            Log.i("BUPost", "Post>>" + author);
            Log.i("BUPost", "Image Path>>>" + path);
            if (BUApplication.settings.showimage)
                // 统一站内地址
                path = BUApiHelper.getImageAbsoluteUrl(path);
            else if (!path.contains("smilies_") && !path.contains("bz_"))
                path = "<img src='ic_image_white_48dp'>";
            message = message.replace(m.group(0), path);
        }
    }

    public String parseLocalImage(String imgUrl) {
        // 检查是否为本地表情文件
        Pattern p = Pattern.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif");
        Matcher m = p.matcher(imgUrl);
        if (m.find()) {
            imgUrl = m.group(1) + "_" + m.group(2);
        }
        return imgUrl;
    }

    public String getIcon() {
        return icon;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public String getDateline() {
        Date date = new Date(Long.parseLong(dateline) * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public String getMessage() {
        String m = message;
        // 如果有附件图，以html标记形式添加在最后
        // 如果附件不为图片，以超链接形式添加
        if (attachment != "null" && attachment != null && !attachment.isEmpty()) {
            String attUrl = BUApiHelper.getRootUrl() + "/" + attachment;
            m += "<br>附件：<br>";
            String format = attachment.substring(attachment.length() - 4);
            if (".jpg.png.bmp.gif".contains(format) && BUApplication.settings.showimage)
                m += "<a href='" + attUrl + "'><img src='" + attUrl
                        + "'></a>";
            else
                m += "<a href='" + attUrl + "'>" + attUrl + "</a>";
            Log.v("Attachment", ">>" + attUrl);
        }
        return m;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public List<BUQuote> getQuote() {
        return quotes;
    }

    public String toQuote() {
        String quote = message;
        // Clear other quotes in message
        quote = quote.replaceAll(
                "<table width='90%' style='border:1px dashed #698fc7;font-size:[0-9]{1,2}" +
                        "px;margin:5px;'><tr><td>" + "[\\w\\W]*"
                        + "</td></tr></table>", "");

        // Cut down the message if it's too long
        if (quote.length() > 250)
            quote = quote.substring(0, 250) + "......";

        // Change <br> to \n
        quote = quote.replace("<br>", "\n");
        // Change hypertext reference to Discuz style
        Pattern p = Pattern.compile("<a href='(.+?)'(?:.target='.+?')>(.+?)</a>");
        Matcher m = p.matcher(quote);
        while (m.find()) {
            String discuz = "[url=" + m.group(1) + "]" + m.group(2) + "[/url]";
            quote = quote.replace(m.group(0), discuz);
            m = p.matcher(quote);
        }
        // Change image to Discuz style
        p = Pattern.compile("<img src='([^>])'>");
        m = p.matcher(quote);
        while (m.find()) {
            quote = quote.replace(m.group(0), "[img]" + m.group(1) + "[/img]");
            m = p.matcher(quote);
        }
        // Clear other HTML marks
        quote = Html.fromHtml(quote).toString();
        quote = "[quote=" + pid + "][b]" + getAuthor() + "[/b] "
                + getDateline() + "\n" + quote + "[/quote]\n";
        if (!BUApplication.settings.referenceat)
            return quote;
        else
            return quote + "[@]" + getAuthor() + "[/@]\n";
    }

    /**
     * Change HTML message returned from server to application style.
     *
     * @return String of costumed HTML style for layout
     */
    public String getHtmlLayout(int count) {
        String htmlcontent;
        htmlcontent = "<p><div class='tdiv'>" +
                "<table width='100%' style='background-color:#92ACD3;padding:2px 5px;font-size:" + BUApplication.settings.titletextsize + "px;'>" +
                "<tr><td>#" + count + "&nbsp;<span onclick=authorOnClick(" + authorid + ")>" + getAuthor() +
                "</span>&nbsp;&nbsp;&nbsp;<span onclick=referenceOnClick(" + count + ")><u>引用</u></span></td>" +
                "<td style='text-align:right;'>" + getDateline() + "</td></tr></table>" +
                "</div>" +
                "<div class='mdiv' width='100%' style='padding:5px;word-break:break-all;'>" +
                getMessage() + "</div></p>";
        return htmlcontent;
    }
}
