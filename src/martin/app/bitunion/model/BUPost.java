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

import martin.app.bitunion.BUApp;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.Utils;

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
    private Attachment attachment;
    private int uid;
    private String username;
    private String avatar;
    private List<BUQuote> quotes = new ArrayList<BUQuote>();

    public static class Attachment {
        public final String fileName;
        public final String fileType;
        public final String size;
        public final String url;
        public final int downloads;
        public Attachment(String name, String type, String size, String url, int dwn) {
            fileName = name;
            fileType = type;
            this.size = size;
            this.url = url;
            downloads = dwn;
        }
    }

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
        if (attachment != null) {
            dest.writeByte((byte) 0x01);
            dest.writeString(attachment.fileName);
            dest.writeString(attachment.fileType);
            dest.writeString(attachment.size);
            dest.writeString(attachment.url);
            dest.writeInt(attachment.downloads);
        } else
            dest.writeByte((byte) 0x00);
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
        if (in.readByte() == 0x01) {
            String name = in.readString();
            String type = in.readString();
            String size = in.readString();
            String url = in.readString();
            int dwn = in.readInt();
            attachment = new Attachment(name, type, size, url, dwn);
        }
        uid = in.readInt();
        username = in.readString();
        avatar = in.readString();
        if (in.readByte() != 0x00) {
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

    public BUPost(JSONObject jsonObject) throws JSONException{
        String tmp;
        try {
            pid = jsonObject.optInt("pid");
            fid = jsonObject.optInt("fid");
            tid = jsonObject.getInt("tid");
            aid = jsonObject.optInt("aid");
            icon = jsonObject.optString("icon");
            author = URLDecoder.decode(jsonObject.getString("author"), "utf-8");
            authorid = jsonObject.getInt("authorid");
            subject = URLDecoder.decode(jsonObject.optString("subject", ""), "utf-8");
            dateline = jsonObject.getString("dateline");
            message = URLDecoder.decode(jsonObject.optString("message", ""), "utf-8");
            usesig = jsonObject.getString("usesig");
            if (!jsonObject.isNull("attachment")) {
                attachment = new Attachment(
                        URLDecoder.decode(jsonObject.optString("filename", ""), "utf-8"),
                        URLDecoder.decode(jsonObject.optString("filetype", ""), "utf-8"),
                        URLDecoder.decode(jsonObject.optString("attachsize", ""), "utf-8"),
                        URLDecoder.decode(jsonObject.optString("attachment", ""), "utf-8"),
                        jsonObject.optInt("downloads", 0));
            }
            uid = jsonObject.getInt("uid");
            username = URLDecoder.decode(jsonObject.getString("username"), "utf-8");
            avatar = URLDecoder.decode(jsonObject.optString("avatar", ""), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Parse subject
        if (subject != null && !subject.isEmpty()) {
            subject = subject.replaceAll("<[^>]+>", "");
            subject = Utils.replaceHtmlChar(subject);
        } else
            subject = null;

        parseQuote();
        parseMessage();
    }

    private void parseQuote() {
        message = message.replace("\"", "'");
        Pattern p = Pattern.compile(Utils.QUOTE_REGEX);
        Matcher m = p.matcher(message);
        while (m.find()) {
            BUQuote q = new BUQuote(m.group(1));
            quotes.add(q);
            message = message.replace(m.group(0),
                    "<table width='90%' style='border:1px dashed #698fc7;font-size:"
                            + BUApp.settings.contenttextsize
                            + "px;margin:5px;'><tr><td>" + q.toString()
                            + "</td></tr></table>");
//			message = message.replace(m.group(0), "");
//			Log.v("BUpost", "quote>>" + q.toString());
            m = p.matcher(message);
        }
    }

    private void parseMessage() {
        parseAt();
        parseImage();
        message = message.replace("[复制到剪贴板]", "");
    }

    /**
     * 找到@标记，并将超链接去掉
     */
    private void parseAt() {
        Pattern p = Pattern.compile("<font color='Blue'>@<a href='/[^>]+'>([^\\s]+?)</a></font>");
        Matcher m = p.matcher(message);
        while (m.find()) {
            message = message.replace(m.group(0), "<font color='Blue'>@<u>"
                    + m.group(1) + "</u></font>");
        }
    }

    private void parseImage() {
//		message = message.replace("src='..", "scr='" + BUApp.settings.ROOTURL);
        // 处理图片标签
        Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
        Matcher m = p.matcher(message);
        while (m.find()) {
            String url = parseLocalImage(m.group(1));
            String path = "<span onclick=imageOnClick('"+url+"')><img src='" + url + "'></span>";
            Log.i("BUPost", "Post>>" + author);
            Log.i("BUPost", "Image Path>>>" + path);
            if (BUApp.settings.showImage)
                // 统一站内地址
                path = BUApi.getImageAbsoluteUrl(path);
            else if (!path.contains("smilies_") && !path.contains("bz_"))
                path = "<img src='ic_image_white_48dp'>";
            message = message.replace(m.group(0), path);
        }
    }

    private String parseLocalImage(String imgUrl) {
        // 检查是否为本地表情文件
        Pattern p = Pattern.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif$");
        Matcher m = p.matcher(imgUrl);
        if (m.find()) {
            // Use local assets for emotions
            imgUrl = "file:///android_asset/" + m.group(1) + "_" + m.group(2)+".gif";
            Log.d("BUPost", "local emotion >> " + imgUrl);
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
        StringBuilder m = new StringBuilder(message);
        // 如果有附件图，以html标记形式添加在最后
        // 如果附件不为图片，以超链接形式添加
        if (attachment != null) {
            String attUrl = BUApi.getRootUrl() + "/" + attachment.url;
            m.append("<br>");
            m.append("<b>附件:</b> ");
            m.append("<a href='"+attUrl+"'>"+attachment.fileName+"</a>");
            m.append(" <i>" + attachment.size + "</i>");
            m.append("<br>");
            if (attachment.fileType.startsWith("image/") && BUApp.settings.showImage)
                m.append("<span onclick=imageOnClick('" + attUrl+ "')><img src='" + attUrl + "'></span>");
            Log.v("Attachment", ">>" + attUrl);
        }
        return m.toString();
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
        if (!BUApp.settings.useReferAt)
            return quote;
        else
            return quote + "[@]" + getAuthor() + "[/@]\n";
    }

    /**
     * Change HTML message returned from server to application style.
     *
     * @return HTML text for layout
     */
    public String getHtmlLayout(int count) {
        String htmlcontent;
        htmlcontent = "<p><div class='tdiv'>" +
                "<table width='100%' style='background-color:#92ACD3;padding:2px 5px;font-size:" + BUApp.settings.titletextsize + "px;'>" +
                "<tr><td>#" + count + "&nbsp;<span onclick=authorOnClick(" + authorid + ")>" + getAuthor() +
                "</span>&nbsp;&nbsp;&nbsp;<span onclick=referenceOnClick(" + count + ")><u>引用</u></span></td>" +
                "<td style='text-align:right;'>" + getDateline() + "</td></tr></table>" +
                "</div>" +
                "<div class='mdiv' width='100%' style='padding:5px;word-break:break-all;'>" +
                getMessage() + "</div></p>";
        return htmlcontent;
    }
}
