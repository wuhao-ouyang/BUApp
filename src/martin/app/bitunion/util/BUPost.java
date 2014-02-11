package martin.app.bitunion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BUPost extends BUContent {

	private String pid;
	private String fid;
	private String tid;
	private String aid;
	private String icon;
	private String author;
	private String authorid;
	private String subject;
	private String dateline;
	private String message;
	private String usesig;
	private String attachment;
	private String uid;
	private String username;
	private String avatar;
	private JSONObject json;

	private ArrayList<BUQuote> quote = new ArrayList<BUQuote>();
	private ArrayList<String> images = new ArrayList<String>();

	public BUPost(JSONObject jsonObject) {
		json = jsonObject;
		try {
			pid = jsonObject.getString("pid");
			fid = jsonObject.getString("fid");
			tid = jsonObject.getString("tid");
			aid = jsonObject.getString("aid");
			icon = jsonObject.getString("icon");
			author = URLDecoder.decode(jsonObject.getString("author"), "utf-8");
			authorid = jsonObject.getString("authorid");
			subject = URLDecoder.decode(jsonObject.getString("subject"),
					"utf-8");
			dateline = jsonObject.getString("dateline");
			message = URLDecoder.decode(jsonObject.getString("message"),
					"utf-8");
			usesig = jsonObject.getString("usesig");
			attachment = URLDecoder.decode(jsonObject.getString("attachment"),
					"utf-8");
			uid = jsonObject.getString("uid");
			username = URLDecoder.decode(jsonObject.getString("username"),
					"utf-8");
			avatar = URLDecoder.decode(jsonObject.getString("avatar"), "utf-8");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		Pattern p = Pattern.compile(BUAppUtils.QUOTE_REGEX, Pattern.DOTALL);
		Matcher m = p.matcher(message);
		while (m.find()) {
			message = message.replace(m.group(0), "");
			quote.add(new BUQuote(m.group(1)));
			m = p.matcher(message);
		}
	}

	public void parseMessage() {
		parseAt();
		parseImage();
		// 去掉除了特定标记外的其他所有html标签
		message = message
				.replaceAll(
						"<(?!(a href=[^>]+>)|(/a>|i>|/i>|img src="
								+ "|font color='(Blue|Red|Green|Purple|Maroon|Orange|Brown|Pink|Yellow|LimeGreen)'>"
								+ "|/font>|u>|/u>))[^>]+>", "");
		// 截去最后换行符，并把所有换行符替换成html标记<br>
		message = message.substring(0, message.length());
		message = message.replace("\n", "<br>");
		message = message.replace("..::", "<br>..::");
		// 如果有附件图，以html标记形式添加在最后
		if (attachment != "null" && attachment != null && !attachment.isEmpty()) {
			String format = attachment.substring(attachment.length() - 4);
			if (".jpg.png.bmp.gif".contains(format))
				message += "<br><img src='../" + attachment + "'>";
		}
	}

	public void parseAt() {
		// 找到@标记，并将超链接去掉
		Pattern p = Pattern
				.compile("<font color='Blue'>@<a href='/[^>]+'>([^\\s]+?)</a></font>");
		Matcher m = p.matcher(message);
		while (m.find()) {
			message = message.replace(m.group(0), "<font color='Blue'>@ <u>"
					+ m.group(1) + "</u></font>");
		}
	}

	public void parseImage() {
		// Pattern p = Pattern.compile("<img src='[^>]+>");
		// Matcher m = p.matcher(message);
		// while
	}

	public String getPid() {
		return pid;
	}

	public String getFid() {
		return fid;
	}

	public String getTid() {
		return tid;
	}

	public String getAid() {
		return aid;
	}

	public String getIcon() {
		return icon;
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
		Date date = new Date(Integer.parseInt(dateline) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public String getMessage() {
		// return Html.fromHtml("<br>" + message, , null);
		return message;
	}

	public String getUsesig() {
		return usesig;
	}

	public String getAttachment() {
		return attachment;
	}

	public String getUid() {
		return uid;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatar() {
		return avatar;
	}

	public ArrayList<BUQuote> getQuote() {
		return quote;
	}

	public String toString() {
		return json.toString();
	}
}
