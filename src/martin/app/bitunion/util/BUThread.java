package martin.app.bitunion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class BUThread extends BUContent {

	private String tid;
	private String author;
	private String authorid;
	private String subject;
	private String dateline;
	private String lastpost;
	private String lastposter;
	private String views;
	private String replies;
	private JSONObject jsonObject;

	public BUThread(JSONObject object) {
		jsonObject = object;
		try {
			tid = object.getString("tid");
			author = URLDecoder.decode(object.getString("author"), "utf-8");
			authorid = object.getString("authorid");
			subject = URLDecoder.decode(object.getString("subject"), "utf-8");
			dateline = object.getString("dateline");
			lastpost = object.getString("lastpost");
			lastposter = object.getString("lastposter");
			views = object.getString("views");
			replies = object.getString("replies");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
		subject = BUAppUtils.replaceHtmlChar(subject);
	}

	public String getTid() {
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
		Date date = new Date(Integer.parseInt(dateline) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public String getLastpost() {
		return lastpost;
	}

	public String getLastposter() {
		return lastposter;
	}

	public String getViews() {
		int n = Integer.parseInt(views);
		if (n > 9999){
			return Integer.toString(n / 10000) + "." + Integer.toString(n % 10000 / 1000) + "Íò";
		} else
		return views;
	}

	public String getReplies() {
		int n = Integer.parseInt(replies);
		if (n > 9999){
			return Integer.toString(n / 1000) + "." + Integer.toString(n % 1000 / 100) + "Íò";
		} else
		return replies;
	}
	
	public String toString(){
		return jsonObject.toString();
	}

}
