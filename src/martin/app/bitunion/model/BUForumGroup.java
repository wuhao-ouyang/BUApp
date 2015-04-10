package martin.app.bitunion.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BUForumGroup extends BUContent implements Parcelable {
    public final int gid;
    public final String groupName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gid);
        dest.writeString(groupName);
    }

    private BUForumGroup(Parcel in) {
        this.gid = in.readInt();
        this.groupName = in.readString();
    }

    public BUForumGroup(int gid, String groupName) {
        this.gid = gid;
        this.groupName = groupName;
    }

    public static final Parcelable.Creator<BUForumGroup> CREATOR = new Creator<BUForumGroup>() {
        @Override
        public BUForumGroup createFromParcel(Parcel source) {
            return new BUForumGroup(source);
        }

        @Override
        public BUForumGroup[] newArray(int size) {
            return new BUForumGroup[size];
        }
    };
}
