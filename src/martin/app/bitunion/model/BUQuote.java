package martin.app.bitunion.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BUQuote extends BUContent implements Parcelable {

    private String author;
    private String postdate;
    private String message;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(postdate);
        dest.writeString(message);
    }

    BUQuote(Parcel in) {
        author = in.readString();
        postdate = in.readString();
        message = in.readString();
    }

    public static final Parcelable.Creator<BUQuote> CREATOR =  new Parcelable.Creator<BUQuote>() {

        @Override
        public BUQuote createFromParcel(Parcel source) {
            return new BUQuote(source);
        }

        @Override
        public BUQuote[] newArray(int size) {
            return new BUQuote[size];
        }
    };

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

    public String toString() {
        return "引用:\t" + author + "\t\t" + postdate + message;
    }
}
