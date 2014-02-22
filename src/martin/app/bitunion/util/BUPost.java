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

import martin.app.bitunion.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
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
	private int count;

	private ArrayList<BUQuote> quote = new ArrayList<BUQuote>();
	private ArrayList<String> images = new ArrayList<String>();

	public BUPost(JSONObject jsonObject, int count) {
		json = jsonObject;
		this.count = count;
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
		Pattern p = Pattern.compile(BUAppUtils.QUOTE_REGEX);
		Matcher m = p.matcher(message);
		while (m.find()) {
			BUQuote q = new BUQuote(m.group(1));
			quote.add(q);
			message = message.replace(
					m.group(0),
					"<table width='90%' style='border:1px dashed #698fc7;font-size:"+ MainActivity.settings.titletextsize +"px;margin:5px;'><tr><td>"
							+ q.toString() + "\n</td></tr></table>");
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
//		message = message.replace("src='..", "scr='" + MainActivity.settings.ROOTURL);
		// 处理图片标签
		Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
		Matcher m = p.matcher(message);
		while (m.find()) {
			String path = "<img src='" + parseLocalImage(m.group(1)) + "'>";
			// 统一站内地址
			path = path.replaceAll("(http://)?((out.|kiss.|www.)?" +
					"bitunion.org|btun.yi.org|10.1.10.253)", MainActivity.settings.ROOTURL);
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
		String m = message;
		// 如果有附件图，以html标记形式添加在最后
		// 如果附件不为图片，以超链接形式添加
		if (attachment != "null" && attachment != null && !attachment.isEmpty()) {
			String attUrl = MainActivity.settings.ROOTURL + "/" + attachment;
			m += "<br>附件：<br>";
			String format = attachment.substring(attachment.length() - 4);
			if (".jpg.png.bmp.gif".contains(format))
				m += "<a href='" + attUrl + "'><img src='" + attUrl
						+ "'></a>";
			else
				m += "<a href='" + attUrl + "'>" + attUrl + "</a>";
			Log.v("Attachment", ">>" + attUrl);
		}
		return m;
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

	public String toQuote() {
		String quote = message;
		
		// Cut down the message if it's too long
		if (quote.length() > 250)
			quote = quote.substring(0, 250) + "......";
		
		// Clear other quotes in message
		quote = quote.replaceAll("<table width='90%' style='border:1px dashed #698fc7;font-size:"+ MainActivity.settings.titletextsize +"px;margin:5px;'><tr><td>" +
				"[.\n]*\n</td></tr></table>", "");
		
		// Change <br> to \n
		quote = quote.replace("<br>", "\n");
		// Change hypertext reference to Discuz style
		Pattern p = Pattern.compile("<a href='(.+?)'(?:.target='.+?')>(.+?)</a>");
		Matcher m = p.matcher(quote);
		while (m.find()) {
			String discuz ="[url=" + m.group(1) +"]" + m.group(2) + "[/url]";
			quote = quote.replace(m.group(0), discuz);
			m = p.matcher(quote);
		}
		// Change image to Discuz style
		p = Pattern.compile("<img src='([^>])'>");
		m = p.matcher(quote);
		while (m.find()){
			quote = quote.replace(m.group(0), "[img]" + m.group(1) + "[/img]");
			m = p.matcher(quote);
		}
		// Clear other HTML marks
		quote = Html.fromHtml(quote).toString();
		quote = "[quote=" + getPid() + "][b]" + getAuthor() + "[/b] "
				+ getDateline() + "\n" + quote + "[/quote]\n";
		return quote;
	}
	
	public int getCount(){
		return count;
	}
	
	/**
	 * Change HTML message returned from server to application style.
	 * @return String of costumed HTML style for layout
	 */
	public String getHtmlLayout(){
		String htmlcontent;
			htmlcontent = "<p><div class='tdiv'>" +
					"<table width='100%' style='background-color:#92ACD3;padding:2px 5px;font-size:"+ MainActivity.settings.titletextsize +"px;'>" +
					"<tr><td>#" + Integer.toString(getCount()) + "&nbsp;<span onclick=authorOnClick(" + getAuthorid() +")>" + getAuthor() + 
					"</span>&nbsp;&nbsp;&nbsp;<span onclick=referenceOnClick("+ getCount() +")><u>引用</u></span></td>" +
					"<td style='text-align:right;'>" + getDateline() + "</td></tr></table>" +
					"</div>" +
					"<div class='mdiv' width='100%' style='padding:5px;word-break:break-all;'>" + 
					getMessage() + "</div></p>";
		return htmlcontent;
	}
}
