package martin.app.bitunion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Strider_oy
 *
 */
public class BUUserInfo {

	String uid;
	String status;
	String username;
	String avatar;
	String credit;
	String regdate;
	String lastvisit;
	String bday;
	String signature;
	String postnum;
	String threadnum;
	String email;
	String site;
	
	public BUUserInfo(JSONObject json) {
		try {
			uid = json.getString("uid");
			status = json.getString("status");
			username = URLDecoder.decode(json.getString("username"), "utf-8");
			avatar = URLDecoder.decode(json.getString("avatar"), "utf-8");
			credit = json.getString("credit");
			regdate = json.getString("regdate");
			lastvisit = json.getString("lastvisit");
			bday = json.getString("bday");
			signature = URLDecoder.decode(json.getString("signature"), "utf-8");
			postnum = json.getString("postnum");
			threadnum = json.getString("threadnum");
			email = URLDecoder.decode(json.getString("email"), "utf-8");
			site = URLDecoder.decode(json.getString("site"), "utf-8");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getUid() {
		return uid;
	}

	public String getStatus() {
		return status;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatar() {
		return avatar;
	}

	public String getCredit() {
		return credit;
	}

	public String getRegdate() {
		Date date = new Date(Integer.parseInt(regdate) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public String getRegdateLong() {
		Date date = new Date(Integer.parseInt(regdate) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public String getLastvisit() {
		Date date = new Date(Integer.parseInt(lastvisit) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public String getLastvisitLong() {
		Date date = new Date(Integer.parseInt(lastvisit) * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public String getBday() {
		return bday;
	}

	public String getSignature() {
		return signature;
	}

	public String getPostnum() {
		return postnum;
	}

	public String getThreadnum() {
		return threadnum;
	}

	public String getEmail() {
		return email;
	}

	public String getSite() {
		return site;
	}
}
