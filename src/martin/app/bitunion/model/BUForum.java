package martin.app.bitunion.model;

public class BUForum extends BUContent {

    private final String name;
    private final int fid;
    private final int type;

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
