package martin.app.bitunion.model;

import android.os.Parcel;

public class BUForum extends BUContent {

    private final String name;
    private final int fid;
    private final int type;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(fid);
        dest.writeInt(type);
    }

    public static final Creator<BUForum> CREATOR = new Creator<BUForum>() {
        @Override
        public BUForum createFromParcel(Parcel source) {
            return new BUForum(source);
        }

        @Override
        public BUForum[] newArray(int size) {
            return new BUForum[size];
        }
    };

    private BUForum(Parcel in) {
        name = in.readString();
        fid = in.readInt();
        type = in.readInt();
    }

    public BUForum(String name, int fid, int type) {
        this.name = name;
        this.fid = fid;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getFid() {
        return fid;
    }

    public int getType() {
        return type;
    }

}
