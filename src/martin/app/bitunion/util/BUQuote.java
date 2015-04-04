package martin.app.bitunion.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class BUQuote extends BUContent {

	private String author;
	private String postdate;
	private String message;

	public BUQuote(String unparsed) {
		this.author = "";
		this.postdate = "";
		this.message = unparsed;
		Pattern p = Pattern
				.compile("<b>([^<]+)</b> (\\d{4}-\\d{1,2}-\\d{1,2} \\d{2}:\\d{2}( (A|P)M)*)");
		Matcher m = p.matcher(this.message);
		if (m.find()) {
			this.author = m.group(1);
			this.postdate = m.group(2);
			this.message = this.message.replace(m.group(0), "");
		}
	}

	public BUQuote(String author, String postdate, String message) {
		super();
		this.author = author;
		this.postdate = postdate;
		this.message = message;
	}

	public String getAuthor() {
		return author;
	}

	public String getPostdate() {
		return postdate;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return "ÒýÓÃ:\t" + author + "\t\t" + postdate + message;
	}
}
